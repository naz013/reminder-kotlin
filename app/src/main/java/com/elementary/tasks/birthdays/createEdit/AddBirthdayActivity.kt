package com.elementary.tasks.birthdays.createEdit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.birthdays.BirthdayViewModel
import kotlinx.android.synthetic.main.activity_add_birthday.*
import java.io.IOException
import java.text.ParseException
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class AddBirthdayActivity : ThemedActivity() {

    private lateinit var viewModel: BirthdayViewModel

    private var myYear = 0
    private var myMonth = 0
    private var myDay = 0
    private var number: String = ""
    private var mBirthday: Birthday? = null
    private var date: Long = 0

    @Inject lateinit var backupTool: BackupTool

    private var myDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        myYear = year
        myMonth = monthOfYear
        myDay = dayOfMonth
        val monthStr: String = if (myMonth < 9) {
            "0" + (myMonth + 1)
        } else
            (myMonth + 1).toString()
        val dayStr: String = if (myDay < 10) {
            "0$myDay"
        } else
            myDay.toString()
        birthDate.text = SuperUtil.appendString(myYear.toString(), "-", monthStr, "-", dayStr)
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_birthday)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        container.visibility = View.GONE
        contactCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                container.visibility = View.VISIBLE
            else
                container.visibility = View.GONE
        }
        birthDate.setOnClickListener { dateDialog() }
        pickContact.setOnClickListener { pickContact() }

        loadBirthday()
    }

    private fun showBirthday(birthday: Birthday?) {
        this.mBirthday = birthday

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        toolbar.setTitle(R.string.add_birthday)
        if (birthday != null) {
            birthName.setText(birthday.name)
            try {
                val dt = CheckBirthdaysAsync.DATE_FORMAT.parse(birthday.date)
                if (dt != null) calendar.time = dt
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (!TextUtils.isEmpty(birthday.number)) {
                phone.setText(birthday.number)
                contactCheck.isChecked = true
            }
            toolbar.setTitle(R.string.edit_birthday)
            this.number = birthday.number
        } else if (date != 0L) {
            calendar.timeInMillis = date
        }
        myYear = calendar.get(Calendar.YEAR)
        myMonth = calendar.get(Calendar.MONTH)
        myDay = calendar.get(Calendar.DAY_OF_MONTH)
        birthDate.text = CheckBirthdaysAsync.DATE_FORMAT.format(calendar.time)
    }

    private fun loadBirthday() {
        date = intent.getLongExtra(Constants.INTENT_DATE, 0)
        val id = intent.getIntExtra(Constants.INTENT_ID, 0)
        initViewModel(id)
        if (intent.data != null) {
            try {
                val name = intent.data
                val scheme = name!!.scheme
                mBirthday = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    backupTool.getBirthday(cr, name)
                } else {
                    backupTool.getBirthday(name.path, null)
                }
                showBirthday(mBirthday)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    private fun initViewModel(id: Int) {
        viewModel = ViewModelProviders.of(this, BirthdayViewModel.Factory(application, id)).get(BirthdayViewModel::class.java)
        viewModel.birthday.observe(this, Observer<Birthday> { this.showBirthday(it) })
        viewModel.result.observe(this, Observer<Commands> {
            if (it != null) {
                when (it) {
                    Commands.SAVED, Commands.DELETED -> closeScreen()
                }
            }
        })
    }

    private fun checkContactPermission(code: Int): Boolean {
        if (!Permissions.checkPermission(this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            Permissions.requestPermission(this, code, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
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
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_group_edit, menu)
        if (mBirthday != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                saveBirthday()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mBirthday != null && prefs!!.isAutoSaveEnabled) {
            saveBirthday()
        }
    }

    private fun saveBirthday() {
        val contact = birthName.text!!.toString()
        if (contact.matches("".toRegex())) {
            birthName.error = getString(R.string.must_be_not_empty)
            return
        }
        var contactId = 0
        if (contactCheck.isChecked) {
            number = phone.text!!.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(number)) {
                phone.error = getString(R.string.you_dont_insert_number)
                return
            }
            if (!checkContactPermission(CONTACT_PERM)) {
                return
            }
            contactId = Contacts.getIdFromNumber(number, this)
        }
        var birthday = mBirthday
        if (birthday != null) {
            birthday.name = contact
            birthday.contactId = contactId
            birthday.date = birthDate.text.toString()
            birthday.number = number
            birthday.day = myDay
            birthday.month = myMonth
        } else {
            birthday = Birthday(contact, birthDate.text.toString().trim { it <= ' ' }, number, 0, contactId, myDay, myMonth)
        }
        viewModel.saveBirthday(birthday)
    }

    private fun closeScreen() {
        setResult(Activity.RESULT_OK)
        finish()
        sendBroadcast(Intent(this, PermanentBirthdayReceiver::class.java)
                .setAction(PermanentBirthdayReceiver.ACTION_SHOW))
    }

    private fun deleteItem() {
        if (mBirthday != null) {
            viewModel.deleteBirthday(mBirthday!!)
        }
    }

    private fun dateDialog() {
        TimeUtil.showDatePicker(this, prefs, myDateCallBack, myYear, myMonth, myDay)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == Activity.RESULT_OK) {
                val name = data!!.getStringExtra(Constants.SELECTED_CONTACT_NAME)
                number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
                if (birthName.text!!.toString().matches("".toRegex())) {
                    birthName.setText(name)
                }
                phone.setText(number)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SuperUtil.selectContact(this@AddBirthdayActivity, Constants.REQUEST_CODE_CONTACTS)
            }
            CONTACT_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveBirthday()
            }
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val CONTACT_PERM = 102
    }
}
