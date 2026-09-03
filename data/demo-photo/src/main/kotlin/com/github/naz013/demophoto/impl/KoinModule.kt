package com.github.naz013.demophoto.impl

import com.github.naz013.demophoto.DemoPhotoDownloader
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val PICSUM_BASE_URL = "https://picsum.photos/"

val demoPhotoModule = module {
  single<PicsumService> {
    Retrofit.Builder()
      .baseUrl(PICSUM_BASE_URL)
      .client(OkHttpClient.Builder().build())
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(PicsumService::class.java)
  }
  factoryOf(::DemoPhotoCache)
  factory<DemoPhotoDownloader> { DemoPhotoDownloaderImpl(get(), get()) }
}
