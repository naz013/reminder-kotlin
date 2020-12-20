package com.elementary.tasks.birthdays.create

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.listenScrollableView
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.showError
import com.elementary.tasks.core.utils.text
import com.elementary.tasks.core.utils.trimmedText
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel
import com.elementary.tasks.databinding.ActivityAddBirthdayBinding
import com.github.naz013.calendarext.newCalendar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.text.ParseException
import java.util.*

class AddBirthdayActivity : BindingActivity<ActivityAddBirthdayBinding>() {

  private val viewModel by viewModel<BirthdayViewModel> { parametersOf(idFromIntent()) }

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

  private fun initContact() {
    if (prefs.isTelephonyAllowed) {
      binding.contactCheck.show()
      binding.contactCheck.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked && !prefs.isTelephonyAllowed) return@setOnCheckedChangeListener
        viewModel.onContactAttached(isChecked)
      }
    } else {
      binding.contactCheck.hide()
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
      try {
        val dt = TimeUtil.BIRTH_DATE_FORMAT.parse(it.date)
        if (dt != null) calendar.time = dt
      } catch (e: ParseException) {
        e.printStackTrace()
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
      intent.data != null -> readUri()
      intent.hasExtra(Constants.INTENT_ITEM) -> showBirthday(birthdayFromIntent(), true)
      intent.hasExtra(Constants.INTENT_DATE) -> viewModel.onDateChanged(dateFromIntent())
      else -> {
        if ((viewModel.date.value ?: 0L) == 0L) {
          viewModel.onDateChanged(System.currentTimeMillis())
        }
      }
    }
  }

  private fun idFromIntent() = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  private fun dateFromIntent() =
    intent.getLongExtra(Constants.INTENT_DATE, System.currentTimeMillis())

  private fun birthdayFromIntent() = try {
    intent.getParcelableExtra(Constants.INTENT_ITEM) as? Birthday?
  } catch (e: Exception) {
    null
  }

  private fun readUri() {
    if (!Permissions.checkPermission(this, SD_REQ, Permissions.READ_EXTERNAL)) {
      return
    }
    intent.data?.let {
      try {
        showBirthday(
          if (ContentResolver.SCHEME_CONTENT != it.scheme) {
            val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_BIRTHDAY)
            if (any != null && any is Birthday) {
              any
            } else null
          } else null,
          true
        )
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun initViewModel() {
    viewModel.birthday.observe(this) { showBirthday(it) }
    viewModel.result.observe(this) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> closeScreen()
        else -> {
        }
      }
    }
    viewModel.date.observe(this) {
      Timber.d("initViewModel: ${TimeUtil.getFullDateTime(it, true)}")
      binding.birthDate.text = TimeUtil.BIRTH_DATE_FORMAT.format(it.time)
    }
    viewModel.isContactAttached.observe(this) { binding.container.visibleGone(it) }
  }

  private fun checkContactPermission(code: Int): Boolean {
    if (!Permissions.checkPermission(this, code, Permissions.READ_CONTACTS)) {
      return false
    }
    return true
  }

  private fun pickContact() {
    if (!checkContactPermission(101)) {
      return
    }
    SuperUtil.selectContact(this, Constants.REQUEST_CODE_CONTACTS)
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
    if (binding.contactCheck.isChecked) {
      if (number.isNullOrEmpty()) {
        binding.numberLayout.showError(R.string.you_dont_insert_number)
        return
      }
      if (!checkContactPermission(CONTACT_PERM)) {
        return
      }
    }
    viewModel.save(contact, number, binding.birthDate.text(), newId)
  }

  private fun closeScreen() {
    sendBroadcast(Intent(this, PermanentBirthdayReceiver::class.java)
      .setAction(PermanentBirthdayReceiver.ACTION_SHOW))
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
      if (Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
        val contact = Contacts.readPickerResults(this, requestCode, resultCode, data)
        if (contact != null) {
          if (binding.birthName.text.toString().trim() == "") {
            binding.birthName.setText(contact.name)
          }
          binding.numberView.setText(contact.phone)
        }
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      101 -> if (Permissions.checkPermission(grantResults)) {
        SuperUtil.selectContact(this, Constants.REQUEST_CODE_CONTACTS)
      }
      CONTACT_PERM -> if (Permissions.checkPermission(grantResults)) {
        askCopySaving()
      }
      SD_REQ -> if (Permissions.checkPermission(grantResults)) {

      }
    }
  }

  override fun requireLogin() = true

  companion object {
    private const val SD_REQ = 555
    private const val MENU_ITEM_DELETE = 12
    private const val CONTACT_PERM = 102

    fun openLogged(context: Context, intent: Intent? = null) {
      if (intent == null) {
        context.startActivity(Intent(context, AddBirthdayActivity::class.java)
          .putExtra(ARG_LOGIN_FLAG, true))
      } else {
        intent.putExtra(ARG_LOGIN_FLAG, true)
        context.startActivity(intent)
      }
    }

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
