package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

interface DialogDispatcher {
  fun showDialog(
    iconRes: Int? = null,
    iconContentDescription: String? = null,
    titleRes: Int? = null,
    title: String? = null,
    textRes: Int? = null,
    text: String? = null,
    positiveButtonRes: Int? = null,
    positiveButton: String? = null,
    negativeButtonRes: Int? = null,
    negativeButton: String? = null,
    onNegative: () -> Unit = {},
    onPositive: () -> Unit = {},
  )
}

@Composable
fun rememberDialogDispatcher(): DialogDispatcher {
  val openAlertDialog = remember { mutableStateOf(false) }
  val dialogData = remember { mutableStateOf(DialogData()) }

  if (openAlertDialog.value) {
    Dialog(dialogData.value)
  }

  return object : DialogDispatcher {
    override fun showDialog(
      iconRes: Int?,
      iconContentDescription: String?,
      titleRes: Int?,
      title: String?,
      textRes: Int?,
      text: String?,
      positiveButtonRes: Int?,
      positiveButton: String?,
      negativeButtonRes: Int?,
      negativeButton: String?,
      onNegative: () -> Unit,
      onPositive: () -> Unit
    ) {
      dialogData.value = DialogData(
        iconRes = iconRes,
        iconContentDescription = iconContentDescription,
        titleRes = titleRes,
        title = title,
        textRes = textRes,
        text = text,
        positiveButtonRes = positiveButtonRes,
        positiveButton = positiveButton,
        negativeButtonRes = negativeButtonRes,
        negativeButton = negativeButton,
        onNegative = {
          onNegative()
          openAlertDialog.value = false
        },
        onPositive = {
          onPositive()
          openAlertDialog.value = false
        }
      )
      openAlertDialog.value = true
    }
  }
}

private data class DialogData(
  val iconRes: Int? = null,
  val iconContentDescription: String? = null,
  val titleRes: Int? = null,
  val title: String? = null,
  val textRes: Int? = null,
  val text: String? = null,
  val positiveButtonRes: Int? = null,
  val positiveButton: String? = null,
  val negativeButtonRes: Int? = null,
  val negativeButton: String? = null,
  val onNegative: () -> Unit = {},
  val onPositive: () -> Unit = {},
)

@Composable
private fun Dialog(data: DialogData) {
  AlertDialog(
    icon = {
      data.iconRes?.let {
        Icon(painterResource(it), contentDescription = data.iconContentDescription)
      }
    },
    title = {
      data.title?.let {
        Text(text = it)
      } ?: data.titleRes?.let {
        Text(text = stringResource(it))
      }
    },
    text = {
      data.text?.let {
        Text(text = it)
      } ?: data.textRes?.let {
        Text(text = stringResource(it))
      }
    },
    onDismissRequest = {
      data.onNegative()
    },
    confirmButton = {
      val positiveButton: @Composable (String) -> Unit = {
        TextButton(
          onClick = {
            data.onPositive()
          }
        ) {
          Text(it)
        }
      }
      data.positiveButton?.let {
        positiveButton(it)
      } ?: data.positiveButtonRes?.let {
        positiveButton(stringResource(it))
      }
    },
    dismissButton = {
      val negativeButton: @Composable (String) -> Unit = {
        TextButton(
          onClick = {
            data.onNegative()
          }
        ) {
          Text(it)
        }
      }
      data.negativeButton?.let {
        negativeButton(it)
      } ?: data.negativeButtonRes?.let {
        negativeButton(stringResource(it))
      }
    },
  )
}
