package com.elementary.tasks.birthdays.create

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.data.ContactData
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.listenScrollableView
import com.elementary.tasks.core.utils.ui.showError
import com.elementary.tasks.core.utils.ui.text
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.CreateBirthdayViewModel
import com.elementary.tasks.databinding.ActivityAddBirthdayBinding
import com.github.naz013.calendarext.newCalendar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.util.Calendar

class AddBirthdayActivity : BindingActivity<ActivityAddBirthdayBinding>() {

  private val viewModel by viewModel<CreateBirthdayViewModel> { parametersOf(idFromIntent()) }
  private val permissionFlow = PermissionFlow(this, dialogues)

  private val contactPicker = ContactPicker(this) { showContact(it) }

  override fun inflateBinding() = ActivityAddBirthdayBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()
    initContact()
    binding.scrollView.listenScrollableView { binding.appBar.isSelected = it > 0 }
    binding.birthDate.setOnClickListener { dateDialog() }
    binding.pickContact.setOnClickListener { pickContact() }
    loadBirthday()
    setInitState().takeIf { savedInstanceState == null }
  }

  private fun showContact(contact: ContactData) {
    if (binding.birthName.text.toString().trim() == "") {
      binding.birthName.setText(contact.name)
    }
    binding.numberView.setText(contact.phone)
  }

  private fun initContact() {
    if (prefs.isTelephonyAllowed) {
      binding.contactCheck.visible()
      binding.contactCheck.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked && !prefs.isTelephonyAllowed) return@setOnCheckedChangeListener
        viewModel.onContactAttached(isChecked)
      }
    } else {
      binding.contactCheck.gone()
    }
  }

  private fun setInitState() {
    viewModel.onContactAttached(false)
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
  }

  private fun showBirthday(birthday: Birthday?, fromFile: Boolean = false) {
    val calendar = newCalendar()
    binding.toolbar.setTitle(R.string.add_birthday)

    birthday?.also {
      viewModel.editBirthday(it)

      binding.toolbar.setTitle(R.string.edit_birthday)

      if (viewModel.isEdited) return
      binding.birthName.setText(it.name)
      runCatching {
        val dt = TimeUtil.BIRTH_DATE_FORMAT.parse(it.date)
        if (dt != null) calendar.time = dt
      }

      viewModel.onDateChanged(calendar.timeInMillis)

      if (!TextUtils.isEmpty(it.number)) {
        binding.numberView.setText(it.number)
        binding.contactCheck.isChecked = true
      }
      viewModel.isEdited = true
      viewModel.isFromFile = fromFile
      if (fromFile) {
        viewModel.findSame(it.uuId)
      }
    }
  }

  private fun loadBirthday() {
    initViewModel()
    when {
      intent.data != null -> {
        permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
          readUri()
        }
      }
      intent.hasExtra(Constants.INTENT_ITEM) -> showBirthday(birthdayFromIntent(), true)
      intent.hasExtra(Constants.INTENT_DATE) -> viewModel.onDateChanged(dateFromIntent())
      else -> {
        if ((viewModel.date.value ?: 0L) == 0L) {
          viewModel.onDateChanged(System.currentTimeMillis())
        }
      }
    }
  }

  private fun idFromIntent() = intentString(Constants.INTENT_ID)

  private fun dateFromIntent() = intentLong(Constants.INTENT_DATE, System.currentTimeMillis())

  private fun birthdayFromIntent() = intentParcelable(Constants.INTENT_ITEM, Birthday::class.java)

  private fun readUri() {
    intent.data?.let {
      runCatching {
        showBirthday(
          if (ContentResolver.SCHEME_CONTENT != it.scheme) {
            val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_BIRTHDAY)
            if (any != null && any is Birthday) {
              any
            } else null
          } else null,
          true
        )
      }
    }
  }

  private fun initViewModel() {
    viewModel.birthday.observe(this) { showBirthday(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> closeScreen()
        else -> {
        }
      }
    }
    viewModel.date.nonNullObserve(this) {
      Timber.d("initViewModel: ${TimeUtil.getFullDateTime(it, true)}")
      binding.birthDate.text = TimeUtil.BIRTH_DATE_FORMAT.format(it.time)
    }
    viewModel.isContactAttached.nonNullObserve(this) { binding.container.visibleGone(it) }
  }

  private fun pickContact() {
    permissionFlow.askPermission(Permissions.READ_CONTACTS) { contactPicker.pickContact() }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_simple_save_action, menu)
    if (viewModel.isEdited && !viewModel.isFromFile) {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_add -> {
        askCopySaving()
        true
      }

      android.R.id.home -> {
        finish()
        true
      }

      MENU_ITEM_DELETE -> {
        dialogues.askConfirmation(this, getString(R.string.delete)) {
          if (it) deleteItem()
        }
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(this)
        .setMessage(R.string.same_birthday_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveBirthday(true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveBirthday()
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      saveBirthday()
    }
  }

  private fun saveBirthday(newId: Boolean = false) {
    val contact = binding.birthName.trimmedText()
    if (contact.isEmpty()) {
      binding.birthNameLayout.showError(R.string.must_be_not_empty)
      return
    }
    val number = binding.numberView.trimmedText().takeIf { binding.contactCheck.isChecked }
    viewModel.prepare(contact, number, binding.birthDate.text(), newId)
    if (binding.contactCheck.isChecked) {
      if (number.isNullOrEmpty()) {
        binding.numberLayout.showError(R.string.you_dont_insert_number)
        return
      }
      permissionFlow.askPermission(Permissions.READ_CONTACTS) { finalSave() }
      return
    }
    finalSave()
  }

  private fun finalSave() {
    analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_BIRTHDAY))
    viewModel.save()
  }

  private fun closeScreen() {
    sendBroadcast(
      Intent(this, PermanentBirthdayReceiver::class.java)
        .setAction(PermanentBirthdayReceiver.ACTION_SHOW)
    )
    setResult(Activity.RESULT_OK)
    finish()
  }

  private fun deleteItem() {
    if (viewModel.isEdited && !viewModel.isFromFile) {
      viewModel.deleteBirthday(viewModel.editableBirthday.uuId)
    }
  }

  private fun dateDialog() {
    TimeUtil.showDatePicker(this, prefs, viewModel.date.value) {
      viewModel.onDateChanged(it)
    }
  }

  override fun requireLogin() = true

  companion object {

    private const val MENU_ITEM_DELETE = 12

    fun createBirthDate(day: Int, month: Int, year: Int): String {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = System.currentTimeMillis()
      calendar.set(Calendar.YEAR, year)
      calendar.set(Calendar.MONTH, month)
      calendar.set(Calendar.DAY_OF_MONTH, day)
      return TimeUtil.BIRTH_DATE_FORMAT.format(calendar.time)
    }
  }
}
