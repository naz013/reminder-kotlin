package com.elementary.tasks.core.birthdays

import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.logic.birthday.BirthdayNotifier

class AppBirthdayNotifier(
  private val notifier: Notifier,
) : BirthdayNotifier {
  override fun showBirthdayPermanent() {
    notifier.showBirthdayPermanent()
  }
}
