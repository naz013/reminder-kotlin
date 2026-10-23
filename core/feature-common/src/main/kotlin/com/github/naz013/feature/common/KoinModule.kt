package com.github.naz013.feature.common

import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinatorImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val featureCommonModule = module {
  factory { DispatcherProvider() }

  factory { SystemServiceProvider(get()) }

  single<PendingDeleteCoordinator> {
    val io = get<DispatcherProvider>().io()
    PendingDeleteCoordinatorImpl(CoroutineScope(SupervisorJob() + io), io)
  }
}
