package com.github.naz013.navigation

import android.os.Bundle

sealed class Destination

data class ActivityDestination(
  val activityClass: ActivityClass,
  val flags: Int? = null,
  val extras: Bundle? = null,
  val isLoggedIn: Boolean = false,
  val action: String? = null
) : Destination()
