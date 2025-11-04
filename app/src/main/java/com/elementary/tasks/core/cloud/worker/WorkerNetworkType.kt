package com.elementary.tasks.core.cloud.worker

import androidx.work.NetworkType

enum class WorkerNetworkType(val type: NetworkType) {
  Any(NetworkType.CONNECTED),
  Wifi(NetworkType.UNMETERED),
  Cellular(NetworkType.METERED)
}
