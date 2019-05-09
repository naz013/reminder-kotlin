package com.elementary.tasks.experimental

import com.elementary.tasks.core.utils.Prefs

object NavUtil {

    fun homeScreen(prefs: Prefs): Class<*> {
        return BottomNavActivity::class.java
    }
}