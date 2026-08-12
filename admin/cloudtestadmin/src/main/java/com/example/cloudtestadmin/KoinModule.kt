package com.example.cloudtestadmin

import com.github.naz013.cloudapi.CloudKeysStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for the Cloud Test Admin application.
 *
 * Provides ViewModel dependencies.
 */
val cloudTestAdminModule = module {
  viewModelOf(::CloudTestViewModel)

  factory { CloudKeysStorageImpl(androidContext()) as CloudKeysStorage }
}
