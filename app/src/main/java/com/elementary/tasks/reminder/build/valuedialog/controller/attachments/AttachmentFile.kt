package com.elementary.tasks.reminder.build.valuedialog.controller.attachments

import android.net.Uri
import androidx.annotation.DrawableRes

data class AttachmentFile(
  val uri: Uri,
  val name: String,
  @DrawableRes
  val icon: Int,
  val type: AttachmentType
)

enum class AttachmentType {
  IMAGE,
  VIDEO,
  AUDIO,
  GIF,
  FILE
}
