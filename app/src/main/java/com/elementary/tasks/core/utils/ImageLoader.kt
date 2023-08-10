package com.elementary.tasks.core.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import java.io.File

class ImageLoader(
  private val context: Context
) {

  private val internalImageLoader = ImageLoader.Builder(context)
    .crossfade(true)
    .build()

  suspend fun execute(imageRequest: ImageRequest): ImageResult {
    return internalImageLoader.execute(imageRequest)
  }

  fun loadFromFile(
    file: File,
    onSuccess: (Drawable) -> Unit = { },
    onFail: (Drawable?) -> Unit = { },
    onStart: (Drawable?) -> Unit = { }
  ) {
    internalImageLoader.enqueue(buildRequest(file, onSuccess, onFail, onStart))
  }

  fun loadFromUri(
    uri: Uri,
    onSuccess: (Drawable) -> Unit = { },
    onFail: (Drawable?) -> Unit = { },
    onStart: (Drawable?) -> Unit = { }
  ) {
    internalImageLoader.enqueue(buildRequest(uri, onSuccess, onFail, onStart))
  }

  private fun buildRequest(
    any: Any,
    onSuccess: (Drawable) -> Unit,
    onFail: (Drawable?) -> Unit,
    onStart: (Drawable?) -> Unit
  ): ImageRequest {
    return ImageRequest.Builder(context)
      .data(any)
      .target(
        onError = onFail,
        onSuccess = onSuccess,
        onStart = onStart
      )
      .build()
  }
}
