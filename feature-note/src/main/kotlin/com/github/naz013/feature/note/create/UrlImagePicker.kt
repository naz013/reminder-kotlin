package com.github.naz013.feature.note.create

import android.content.ClipboardManager
import android.content.Context
import android.util.Patterns
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.github.naz013.feature.note.R

@Stable
internal class UrlImagePickerState internal constructor() {
  internal var clipboardUrl: String? by mutableStateOf(null)
  internal var showUrlInput: Boolean by mutableStateOf(false)

  fun start(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val text = clipboard?.takeIf { it.hasPrimaryClip() }?.primaryClip?.getItemAt(0)?.text?.toString()
    if (text != null && Patterns.WEB_URL.matcher(text).matches()) {
      clipboardUrl = text
    } else {
      showUrlInput = true
    }
  }

  internal fun dismiss() {
    clipboardUrl = null
    showUrlInput = false
  }

  internal fun showManualInput() {
    clipboardUrl = null
    showUrlInput = true
  }
}

@Composable
internal fun rememberUrlImagePickerState(): UrlImagePickerState = remember { UrlImagePickerState() }

/** Renders whichever step of the URL flow is pending on [state]; no-op while neither is active. */
@Composable
internal fun UrlImagePickerDialogs(
  state: UrlImagePickerState,
  onUrlConfirmed: (String) -> Unit,
) {
  state.clipboardUrl?.let { url ->
    AlertDialog(
      onDismissRequest = state::dismiss,
      title = { Text(stringResource(R.string.from_url)) },
      text = { Text(url) },
      confirmButton = {
        TextButton(onClick = {
          state.dismiss()
          onUrlConfirmed(url)
        }) {
          Text(stringResource(R.string.download))
        }
      },
      dismissButton = {
        TextButton(onClick = state::showManualInput) { Text(stringResource(R.string.cancel)) }
      },
    )
  }

  if (state.showUrlInput) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = state::dismiss,
      title = { Text(stringResource(R.string.from_url)) },
      text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
      confirmButton = {
        TextButton(onClick = {
          state.dismiss()
          onUrlConfirmed(text.trim())
        }) {
          Text(stringResource(R.string.download))
        }
      },
      dismissButton = {
        TextButton(onClick = state::dismiss) { Text(stringResource(R.string.cancel)) }
      },
    )
  }
}
