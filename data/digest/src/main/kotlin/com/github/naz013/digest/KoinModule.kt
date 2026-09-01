package com.github.naz013.digest

import com.github.naz013.digest.work.DailyDigestTask
import com.github.naz013.digestapi.DigestCapabilityChecker
import com.github.naz013.digestapi.DigestScheduler
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.qualifier.named
import org.koin.dsl.module

val digestModule = module {
  // single: holds the on-disk capability cache, no reason to rebuild it per-injection-site.
  single<DigestCapabilityChecker> { DigestCapabilityCheckerImpl(get()) }
  single<DigestScheduler> { DigestSchedulerImpl(get()) }

  factory { DigestContentBuilder(get(), get()) }
  factory { OnDeviceDigestSummarizer(get()) }
  factory { TemplateDigestSummarizer(get()) }
  factory { DigestSummarizerChain(get(), get()) }

  factory<BackgroundTask>(named(DailyDigestTask.TASK_KEY)) {
    DailyDigestTask(get(), get(), get(), get(), get())
  }
}
