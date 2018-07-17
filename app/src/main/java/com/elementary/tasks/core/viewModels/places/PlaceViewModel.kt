package com.elementary.tasks.core.viewModels.places

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

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
class PlaceViewModel private constructor(application: Application, key: String) : BasePlacesViewModel(application) {

    var place: LiveData<Place>

    init {
        place = appDb.placesDao().loadByKey(key)
    }

    fun savePlace(place: Place) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.placesDao().insert(place)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
        }
    }

    class Factory(private val application: Application, private val key: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaceViewModel(application, key) as T
        }
    }
}
