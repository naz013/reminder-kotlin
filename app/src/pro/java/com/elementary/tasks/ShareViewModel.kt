package com.elementary.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.reminder.work.SingleBackupWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class ShareViewModel : ViewModel(), KoinComponent {

    private val qrShareProvider: QrShareProvider by inject()
    private val prefs: Prefs by inject()
    private val appDb: AppDb by inject()
    private val auth = FirebaseAuth.getInstance()

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _isLogged = MutableLiveData<Boolean>()
    val isLogged: LiveData<Boolean> = _isLogged
    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess
    private val _reminder = MutableLiveData<Reminder>()
    val reminder: LiveData<Reminder> = _reminder

    init {
        val currentUser = auth.currentUser
        _isLogged.postValue(currentUser != null)

        if (currentUser == null) {
            login()
        }
    }

    private fun login() {
        auth.signInAnonymously()
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        Timber.d("signInAnonymously:success")
                        val user = auth.currentUser
                        _isLogged.postValue(user != null)
                    } else {
                        Timber.d("signInAnonymously:failure -> ${task.exception}")
                        _isLogged.postValue(false)
                    }
                }
    }

    fun read(key: String, password: String) {
        _isLoading.postValue(true)
        qrShareProvider.readData(key.toUpperCase()) {
            if (it) {
                qrShareProvider.verifyData(password) { type, data ->
                    if (type != null && data != null) {
                        importData(type, data)
                    } else {
                        _isLoading.postValue(false)
                        _isError.postValue(true)
                    }
                }
            } else {
                _isLoading.postValue(false)
                _isError.postValue(true)
            }
        }
    }

    private fun importData(type: String, data: String) {
        Timber.d("importData: $type, $data")
        when (type) {
            QrShareProvider.TYPE_REMINDER -> {
                importReminder(data)
            }
            else -> {
                _isLoading.postValue(false)
            }
        }
    }

    private fun importReminder(data: String) {
        launchDefault {
            val json = QrShareProvider.readData(data)
            if (json != null) {
                val reminder = Gson().fromJson(json, Reminder::class.java)
                Timber.d("importReminder: $reminder")
                if (reminder != null && reminder.type != 0) {
                    if (prefs.isAutoImportSharedData) {
                        saveAndStartReminder(reminder)
                    } else {
                        _isLoading.postValue(false)
                        _reminder.postValue(reminder)
                    }
                } else {
                    _isLoading.postValue(false)
                    _isError.postValue(true)
                }
            } else {
                _isLoading.postValue(false)
                _isError.postValue(true)
            }
        }
    }

    private fun saveAndStartReminder(reminder: Reminder) {
        launchDefault {
            runBlocking {
                Timber.d("saveAndStartReminder: save START")
                val group = appDb.reminderGroupDao().defaultGroup()
                if (group != null) {
                    reminder.groupColor = group.groupColor
                    reminder.groupTitle = group.groupTitle
                    reminder.groupUuId = group.groupUuId
                }
                appDb.reminderDao().insert(reminder)
                if (reminder.isActive && !reminder.isRemoved) {
                    val control = EventControlFactory.getController(reminder)
                    if (control.canSkip()) {
                        control.next()
                    } else {
                        control.start()
                    }
                }
                Timber.d("saveAndStartReminder: save DONE")
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
            _isLoading.postValue(false)
            _isSuccess.postValue(true)
        }
    }

    private fun startWork(clazz: Class<out Worker>, key: String, valueTag: String) {
        if (prefs.isBackupEnabled) {
            val work = OneTimeWorkRequest.Builder(clazz)
                    .setInputData(Data.Builder().putString(key, valueTag).build())
                    .addTag(valueTag)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}