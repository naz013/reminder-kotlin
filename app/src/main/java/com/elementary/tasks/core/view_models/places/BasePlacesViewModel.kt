package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.places.work.DeleteBackupWorker

abstract class BasePlacesViewModel : BaseDbViewModel() {

    fun deletePlace(place: Place) {
        postInProgress(true)
        launchDefault {
            appDb.placesDao().delete(place)
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, place.id)
            postInProgress(false)
            postCommand(Commands.DELETED)
        }
    }
}
