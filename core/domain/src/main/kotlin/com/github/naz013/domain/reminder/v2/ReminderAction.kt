package com.github.naz013.domain.reminder.v2

sealed class ReminderAction {
  data object None : ReminderAction()

  data class Call(
    val target: String
  ) : ReminderAction()

  data class Sms(
    val target: String,
    val subject: String
  ) : ReminderAction()

  data class Link(
    val target: String
  ) : ReminderAction()

  data class App(
    val target: String
  ) : ReminderAction()

  data class Email(
    val target: String,
    val subject: String
  ) : ReminderAction()

  data object Shopping : ReminderAction()
}
