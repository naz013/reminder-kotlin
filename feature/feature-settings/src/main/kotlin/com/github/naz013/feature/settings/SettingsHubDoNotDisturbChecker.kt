package com.github.naz013.feature.settings

interface SettingsHubDoNotDisturbChecker {
  fun isActive(): Boolean
  fun addChangeObserver(observer: () -> Unit)
  fun removeChangeObserver(observer: () -> Unit)
}
