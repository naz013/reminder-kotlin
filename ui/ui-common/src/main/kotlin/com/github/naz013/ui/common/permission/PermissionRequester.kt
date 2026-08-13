package com.github.naz013.ui.common.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.naz013.common.Permissions
import com.github.naz013.common.system.Module
import com.github.naz013.ui.common.R

@Stable
class PermissionRequester internal constructor(
  private val activity: Activity,
) {
  internal var launchSystemPrompt: (String) -> Unit = {}

  var rationale by mutableStateOf<UiPermissionDialogData?>(null)
    private set

  private val queue = ArrayDeque<String>()
  private val granted = mutableListOf<String>()
  private var onGranted: ((List<String>) -> Unit)? = null
  private var onDenied: ((String) -> Unit)? = null

  fun request(
    permissions: List<String>,
    onGranted: (granted: List<String>) -> Unit,
    onDenied: (permission: String) -> Unit = {},
  ) {
    queue.clear()
    queue.addAll(permissions)
    granted.clear()
    this.onGranted = onGranted
    this.onDenied = onDenied
    processNext()
  }

  fun request(
    permission: String,
    onGranted: () -> Unit,
    onDenied: (permission: String) -> Unit = {},
  ) = request(listOf(permission), onGranted = { onGranted() }, onDenied = onDenied)

  fun onRationaleConfirmed() {
    val permission = rationale?.permission ?: return
    rationale = null
    launchSystemPrompt(permission)
  }

  fun onRationaleDismissed() {
    val permission = rationale?.permission
    rationale = null
    if (permission != null) onDenied?.invoke(permission)
  }

  internal fun onSystemResult(
    permission: String,
    isGranted: Boolean,
  ) {
    if (isGranted) {
      granted.add(permission)
      processNext()
    } else {
      onDenied?.invoke(permission)
    }
  }

  private fun processNext() {
    val permission = queue.removeFirstOrNull()
    if (permission == null) {
      onGranted?.invoke(granted.toList())
      return
    }
    if (isSkipped(permission) || isAlreadyGranted(permission)) {
      granted.add(permission)
      processNext()
      return
    }
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
      val data = rationaleFor(permission)
      if (data != null) {
        rationale = data
      } else {
        launchSystemPrompt(permission)
      }
    } else {
      launchSystemPrompt(permission)
    }
  }

  private fun isSkipped(permission: String): Boolean =
    when (permission) {
      Permissions.FOREGROUND_SERVICE_LOCATION -> !Module.is15
      Permissions.POST_NOTIFICATION -> !Module.is13
      Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL -> Module.is13
      else -> false
    }

  private fun isAlreadyGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

  private fun rationaleFor(permission: String): UiPermissionDialogData? =
    when (permission) {
      Permissions.READ_CONTACTS -> UiPermissionDialogData.READ_CONTACTS
      Permissions.GET_ACCOUNTS -> UiPermissionDialogData.GET_ACCOUNTS
      Permissions.CALL_PHONE -> UiPermissionDialogData.CALL_PHONE
      Permissions.READ_CALENDAR -> UiPermissionDialogData.READ_CALENDAR
      Permissions.WRITE_CALENDAR -> UiPermissionDialogData.WRITE_CALENDAR
      Permissions.READ_EXTERNAL -> UiPermissionDialogData.READ_EXTERNAL
      Permissions.WRITE_EXTERNAL -> UiPermissionDialogData.WRITE_EXTERNAL
      Permissions.ACCESS_FINE_LOCATION -> UiPermissionDialogData.FINE_LOCATION
      Permissions.ACCESS_COARSE_LOCATION -> UiPermissionDialogData.COARSE_LOCATION
      Permissions.RECORD_AUDIO -> UiPermissionDialogData.RECORD_AUDIO
      Permissions.BACKGROUND_LOCATION -> UiPermissionDialogData.BACKGROUND_LOCATION
      Permissions.FOREGROUND_SERVICE -> UiPermissionDialogData.FOREGROUND_SERVICE
      Permissions.FOREGROUND_SERVICE_LOCATION -> UiPermissionDialogData.FOREGROUND_SERVICE_LOCATION
      Permissions.POST_NOTIFICATION -> UiPermissionDialogData.POST_NOTIFICATION
      else -> null
    }
}

@Composable
fun rememberPermissionRequesterRationale(): PermissionRequester {
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() }
  requireNotNull(activity) { "rememberPermissionRequester must be called from an Activity-backed composition" }

  val requester = remember(activity) { PermissionRequester(activity) }
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
      if (results.entries.isEmpty()) {
        return@rememberLauncherForActivityResult
      }
      val [permission, isGranted] = results.entries.first()
      requester.onSystemResult(permission, isGranted)
    }
  requester.launchSystemPrompt = { permission -> launcher.launch(arrayOf(permission)) }
  PermissionRationaleDialog(requester)
  return requester
}

/** Renders [PermissionRequester.rationale] as an explanation dialog; no-op while there's none pending. */
@Composable
private fun PermissionRationaleDialog(state: PermissionRequester) {
  val data = state.rationale ?: return
  AlertDialog(
    onDismissRequest = state::onRationaleDismissed,
    title = { Text(stringResource(data.title)) },
    text = { Text(stringResource(data.description)) },
    confirmButton = {
      TextButton(onClick = state::onRationaleConfirmed) { Text(stringResource(R.string.ok)) }
    },
    dismissButton = {
      TextButton(onClick = state::onRationaleDismissed) { Text(stringResource(R.string.cancel)) }
    },
  )
}

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
