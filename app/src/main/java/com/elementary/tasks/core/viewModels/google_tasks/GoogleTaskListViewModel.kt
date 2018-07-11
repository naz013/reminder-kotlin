package com.elementary.tasks.core.viewModels.google_tasks

import android.app.Application

import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.viewModels.Commands

import java.io.IOException
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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
class GoogleTaskListViewModel(application: Application, listId: String?) : BaseTaskListsViewModel(application) {

    var googleTaskList: LiveData<GoogleTaskList>
    var defaultTaskList: LiveData<GoogleTaskList>
    var googleTasks: LiveData<List<GoogleTask>>

    init {
        googleTaskList = appDb!!.googleTaskListsDao().loadById(listId)
        defaultTaskList = appDb!!.googleTaskListsDao().loadDefault()
        if (listId == null) {
            googleTasks = appDb!!.googleTasksDao().loadAll()
        } else {
            googleTasks = appDb!!.googleTasksDao().loadAllByList(listId)
        }
    }

    fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = Google.getInstance(getApplication())
        if (google == null || google.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        run {
            google.tasks!!.insertTasksList(googleTaskList.title, googleTaskList.color)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
        }
    }

    fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = Google.getInstance(getApplication())
        if (google == null || google.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        run {
            appDb!!.googleTaskListsDao().insert(googleTaskList)
            try {
                google.tasks!!.updateTasksList(googleTaskList.title, googleTaskList.listId)
                end {
                    isInProgress.postValue(false)
                    result.postValue(Commands.SAVED)
                }
            } catch (e: IOException) {
                end {
                    isInProgress.postValue(false)
                    result.postValue(Commands.FAILED)
                }
            }
        }
    }

    fun saveLocalGoogleTaskList(googleTaskList: GoogleTaskList) {
        isInProgress.postValue(true)
        run {
            appDb!!.googleTaskListsDao().insert(googleTaskList)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
        }
    }

    class Factory(private val application: Application, private val id: String?) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleTaskListViewModel(application, id) as T
        }
    }
}
