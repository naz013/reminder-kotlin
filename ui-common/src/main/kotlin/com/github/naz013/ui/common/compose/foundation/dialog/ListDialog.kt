package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * A dialog listing plain tap-to-trigger items (no persistent selection, no OK/Cancel) - the
 * Compose replacement for the legacy `MaterialAlertDialogBuilder.setItems(...)` pattern. For a
 * dialog with a fixed set of action buttons (confirm/cancel, or confirm/negative/neutral), use
 * [DialogDispatcher] instead.
 */
interface ListDialogDispatcher {
  fun showDialog(
    titleRes: Int? = null,
    title: String? = null,
    items: List<String>,
    onItemClick: (Int) -> Unit,
  )
}

@Composable
fun rememberListDialogDispatcher(): ListDialogDispatcher {
  val openDialog = remember { mutableStateOf(false) }
  val dialogData = remember { mutableStateOf(ListDialogData()) }

  if (openDialog.value) {
    ListDialog(dialogData.value, onDismissRequest = { openDialog.value = false })
  }

  return object : ListDialogDispatcher {
    override fun showDialog(
      titleRes: Int?,
      title: String?,
      items: List<String>,
      onItemClick: (Int) -> Unit,
    ) {
      dialogData.value = ListDialogData(
        titleRes = titleRes,
        title = title,
        items = items,
        onItemClick = {
          onItemClick(it)
          openDialog.value = false
        },
      )
      openDialog.value = true
    }
  }
}

private data class ListDialogData(
  val titleRes: Int? = null,
  val title: String? = null,
  val items: List<String> = emptyList(),
  val onItemClick: (Int) -> Unit = {},
)

@Composable
private fun ListDialog(
  data: ListDialogData,
  onDismissRequest: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      data.title?.let { Text(text = it) } ?: data.titleRes?.let { Text(text = stringResource(it)) }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
      ) {
        data.items.forEachIndexed { index, item ->
          TextButton(onClick = { data.onItemClick(index) }) {
            Text(text = item, modifier = Modifier.fillMaxWidth())
          }
        }
      }
    },
    confirmButton = {},
  )
}
