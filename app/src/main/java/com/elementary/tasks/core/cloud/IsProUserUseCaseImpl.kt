package com.elementary.tasks.core.cloud

import com.github.naz013.platform.BuildInfo
import com.github.naz013.sync.IsProUserUseCase

class IsProUserUseCaseImpl(
  private val buildInfo: BuildInfo,
) : IsProUserUseCase {
  override fun invoke(): Boolean = buildInfo.isPro
}
