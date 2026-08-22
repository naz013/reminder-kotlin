package com.elementary.tasks.ads

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.elementary.tasks.AdsProvider
import com.github.naz013.common.system.BuildInfo
import org.koin.compose.koinInject

enum class AdBanner {
  ReminderPreview,
  NotePreview,
  GoogleTask,
  GoogleTaskList,
  Group,
  Tag,
  Birthday,
  Place,
  PinLogin,
  ActionScreen,
  Routine
}

@Composable
fun NormalAdBanner(
  modifier: Modifier = Modifier,
  adBanner: AdBanner,
) {
  val buildInfo = koinInject<BuildInfo>()
  if (buildInfo.isPro || !AdsProvider.hasAds()) return

  val adsProvider = remember { AdsProvider() }
  val context = LocalContext.current
  AndroidView(
    modifier = modifier,
    factory = { FrameLayout(context) },
    update = { viewGroup ->
      adsProvider.showBanner(viewGroup, adBanner)
    },
  )
}
