package com.elementary.tasks.globalsearch

sealed class NavigationAction

data class ActivityNavigation(
  val clazz: Class<*>,
  val objectId: String
) : NavigationAction()

data class FragmentNavigation(
  val id: Int,
  val objectId: String
) : NavigationAction()
