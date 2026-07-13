package com.elementary.tasks.core.utils

import com.elementary.tasks.core.platform.BuildInfoImpl
import com.elementary.tasks.core.uicommon.FontApiImpl
import com.elementary.tasks.core.utils.io.UriHelper
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.ui.common.font.FontApi
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val newUtilsModule = module {
  singleOf(::ImageLoader)
  factoryOf(::UriHelper)
  factoryOf(::GeocoderTask)
  factoryOf<BuildInfo>(::BuildInfoImpl)

  factory { FontApiImpl(get()) as FontApi }
}
