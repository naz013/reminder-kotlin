package com.github.nsy.reviewsadmin.di

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.nsy.reviewsadmin.cache.LogFileCache
import com.github.nsy.reviewsadmin.cache.ReviewIdCache
import com.github.nsy.reviewsadmin.ui.dashboard.DashboardViewModel
import com.github.nsy.reviewsadmin.ui.login.LoginViewModel
import com.github.nsy.reviewsadmin.ui.logviewer.LogViewerViewModel
import com.github.nsy.reviewsadmin.ui.reviewdetail.ReviewDetailViewModel
import com.github.nsy.reviewsadmin.ui.reviewlist.ReviewListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module for the reviewsadmin app.
 *
 * Provides ViewModels and dependencies.
 */
val reviewsAdminModule = module {

  // Dispatcher Provider
  single { DispatcherProvider() }

  // Log File Cache (100 MB)
  single { LogFileCache(androidContext(), maxCacheSizeBytes = 100 * 1024 * 1024) }

  // Review ID Cache
  single { ReviewIdCache(androidContext()) }

  // ViewModels
  viewModel { LoginViewModel(get(), get()) }
  viewModel { DashboardViewModel(get(), get(), get()) }
  viewModel { ReviewListViewModel(get(), get()) }
  viewModel { ReviewDetailViewModel(get()) }
  viewModel { LogViewerViewModel(get(), get()) }
}
