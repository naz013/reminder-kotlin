package com.elementary.tasks.navigation.settings.export.backups

import com.elementary.tasks.navigation.settings.export.BackupsFragment

data class UserItem(
  var name: String = "",
  var photo: String = "",
  var quota: Long = 0,
  var used: Long = 0,
  var count: Int = 0,
  var kind: BackupsFragment.Info? = null
)
