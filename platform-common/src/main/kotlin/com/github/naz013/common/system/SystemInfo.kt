package com.github.naz013.common.system

import android.content.Context
import com.github.naz013.common.Module

class SystemInfo(
  private val context: Context,
) {
  val isTablet: Boolean
    get() = Module.isTablet(context)

  val isChromeOs: Boolean
    get() = Module.isChromeOs(context)

  val hasTelephony: Boolean
    get() = Module.hasTelephony(context)

  val hasLocation: Boolean
    get() = Module.hasLocation(context)

  val hasCamera: Boolean
    get() = Module.hasCamera(context)

  val hasMicrophone: Boolean
    get() = Module.hasMicrophone(context)

  companion object {
    const val FREE_PACKAGE_NAME = "com.cray.software.justreminder"
    const val PRO_PACKAGE_NAME = "com.cray.software.justreminderpro"
  }
}
