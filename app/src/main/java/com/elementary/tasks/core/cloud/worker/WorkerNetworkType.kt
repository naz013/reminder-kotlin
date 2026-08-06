package com.elementary.tasks.core.cloud.worker

import com.github.naz013.workapi.NetworkRequirement

enum class WorkerNetworkType(
  val networkRequirement: NetworkRequirement,
) {
  Any(NetworkRequirement.CONNECTED),
  Wifi(NetworkRequirement.UNMETERED),
  Cellular(NetworkRequirement.METERED),
}

fun WorkerNetworkType.toNetworkRequirement(): NetworkRequirement = networkRequirement
