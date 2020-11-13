package com.elementary.tasks.birthdays.create

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
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
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel
import com.elementary.tasks.databinding.ActivityAddBirthdayBinding
import com.elementary.tasks.pin.PinLoginActivity
import timber.log.Timber
import java.text.ParseException
import java.util.*

class AddBirthdayActivity : BindingActivity<ActivityAddBirthdayBinding>(R.layout.activity_add_birthday) {

  private val viewModel: BirthdayViewModel by lazy {
    ViewModelProvider(this, BirthdayViewModel.Factory(getId())).get(BirthdayViewModel::class.java)
  }
  private var mBirthday: Birthday? = null

  private var mDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, monthOfYear)
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    viewModel.date.postValue(calendar.timeInMillis)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()
    if (prefs.isTelephonyAllowed) {
      binding.contactCheck.visibility = View.VISIBLE
      binding.contactCheck.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked && !prefs.isTelephonyAllowed) return@setOnCheckedChangeListener
        viewModel.isContactAttached.postValue(isChecked)
      }
    } else {
      binding.contactCheck.visibility = View.GONE
    }

    ViewUtils.listenScrollableView(binding.scrollView) {
      binding.appBar.isSelected = it > 0
    }

    binding.birthDate.setOnClickListener { dateDialog() }
    binding.pickContact.setOnClickListener { pickContact() }

    loadBirthday()

    if (savedInstanceState == null) {
      viewModel.isContactAttached.postValue(false)
      viewModel.isLogged = intent.getBooleanExtra(ARG_LOGGED, false)
    }
  }

  private fun getId(): String = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  override fun onStart() {
    super.onStart()
    if (prefs.hasPinCode && !viewModel.isLogged) {
      PinLoginActivity.verify(this)
    }
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
  }

  private fun showBirthday(birthday: Birthday?, fromFile: Boolean = false) {
    this.mBirthday = birthday

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    binding.toolbar.setTitle(R.string.add_birthday)
    if (birthday != null) {
      binding.toolbar.setTitle(R.string.edit_birthday)
      if (!viewModel.isEdited) {
        binding.birthName.setText(birthday.name)
        try {
          val dt = TimeUtil.BIRTH_DATE_FORMAT.parse(birthday.date)
          if (dt != null) calendar.time = dt
        } catch (e: ParseException) {
          e.printStackTrace()
        }

        viewModel.date.postValue(calendar.timeInMillis)

        if (!TextUtils.isEmpty(birthday.number)) {
          binding.numberView.setText(birthday.number)
          binding.contactCheck.isChecked = true
        }
        viewModel.isEdited = true
        viewModel.isFromFile = fromFile
        if (fromFile) {
          viewModel.findSame(birthday.uuId)
        }
      }
    }
  }

  private fun loadBirthday() {
    initViewModel()
    when {
      intent.data != null -> {
        readUri()
      }
      intent.hasExtra(Constants.INTENT_ITEM) -> {
        try {
          mBirthday = intent.getParcelableExtra(Constants.INTENT_ITEM) as Birthday?
          showBirthday(mBirthday, true)
        } catch (e: Exception) {
        }
      }
      intent.hasExtra(Constants.INTENT_DATE) -> {
        viewModel.date.postValue(intent.getLongExtra(Constants.INTENT_DATE, System.currentTimeMillis()))
      }
      else -> {
        if ((viewModel.date.value ?: 0L) == 0L) {
          viewModel.date.postValue(System.currentTimeMillis())
        }
      }
    }
  }

  private fun readUri() {
    if (!Permissions.checkPermission(this, SD_REQ, Permissions.READ_EXTERNAL)) {
      return
    }
    intent.data?.let {
      try {
        mBirthday = if (ContentResolver.SCHEME_CONTENT != it.scheme) {
          val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_BIRTHDAY)
          if (any != null && any is Birthday) {
            any
          } else null
        } else null
        showBirthday(mBirthday, true)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun initViewModel() {
    viewModel.birthday.observe(this, { this.showBirthday(it) })
    viewModel.result.observe(this, { commands ->
      commands?.let {
        when (it) {
          Commands.SAVED, Commands.DELETED -> closeScreen()
          else -> {
          }
        }
      }
    })
    viewModel.date.observe(this, { millis ->
      millis?.let {
        Timber.d("initViewModel: ${TimeUtil.getFullDateTime(millis, true)}")
        binding.birthDate.text = TimeUtil.BIRTH_DATE_FORMAT.format(Date(it))
      }
    })
    viewModel.isContactAttached.observe(this, { isAttached ->
      isAttached?.let {
        binding.container.visibility = if (it) View.VISIBLE else View.GONE
      }
    })
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
    if (mBirthday != null && !viewModel.isFromFile) {
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
    val contact = binding.birthName.text.toString().trim()
    if (contact == "") {
      binding.birthNameLayout.error = getString(R.string.must_be_not_empty)
      binding.birthNameLayout.isErrorEnabled = true
      return
    }
    var contactId = 0L
    val number = binding.numberView.text.toString().trim()
    if (binding.contactCheck.isChecked) {
      if (TextUtils.isEmpty(number)) {
        binding.numberLayout.error = getString(R.string.you_dont_insert_number)
        binding.numberLayout.isErrorEnabled = true
        return
      }
      if (!checkContactPermission(CONTACT_PERM)) {
        return
      }
      contactId = Contacts.getIdFromNumber(number, this)
    }
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = viewModel.date.value ?: System.currentTimeMillis()
    val birthday = (mBirthday ?: Birthday()).apply {
      this.name = contact
      this.contactId = contactId
      this.date = binding.birthDate.text.toString()
      this.number = number
      this.day = calendar.get(Calendar.DAY_OF_MONTH)
      this.month = calendar.get(Calendar.MONTH)
      this.dayMonth = "${this.day}|${this.month}"
    }
    if (newId) {
      birthday.uuId = UUID.randomUUID().toString()
    }
    viewModel.saveBirthday(birthday)
  }

  private fun closeScreen() {
    sendBroadcast(Intent(this, PermanentBirthdayReceiver::class.java)
      .setAction(PermanentBirthdayReceiver.ACTION_SHOW))
    setResult(Activity.RESULT_OK)
    finish()
  }

  private fun deleteItem() {
    mBirthday?.let { viewModel.deleteBirthday(it) }
  }

  private fun dateDialog() {
    val c = Calendar.getInstance()
    c.timeInMillis = viewModel.date.value ?: System.currentTimeMillis()
    TimeUtil.showDatePicker(this, prefs, c.get(Calendar.YEAR),
      c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), mDateCallBack)
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
    } else if (requestCode == PinLoginActivity.REQ_CODE) {
      if (resultCode != Activity.RESULT_OK) {
        finish()
      } else {
        viewModel.isLogged = true
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

  companion object {
    private const val SD_REQ = 555
    private const val MENU_ITEM_DELETE = 12
    private const val CONTACT_PERM = 102
    private const val ARG_LOGGED = "arg_logged"

    fun openLogged(context: Context, intent: Intent? = null) {
      if (intent == null) {
        context.startActivity(Intent(context, AddBirthdayActivity::class.java)
          .putExtra(ARG_LOGGED, true))
      } else {
        intent.putExtra(ARG_LOGGED, true)
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
