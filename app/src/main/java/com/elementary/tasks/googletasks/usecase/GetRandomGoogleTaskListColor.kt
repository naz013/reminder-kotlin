package com.elementary.tasks.googletasks.usecase

import kotlin.random.Random

class GetRandomGoogleTaskListColor {

  operator fun invoke(): Int {
    return Random(System.currentTimeMillis()).nextInt(15)
  }
}
