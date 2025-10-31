package com.github.naz013.reviews

import com.github.naz013.logging.Logger
import com.github.naz013.reviews.auth.FirebaseAuthManager
import com.github.naz013.reviews.db.FirestoreDatabase
import com.github.naz013.reviews.db.ReviewRepositoryImpl
import com.github.naz013.reviews.fileupload.LogFileUploader
import com.github.naz013.reviews.firebase.SecondaryFirebaseAppManager
import com.github.naz013.reviews.form.ReviewDialogViewModel
import com.github.naz013.reviews.logs.FindLatestLogsFileUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reviewsKoinModule = module {
  factory { ReviewsApiImpl(get()) as ReviewsApi }
  factory { ReviewRepositoryImpl(get(), get()) }

  // Firebase instances from the reviews Firebase app
  factory {
    try {
      val firestore = SecondaryFirebaseAppManager.getFirestore()
      if (firestore != null) {
        Logger.d("reviewsKoinModule", "Firestore instance obtained successfully")
        FirestoreDatabase(firestore)
      } else {
        Logger.e("reviewsKoinModule", "Firestore not initialized - ReviewSdk.initialize() must be called first")
        throw IllegalStateException("Firebase not initialized. Call ReviewSdk.initialize() in Application.onCreate()")
      }
    } catch (e: Exception) {
      Logger.e("reviewsKoinModule", "Error creating FirestoreDatabase: ${e.message}")
      throw e
    }
  }

  single {
    val auth = SecondaryFirebaseAppManager.getAuth()
    if (auth != null) {
      FirebaseAuthManager(auth)
    } else {
      Logger.e("reviewsKoinModule", "Auth not initialized - ReviewSdk.initialize() must be called first")
      throw IllegalStateException("Firebase not initialized. Call ReviewSdk.initialize() in Application.onCreate()")
    }
  }

  factory {
    val storage = SecondaryFirebaseAppManager.getStorage()
    if (storage != null) {
      LogFileUploader(storage)
    } else {
      Logger.e("reviewsKoinModule", "Storage not initialized - ReviewSdk.initialize() must be called first")
      throw IllegalStateException("Firebase not initialized. Call ReviewSdk.initialize() in Application.onCreate()")
    }
  }

  factory { FindLatestLogsFileUseCase(get()) }

  viewModel { ReviewDialogViewModel(get(), get(), get(), get(), get()) }
}
