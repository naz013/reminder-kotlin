package com.elementary.tasks.module.demodata

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.logic.demodata.DemoAssetProvider

class AppDemoAssetProvider(
  private val context: Context,
) : DemoAssetProvider {
  override fun demoNotePhotoBytes(): ByteArray =
    context.resources.openRawResource(R.raw.demo_note_photo).use { it.readBytes() }
}
