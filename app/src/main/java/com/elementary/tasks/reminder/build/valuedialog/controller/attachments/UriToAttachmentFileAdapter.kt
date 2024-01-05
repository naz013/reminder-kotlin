package com.elementary.tasks.reminder.build.valuedialog.controller.attachments

import android.net.Uri
import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.utils.io.UriHelper

class UriToAttachmentFileAdapter(
  private val uriHelper: UriHelper
) {

  operator fun invoke(uri: Uri): AttachmentFile {
    Traces.d("UriToAttachmentFileAdapter", "invoke: $uri")
    val type = getAttachmentType(uriHelper.getMimeType(uri) ?: "")
    val fileName = try {
      uriHelper.getFileName(uri) ?: "NA"
    } catch (e: Throwable) {
      Traces.d("UriToAttachmentFileAdapter", "get fileName: $e")
      uri.toString()
    }
    Traces.d("UriToAttachmentFileAdapter", "fileName: $fileName")
    return AttachmentFile(
      uri = uri,
      name = fileName,
      icon = getIcon(type),
      type = type
    )
  }

  private fun getAttachmentType(type: String): AttachmentType {
    Traces.d("UriToAttachmentFileAdapter", "getAttachmentType: $type")
    return when {
      type.contains("gif") -> AttachmentType.GIF
      type.contains("image") -> AttachmentType.IMAGE
      type.contains("video") -> AttachmentType.VIDEO
      type.contains("audio") -> AttachmentType.AUDIO
      else -> AttachmentType.FILE
    }
  }

  @DrawableRes
  private fun getIcon(type: AttachmentType): Int {
    return when (type) {
      AttachmentType.IMAGE -> R.drawable.ic_fluent_image
      AttachmentType.VIDEO -> R.drawable.ic_fluent_movies_and_tv
      AttachmentType.AUDIO -> R.drawable.ic_builder_melody
      AttachmentType.GIF -> R.drawable.ic_fluent_gif
      else -> R.drawable.ic_fluent_document
    }
  }
}
