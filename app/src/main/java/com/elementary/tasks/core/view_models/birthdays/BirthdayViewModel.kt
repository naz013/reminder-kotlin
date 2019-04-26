package com.elementary.tasks.core.view_models.birthdays

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BirthdayViewModel private constructor(id: String) : BaseBirthdaysViewModel() {

    val birthday = appDb.birthdaysDao().loadById(id)
    var date: MutableLiveData<Long> = MutableLiveData()
    var isContactAttached: MutableLiveData<Boolean> = MutableLiveData()

    var isLogged = false
    var isEdited = false

    class Factory(private val key: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BirthdayViewModel(key) as T
        }
    }
}
