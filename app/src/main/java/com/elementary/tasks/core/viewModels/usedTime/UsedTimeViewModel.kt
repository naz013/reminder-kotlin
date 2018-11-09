package com.elementary.tasks.core.viewModels.usedTime

import android.app.Application
import androidx.lifecycle.LiveData
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.viewModels.BaseDbViewModel

class UsedTimeViewModel(application: Application) : BaseDbViewModel(application) {

    var usedTimeList: LiveData<List<UsedTime>>

    init {
        usedTimeList = appDb.usedTimeDao().loadFirst5()
    }

    fun saveTime(after: Long) {
        launchDefault {
            var old = appDb.usedTimeDao().getByTimeMills(after)
            if (old != null) {
                old.useCount = old.useCount + 1
            } else {
                old = UsedTime(0, TimeUtil.generateViewAfterString(after), after, 1)
            }
            appDb.usedTimeDao().insert(old)
        }
    }
}