package com.elementary.tasks.core.view_models.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.places.work.SingleBackupWorker
import kotlinx.coroutines.runBlocking

class PlaceViewModel private constructor(key: String) : BasePlacesViewModel() {

    var place = appDb.placesDao().loadByKey(key)
    var hasSameInDb: Boolean = false

    fun savePlace(place: Place) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                appDb.placesDao().insert(place)
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, place.id)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    fun findSame(id: String) {
        launchDefault {
            val place = appDb.placesDao().getByKey(id)
            hasSameInDb = place != null
        }
    }

    class Factory(private val key: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaceViewModel(key) as T
        }
    }
}
