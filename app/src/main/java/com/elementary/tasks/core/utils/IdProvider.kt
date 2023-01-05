package com.elementary.tasks.core.utils

import java.util.Random
import java.util.UUID

class IdProvider {

  fun generateUuid(): String {
    return UUID.randomUUID().toString()
  }

  fun generateId(): Int {
    return Random().nextInt(Integer.MAX_VALUE)
  }
}
