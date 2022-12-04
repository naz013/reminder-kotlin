package com.elementary.tasks.core.utils

import kotlin.random.Random

object RuntimeRequestCode {

  private var requestCode = Random(250).nextInt(100, 500)

  fun currentCode(): Int {
    return requestCode
  }

  fun obtainNewCode(): Int {
    requestCode++
    return requestCode
  }
}