package com.github.naz013.cloudapi.googletasks

import kotlin.random.Random

internal class GetRandomGoogleTaskListColorUseCase {
  operator fun invoke(): Int {
    return Random(System.currentTimeMillis()).nextInt(15)
  }
}
