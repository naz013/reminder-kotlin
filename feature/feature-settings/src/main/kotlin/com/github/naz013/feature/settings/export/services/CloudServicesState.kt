package com.github.naz013.feature.settings.export.services

data class CloudServicesState(
  val isLoading: Boolean = false,
  val isDropboxVisible: Boolean = false,
  val isDropboxLoggedIn: Boolean = false,
  val isGoogleDriveVisible: Boolean = false,
  val isGoogleDriveLoggedIn: Boolean = false,
  val isGoogleTasksVisible: Boolean = false,
  val isGoogleTasksLoggedIn: Boolean = false,
)
