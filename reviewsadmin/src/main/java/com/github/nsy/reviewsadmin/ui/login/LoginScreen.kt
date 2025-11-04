package com.github.nsy.reviewsadmin.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.nsy.reviewsadmin.R
import org.koin.androidx.compose.koinViewModel

/**
 * Login screen that allows users to sign in with Google.
 *
 * This screen is displayed before the dashboard and requires successful
 * Firebase authentication before proceeding.
 */
@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  viewModel: LoginViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState(LoginUiState.Idle)

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      when (val state = uiState) {
        is LoginUiState.Idle -> {
          Button(
            onClick = { viewModel.signInWithGoogle() },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
          ) {
            Text(text = stringResource(R.string.sign_in_with_google))
          }
        }
        is LoginUiState.Loading -> {
          CircularProgressIndicator()
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.signing_in),
            style = MaterialTheme.typography.bodyMedium
          )
        }
        is LoginUiState.Success -> {
          onLoginSuccess()
        }
        is LoginUiState.Error -> {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = state.message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { viewModel.signInWithGoogle() },
              modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
            ) {
              Text(text = stringResource(R.string.retry))
            }
          }
        }
      }
    }
  }
}

