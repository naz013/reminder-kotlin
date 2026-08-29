package com.github.naz013.common.googleauth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.github.naz013.logging.Logger
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.security.SecureRandom

private const val TAG = "GoogleAuthorization"

/**
 * Two-phase replacement for the legacy `GoogleSignInOptions.requestScopes(...)` flow: Credential
 * Manager identifies the account (email), then the separate Authorization Client API grants the
 * requested OAuth scopes. Credential Manager alone only proves identity - it doesn't grant scope
 * consent, and `GoogleAccountCredential` (still used to talk to the Drive/Tasks APIs) needs that
 * consent to already exist on the device before it can mint access tokens.
 */
class GoogleAuthorizationState internal constructor(
  private val activity: Activity,
  private val serverClientId: String,
  private val credentialManager: CredentialManager,
  private val coroutineScope: CoroutineScope,
  private val onEmailResolved: (String) -> Unit,
  private val launchAuthorizationIntent: (IntentSenderRequest) -> Unit,
  private val onResult: (String) -> Unit,
  private val onFail: () -> Unit,
) {
  fun authorize(scopes: List<String>) {
    coroutineScope.launch {
      val email = resolveEmail()
      if (email == null) {
        onFail()
        return@launch
      }
      onEmailResolved(email)
      requestAuthorization(scopes, email)
    }
  }

  fun signOut(onComplete: () -> Unit) {
    coroutineScope.launch {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: ClearCredentialException) {
        Logger.w(TAG, "clearCredentialState failed, treating as signed out anyway: ${e.message}")
      }
      onComplete()
    }
  }

  private suspend fun resolveEmail(): String? {
    val option = GetSignInWithGoogleOption.Builder(serverClientId)
      .setNonce(generateNonce())
      .build()
    val request = GetCredentialRequest.Builder()
      .addCredentialOption(option)
      .build()
    return try {
      val credential = credentialManager.getCredential(request = request, context = activity).credential
      if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val idTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        emailFromIdToken(idTokenCredential.idToken)
      } else {
        Logger.e(TAG, "Unexpected credential type: ${credential.type}")
        null
      }
    } catch (e: GetCredentialException) {
      Logger.e(TAG, "Google sign-in failed: ${e.message}")
      null
    } catch (e: GoogleIdTokenParsingException) {
      Logger.e(TAG, "Failed to parse Google ID token: ${e.message}")
      null
    }
  }

  private fun requestAuthorization(scopes: List<String>, email: String) {
    val authorizationRequest = AuthorizationRequest.builder()
      .setRequestedScopes(scopes.map { Scope(it) })
      .build()
    Identity.getAuthorizationClient(activity)
      .authorize(authorizationRequest)
      .addOnSuccessListener { authorizationResult ->
        val pendingIntent = authorizationResult.pendingIntent
        if (authorizationResult.hasResolution() && pendingIntent != null) {
          launchAuthorizationIntent(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        } else {
          onResult(email)
        }
      }
      .addOnFailureListener {
        Logger.e(TAG, "Failed to authorize scopes: ${it.message}")
        onFail()
      }
  }
}

@Composable
fun rememberGoogleAuthorization(
  serverClientId: String,
  onResult: (email: String) -> Unit,
  onFail: () -> Unit = {},
): GoogleAuthorizationState {
  val context = LocalContext.current
  val activity = remember(context) {
    context.findActivity()
      ?: error("rememberGoogleAuthorization requires an Activity context")
  }
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val coroutineScope = rememberCoroutineScope()
  var pendingEmail by remember { mutableStateOf<String?>(null) }

  val authorizationLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult(),
  ) { result ->
    try {
      Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(result.data)
      val email = pendingEmail
      if (email != null) onResult(email) else onFail()
    } catch (e: ApiException) {
      Logger.e(TAG, "Authorization intent failed: ${e.message}")
      onFail()
    }
  }

  return remember(activity, serverClientId, credentialManager, coroutineScope, authorizationLauncher) {
    GoogleAuthorizationState(
      activity = activity,
      serverClientId = serverClientId,
      credentialManager = credentialManager,
      coroutineScope = coroutineScope,
      onEmailResolved = { pendingEmail = it },
      launchAuthorizationIntent = authorizationLauncher::launch,
      onResult = onResult,
      onFail = onFail,
    )
  }
}

private fun generateNonce(byteLength: Int = 32): String {
  val randomBytes = ByteArray(byteLength)
  SecureRandom().nextBytes(randomBytes)
  return Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
}

/**
 * `GoogleIdTokenCredential.id` isn't consistently documented as the account email, so the email
 * is read from the ID token's `email` JWT claim instead - the same claim the legacy
 * `GoogleSignInAccount.account.name` was always backed by, and what the auth managers'
 * `isAuthorized()` checks expect to find in storage (an email-shaped string).
 */
private fun emailFromIdToken(idToken: String): String? {
  val payload = idToken.split(".").getOrNull(1) ?: return null
  return try {
    val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    JSONObject(String(decoded, Charsets.UTF_8)).optString("email").takeIf { it.isNotBlank() }
  } catch (e: JSONException) {
    Logger.e(TAG, "Failed to parse ID token payload: ${e.message}")
    null
  } catch (e: IllegalArgumentException) {
    Logger.e(TAG, "Failed to base64-decode ID token payload: ${e.message}")
    null
  }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}
