package com.elementary.tasks.core

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.EnableThread
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.experimental.NavUtil
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.login.LoginActivity
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import org.koin.android.ext.android.inject

class SplashScreenActivity : ThemedActivity() {

    private val db: AppDb by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPrefs()
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        }
    }

    private fun checkIfAppUpdated() {
        try {
            val info = packageManager.getPackageInfo(packageName, 0)
            if (!prefs.getVersion(info.versionName)) {
                prefs.saveVersionBoolean(info.versionName)
                EnableThread.run(this)
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
        if (!prefs.isUserLogged) {
            openIntroScreen()
        } else {
            launchDefault {
                if (db.reminderGroupDao().all().isEmpty()) {
                    GroupsUtil.initDefault(this@SplashScreenActivity)
                }
                withUIContext {
                    if (prefs.hasPinCode) {
                        openPinLogin()
                    } else {
                        runApplication()
                    }
                }
            }
        }
    }

    private fun openPinLogin() {
        startActivity(Intent(this, PinLoginActivity::class.java))
        finish()
    }

    private fun openIntroScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun initPrefs() {
        prefs.initPrefs(this)
        prefs.checkPrefs()
    }

    private fun runApplication() {
        startActivity(Intent(this, NavUtil.homeScreen(prefs)))
        finish()
    }
}