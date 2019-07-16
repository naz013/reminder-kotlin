package com.elementary.tasks.core.services

import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.work.LoadFileWorker
import com.elementary.tasks.core.work.LoadTokensWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

class SyncMessagingService : FirebaseMessagingService() {

    private val prefs: Prefs by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Timber.d("onMessageReceived: ${remoteMessage?.data}")
        if (prefs.multiDeviceModeEnabled && prefs.isBackupEnabled) {
            val data = remoteMessage?.data
            if (data != null) {
                val fileName = data["details"] ?: ""
                when (data["type"] ?: "") {
                    "tokens" -> LoadTokensWorker.schedule()
                    "file" -> LoadFileWorker.schedule(fileName)
                    else -> SyncDataWorker.schedule()
                }
            } else {
                SyncDataWorker.schedule()
            }
        }
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        val dropbox = Dropbox()
        if (dropbox.isLinked) {
            dropbox.updateToken(token)
        }
        GDrive.getInstance(applicationContext)?.updateToken(token)
    }
}