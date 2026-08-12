package com.example.cloudtestadmin

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.ComposeActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

/**
 * Main activity for the Cloud Test Admin application.
 *
 * Displays the cloud service selection and file management screens.
 */
class MainActivity : ComposeActivity() {

  private val dropboxAuthManager: DropboxAuthManager by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.d(TAG, "onCreate: Checking Dropbox auth")
    // Check if returning from Dropbox authentication
    dropboxAuthManager.onAuthFinished()
  }

  override fun onResume() {
    super.onResume()
    Logger.d(TAG, "onResume: Dropbox authorized: ${dropboxAuthManager.isAuthorized()}")
  }

  @Composable
  override fun ActivityContent() {
    // Get ViewModel at Compose level so it's shared properly
    val viewModel: CloudTestViewModel = koinViewModel()

    // Handle Dropbox authentication callback only once when authorized
    androidx.compose.runtime.LaunchedEffect(dropboxAuthManager.isAuthorized()) {
      if (dropboxAuthManager.isAuthorized()) {
        Logger.d(TAG, "Dropbox is authorized, calling onAuthenticationComplete")
        viewModel.onAuthenticationComplete()
      }
    }

    AppTheme {
      CloudTestScreen(viewModel = viewModel)
    }
  }

  companion object {
    private const val TAG = "MainActivity"
  }
}
