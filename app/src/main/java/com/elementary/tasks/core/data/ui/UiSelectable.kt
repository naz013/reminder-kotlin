package com.elementary.tasks.core.data.ui

data class UiSelectable<T>(
  val data: T,
  var isSelected: Boolean = false
)
