package com.github.naz013.navigation

import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.navigation.intent.IntentDataWriter
import org.koin.dsl.module

val navigationApiModule = module {
  single { IntentDataHolder() }
  single { get<IntentDataHolder>() as IntentDataReader }
  single { get<IntentDataHolder>() as IntentDataWriter }
}
