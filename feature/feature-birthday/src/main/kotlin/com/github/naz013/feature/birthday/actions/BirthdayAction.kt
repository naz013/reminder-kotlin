package com.github.naz013.feature.birthday.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.naz013.ui.common.R

enum class BirthdayAction(
  @param:StringRes val titleRes: Int,
  @param:DrawableRes val iconRes: Int,
  val category: BirthdayActionCategory,
) {
  Ok(R.string.ok, R.drawable.ic_fluent_checkmark, BirthdayActionCategory.Main),
  Snooze(R.string.action_snooze, R.drawable.ic_fluent_alert_snooze, BirthdayActionCategory.Main),
  SnoozeCustom(R.string.action_snooze_custom, R.drawable.ic_fluent_snooze, BirthdayActionCategory.Secondary),
  Edit(R.string.action_edit, R.drawable.ic_fluent_edit, BirthdayActionCategory.Secondary),

  MakeCall(R.string.make_call, R.drawable.ic_fluent_phone, BirthdayActionCategory.Action),
  SendSms(R.string.send_sms, R.drawable.ic_fluent_send, BirthdayActionCategory.Action),

  Delete(R.string.action_delete, R.drawable.ic_fluent_delete, BirthdayActionCategory.Secondary),
}

enum class BirthdayActionCategory {
  Main,
  Secondary,
  Action,
}
