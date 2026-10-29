package com.github.naz013.ui.common.datetime

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.naz013.domain.BirthdayEventType
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.icon.DrawableCatalog

@StringRes
fun BirthdayEventType.labelRes(): Int =
  when (this) {
    BirthdayEventType.BIRTHDAY -> R.string.birthday_event_type_birthday
    BirthdayEventType.ANNIVERSARY -> R.string.birthday_event_type_anniversary
    BirthdayEventType.MEMORIAL -> R.string.birthday_event_type_memorial
    BirthdayEventType.OTHER -> R.string.birthday_event_type_other
  }

@DrawableRes
fun BirthdayEventType.iconRes(): Int =
  when (this) {
    BirthdayEventType.BIRTHDAY -> DrawableCatalog.Fluent.FoodCake
    BirthdayEventType.ANNIVERSARY -> DrawableCatalog.Fluent.Heart
    BirthdayEventType.MEMORIAL -> DrawableCatalog.Fluent.Candle
    BirthdayEventType.OTHER -> DrawableCatalog.Fluent.Star
  }
