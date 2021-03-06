package com.elementary.tasks.core.services

import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.work.DeleteFileWorker
import com.elementary.tasks.core.work.LoadFileWorker
import com.elementary.tasks.core.work.LoadTokensWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.get
import timber.log.Timber

class SyncMessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Timber.d("onMessageReceived: ${remoteMessage.data}")
    val prefs = get<Prefs>()
    if (prefs.multiDeviceModeEnabled && prefs.isBackupEnabled) {
      val data = remoteMessage.data
      val fileName = data["details"] ?: ""
      when (data["type"] ?: "") {
        "tokens" -> LoadTokensWorker.schedule(applicationContext)
        "file" -> LoadFileWorker.schedule(applicationContext, fileName)
        "delete" -> DeleteFileWorker.schedule(applicationContext, fileName)
        else -> SyncDataWorker.schedule(applicationContext)
      }
    }
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    get<Dropbox>()
      .takeIf { it.isLinked }
      ?.also { it.updateToken(token) }

    get<GDrive>()
      .takeIf { it.isLogged }
      ?.also { it.updateToken(token) }
  }
}