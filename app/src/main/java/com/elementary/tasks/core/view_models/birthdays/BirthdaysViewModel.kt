package com.elementary.tasks.core.view_models.birthdays

import android.app.Application

import com.elementary.tasks.birthdays.work.DeleteBirthdayFilesAsync
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.view_models.Commands

import java.util.ArrayList

import androidx.lifecycle.LiveData

/**
 * Copyright 2018 Nazar Suhovich
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
class BirthdaysViewModel(application: Application) : BaseBirthdaysViewModel(application) {

    var birthdays: LiveData<List<Birthday>>

    init {
        birthdays = appDb!!.birthdaysDao().loadAll()
    }

    fun deleteAllBirthdays() {
        isInProgress.postValue(true)
        run {
            val list = appDb!!.birthdaysDao().all
            val ids = ArrayList<String>()
            for (birthday in list) {
                appDb!!.birthdaysDao().delete(birthday)
                ids.add(birthday.uuId)
            }
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            DeleteBirthdayFilesAsync(getApplication()).execute(*ids.toTypedArray())
        }
    }
}
