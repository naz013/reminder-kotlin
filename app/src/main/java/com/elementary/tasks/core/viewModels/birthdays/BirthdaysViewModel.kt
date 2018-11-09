package com.elementary.tasks.core.viewModels.birthdays

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import java.util.*

@Suppress("JoinDeclarationAndAssignment")
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
        birthdays = appDb.birthdaysDao().loadAll()
    }

    fun deleteAllBirthdays() {
        isInProgress.postValue(true)
        launchDefault {
            val list = appDb.birthdaysDao().all()
            val ids = ArrayList<String>()
            for (birthday in list) {
                appDb.birthdaysDao().delete(birthday)
                ids.add(birthday.uuId)
            }
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(Data.Builder().putStringArray(Constants.INTENT_IDS, ids.toTypedArray()).build())
                    .addTag("BD_WORK")
                    .build()
            WorkManager.getInstance().enqueue(work)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
        }
    }
}
