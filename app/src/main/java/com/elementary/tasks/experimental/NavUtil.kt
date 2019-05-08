package com.elementary.tasks.experimental

import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.navigation.MainActivity

object NavUtil {

    fun homeScreen(prefs: Prefs): Class<*> {
        return if (prefs.useNewNav) {
            BottomNavActivity::class.java
        } else {
            MainActivity::class.java
        }
    }
}