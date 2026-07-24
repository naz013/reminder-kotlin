package com.elementary.tasks.core.utils

object PhoneNumberValidator {

  fun isPhoneNumber(target: String): Boolean {
    val phonePattern = "^[+]?[0-9 ()-]{3,25}\$".toRegex()
    return phonePattern.matches(target)
  }
}
