package com.github.naz013.feature.settings.location

interface LocationSettingsPreferences {
  var isDistanceNotificationEnabled: Boolean
  var radius: Int
  var mapType: Int
  var mapStyle: Int
  var markerStyle: Int
  var trackTime: Int
  val useMetric: Boolean
  val hapticsEnabled: Boolean
}
