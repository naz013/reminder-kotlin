package com.github.naz013.ui.group

import org.koin.dsl.module

val uiGroupModule = module {
  factory { UiGroupListAdapter(get()) }
  factory { GroupsUtil(get(), get(), get()) }
}
