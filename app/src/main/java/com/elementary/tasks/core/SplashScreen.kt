package com.elementary.tasks.core

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.elementary.tasks.core.async.EnableThread
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.intro.IntroActivity
import com.elementary.tasks.navigation.MainActivity

class SplashScreen : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPrefs()
        if (Prefs.getInstance(this).isSbNotificationEnabled) {
            Notifier.updateReminderPermanent(this, PermanentReminderReceiver.ACTION_SHOW)
        }
    }

    private fun checkIfAppUpdated() {
        try {
            val info = packageManager.getPackageInfo(packageName, 0)
            if (!Prefs.getInstance(this).getVersion(info.versionName)) {
                Prefs.getInstance(this).saveVersionBoolean(info.versionName)
                EnableThread(this).start()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        gotoApp()
    }

    private fun gotoApp() {
        checkIfAppUpdated()
        if (!Prefs.getInstance(this).isUserLogged) {
            openIntroScreen()
        } else {
            initGroups()
            runApplication()
        }
    }

    private fun openIntroScreen() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    private fun initGroups() {
        if (AppDb.getAppDatabase(this).groupDao().all.isEmpty()) {
            GroupsUtil.initDefault(this)
        }
    }

    private fun initPrefs() {
        val prefs = Prefs.getInstance(this)
        prefs.initPrefs(this)
        prefs.checkPrefs()
    }

    private fun runApplication() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}