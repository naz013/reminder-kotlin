package com.github.naz013.demophoto.impl

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/** Picsum Lorem Picsum (picsum.photos) serves the public Unsplash photo library without
 * requiring an API key - unlike Unsplash's own API, which needs a registered access key and
 * whose website blocks plain HTTP scraping behind a bot-detection challenge. */
internal interface PicsumService {
  @GET("v2/list")
  suspend fun list(
    @Query("page") page: Int,
    @Query("limit") limit: Int,
  ): List<PicsumPhotoDto>

  @Streaming
  @GET("id/{id}/{width}/{height}")
  suspend fun downloadPhoto(
    @Path("id") id: String,
    @Path("width") width: Int,
    @Path("height") height: Int,
  ): ResponseBody
}

internal data class PicsumPhotoDto(
  val id: String,
  val author: String,
  val url: String,
)
