package com.github.naz013.ui.map

interface MapPreferences {
  var mapType: Int
  var mapStyle: Int
  var radius: Int
  var markerStyle: Int
  val hapticsEnabled: Boolean
  val useMetric: Boolean
}
