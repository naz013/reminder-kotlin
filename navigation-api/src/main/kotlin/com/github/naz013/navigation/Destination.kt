package com.github.naz013.navigation

import android.os.Bundle

sealed class Destination

data class ActivityDestination(
  val screen: DestinationScreen,
  val flags: Int? = null,
  val extras: Bundle? = null,
  val isLoggedIn: Boolean = false,
  val action: String? = null
) : Destination()

data class DataDestination(
  val data: Any
) : Destination()
