package com.github.naz013.ui.reminder

import org.koin.dsl.module

val uiReminderModule = module {
  factory { ShopItemsFormatter(get()) }
}
