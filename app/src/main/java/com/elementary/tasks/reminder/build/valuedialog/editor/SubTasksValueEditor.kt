package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.notes.ObserveNonNull
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.shopitems.SubTasksViewModel
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.ShopItem

private val LIST_MAX_HEIGHT = 400.dp

/**
 * Editable shopping/checklist grid: type to add text, Enter/Done adds the next row and focuses
 * it, backspace on an empty row deletes it and refocuses the previous one. Checking an item never
 * reorders the list - toggling it in place is what keeps re-checking items predictable while
 * ticking several off. Replaces `SubTasksController`.
 *
 * All of the position/focus bookkeeping is delegated unchanged to [SubTasksViewModel] (a plain,
 * View-framework-agnostic class already used by the legacy controller) - this editor only owns
 * the row rendering and per-row focus requesting.
 */
@Composable
fun SubTasksValueEditor(
  builderItem: BuilderItem<List<ShopItem>>,
  dateTimeManager: DateTimeManager,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val viewModel = remember(builderItem) { SubTasksViewModel(dateTimeManager) }

  LaunchedEffect(builderItem) {
    viewModel.initWithData(builderItem.modifier.getValue() ?: emptyList())
  }

  val items by viewModel.showItems.observeAsState(emptyList())

  viewModel.saveItems.ObserveNonNull { saved ->
    builderItem.modifier.update(saved)
    onValueChange(builderItem)
  }

  LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT)) {
    itemsIndexed(items, key = { _, item -> item.uuId }) { index, item ->
      ShopItemRow(
        item = item,
        onCheckClick = { viewModel.onCheckPressed(index) },
        onTextChange = { text -> viewModel.onTextChanged(index, text) },
        onEnterPressed = { viewModel.onEnterPressed(index) },
        onDeletePressed = { viewModel.onDeletePressed(index) },
        onRemoveClick = { viewModel.onRemovePressed(index) },
      )
    }
  }
}

@Composable
private fun ShopItemRow(
  item: ShopItem,
  onCheckClick: () -> Unit,
  onTextChange: (String) -> Unit,
  onEnterPressed: () -> Unit,
  onDeletePressed: () -> Unit,
  onRemoveClick: () -> Unit,
) {
  var text by remember(item.uuId) { mutableStateOf(item.summary) }
  var isFocused by remember { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(item.showInput) {
    if (item.showInput) {
      focusRequester.requestFocus()
      keyboardController?.show()
    }
  }

  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = onCheckClick, modifier = Modifier.size(40.dp)) {
      Icon(
        painter = painterResource(
          if (item.isChecked) R.drawable.ic_fluent_checkbox_checked else R.drawable.ic_fluent_checkbox_unchecked,
        ),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
      )
    }
    Box(modifier = Modifier.weight(1f)) {
      if (text.isEmpty()) {
        Text(
          text = stringResource(R.string.builder_write_something),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 8.dp),
        )
      }
      BasicTextField(
        value = text,
        onValueChange = {
          text = it
          onTextChange(it)
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
          .focusRequester(focusRequester)
          .onFocusChanged { isFocused = it.isFocused }
          .onPreviewKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown &&
              keyEvent.key == Key.Backspace &&
              text.isEmpty()
            ) {
              onDeletePressed()
              true
            } else {
              false
            }
          },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { if (text.isNotEmpty()) onEnterPressed() }),
      )
    }
    if (isFocused && text.isNotEmpty()) {
      IconButton(onClick = onRemoveClick, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}
