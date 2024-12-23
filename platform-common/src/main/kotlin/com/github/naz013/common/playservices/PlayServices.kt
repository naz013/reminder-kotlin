package com.github.naz013.common.playservices

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object PlayServices {
  fun isGooglePlayServicesAvailable(a: Context): Boolean {
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(a)
    return resultCode == ConnectionResult.SUCCESS
  }
}
