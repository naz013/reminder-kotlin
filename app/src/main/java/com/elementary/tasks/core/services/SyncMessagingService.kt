package com.elementary.tasks.core.services

import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.work.SyncDataWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class SyncMessagingService : FirebaseMessagingService() {

    private val prefs: Prefs by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (prefs.multiDeviceModeEnabled && prefs.isBackupEnabled) {
            SyncDataWorker.schedule()
        }
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        GDrive.getInstance(applicationContext)?.updateToken(token)
    }
}