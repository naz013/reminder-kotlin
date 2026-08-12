package com.github.naz013.feature.note.create

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.ImageResult

internal class ImageLoader(
  private val context: Context,
) {
  private val internalImageLoader =
    ImageLoader
      .Builder(context)
      .crossfade(true)
      .build()

  suspend fun execute(imageRequest: ImageRequest): ImageResult = internalImageLoader.execute(imageRequest)
}
