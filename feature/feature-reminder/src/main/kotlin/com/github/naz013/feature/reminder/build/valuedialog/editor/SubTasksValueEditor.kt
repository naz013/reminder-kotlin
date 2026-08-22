package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.valuedialog.controller.shopitems.GroupedShopItems
import com.github.naz013.feature.reminder.build.valuedialog.controller.shopitems.SubTasksViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.livedata.ObserveNonNull

private val LIST_MAX_HEIGHT = 400.dp
private val ROW_HEIGHT = 40.dp
private const val CHECK_ANIMATION_MS = 150

/** Semantics test tag for a shopping-list row's checkbox, parameterized by [itemId] (`ShopItem
 *  .uuId`) since a list can have several rows and the checkbox itself carries no text/content
 *  description (see [ShopItemRow]). Exposed so instrumented tests can toggle a specific row via
 *  `onNodeWithTag(shopItemCheckTestTag(item.uuId))`. */
fun shopItemCheckTestTag(itemId: String): String = "shop_item_check_$itemId"

/** Same as [shopItemCheckTestTag] but for a row's remove button - only composed once that row is
 *  focused with non-empty text (see [ShopItemRow]). */
fun shopItemRemoveTestTag(itemId: String): String = "shop_item_remove_$itemId"

/**
 * Editable shopping/checklist grid: type to add text, Enter/Done adds the next row and focuses
 * it, backspace on an empty row deletes it and refocuses the previous one. Checking an item never
 * reorders the list - instead it sinks into a collapsed "Completed" section, mirroring how Keep/
 * Reminders keep the active list short. Active rows can be dragged (via a leading handle) to
 * reorder; completed rows can't. Replaces `SubTasksController`.
 *
 * All of the position/focus/grouping bookkeeping is delegated to [SubTasksViewModel] (a plain,
 * View-framework-agnostic class) - this editor only owns row rendering, per-row focus requesting,
 * and the drag gesture itself (which needs live access to Compose state, so it can't live in the
 * plain ViewModel).
 */
@Composable
internal fun SubTasksValueEditor(
  builderItem: BuilderItem<List<ShopItem>>,
  dateTimeManager: DateTimeManager,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val viewModel = remember(builderItem) { SubTasksViewModel(dateTimeManager) }

  LaunchedEffect(builderItem) {
    viewModel.initWithData(builderItem.modifier.getValue() ?: emptyList())
  }

  viewModel.saveItems.ObserveNonNull { saved ->
    builderItem.modifier.update(saved)
    onValueChange(builderItem)
  }

  val grouped by viewModel.groupedItems.observeAsState(GroupedShopItems(emptyList(), emptyList(), false))

  // Read from the drag gesture (a long-lived suspend callback that must never see a stale list),
  // not from a plain captured value - see the kdoc on the drag handle below.
  val latestActive = rememberUpdatedState(grouped.active)
  var draggedItemId by remember { mutableStateOf<String?>(null) }
  var dragOffset by remember { mutableFloatStateOf(0f) }
  val rowHeightPx = with(LocalDensity.current) { ROW_HEIGHT.toPx() }

  LazyColumn(modifier = Modifier
    .fillMaxWidth()
    .heightIn(max = LIST_MAX_HEIGHT)) {
    items(grouped.active, key = { it.value.uuId }) { indexed ->
      val itemId = indexed.value.uuId
      ShopItemRow(
        item = indexed.value,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        onCheckClick = { viewModel.onCheckPressed(indexed.index) },
        onTextChange = { text -> viewModel.onTextChanged(indexed.index, text) },
        onEnterPressed = { viewModel.onEnterPressed(indexed.index) },
        onDeletePressed = { viewModel.onDeletePressed(indexed.index) },
        onRemoveClick = { viewModel.onRemovePressed(indexed.index) },
        modifier = Modifier
          .animateItem()
          .graphicsLayer { translationY = if (draggedItemId == itemId) dragOffset else 0f },
        // Scoped to the handle icon, not the whole row - the row also hosts a BasicTextField
        // that needs undisturbed tap-to-place-cursor behavior.
        dragHandleModifier = Modifier.pointerInput(itemId) {
          detectDragGesturesAfterLongPress(
            onDragStart = {
              draggedItemId = itemId
              dragOffset = 0f
            },
            onDragEnd = {
              draggedItemId = null
              dragOffset = 0f
            },
            onDragCancel = {
              draggedItemId = null
              dragOffset = 0f
            },
            onDrag = { change, dragAmount ->
              change.consume()
              dragOffset += dragAmount.y
              val steps = (dragOffset / rowHeightPx).toInt()
              if (steps != 0) {
                val active = latestActive.value
                val fromDisplayIndex = active.indexOfFirst { it.value.uuId == itemId }
                if (fromDisplayIndex != -1) {
                  val toDisplayIndex = (fromDisplayIndex + steps).coerceIn(0, active.size - 1)
                  if (toDisplayIndex != fromDisplayIndex) {
                    viewModel.onReorder(active[fromDisplayIndex].index, active[toDisplayIndex].index)
                  }
                }
                dragOffset -= steps * rowHeightPx
              }
            },
          )
        },
      )
    }

    if (grouped.active.isEmpty() && grouped.completed.isNotEmpty()) {
      item(key = "all_done") { AllDoneRow(modifier = Modifier.animateItem()) }
    }

    if (grouped.completed.isNotEmpty()) {
      item(key = "completed_header") {
        CompletedHeaderRow(
          count = grouped.completed.size,
          expanded = grouped.completedExpanded,
          onClick = { viewModel.onCompletedToggle() },
          modifier = Modifier.animateItem(),
        )
      }
      if (grouped.completedExpanded) {
        items(grouped.completed, key = { it.value.uuId }) { indexed ->
          ShopItemRow(
            item = indexed.value,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
            onCheckClick = { viewModel.onCheckPressed(indexed.index) },
            onTextChange = { text -> viewModel.onTextChanged(indexed.index, text) },
            onEnterPressed = { viewModel.onEnterPressed(indexed.index) },
            onDeletePressed = { viewModel.onDeletePressed(indexed.index) },
            onRemoveClick = { viewModel.onRemovePressed(indexed.index) },
            modifier = Modifier.animateItem(),
            dragHandleModifier = null,
          )
        }
      }
    }
  }
}

@Composable
private fun ShopItemRow(
  item: ShopItem,
  hapticFeedbackEnabled: Boolean,
  onCheckClick: () -> Unit,
  onTextChange: (String) -> Unit,
  onEnterPressed: () -> Unit,
  onDeletePressed: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier,
  dragHandleModifier: Modifier? = null,
) {
  var text by remember(item.uuId) { mutableStateOf(item.summary) }
  var isFocused by remember { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val hapticFeedback = LocalHapticFeedback.current

  LaunchedEffect(item.showInput) {
    if (item.showInput) {
      focusRequester.requestFocus()
      keyboardController?.show()
    }
  }

  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    if (dragHandleModifier != null) {
      Icon(
        painter = AppIcons.Fluent.ReOrderDots,
        contentDescription = stringResource(R.string.todo_drag_to_reorder),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = dragHandleModifier.size(20.dp),
      )
    } else {
      Box(modifier = Modifier.size(20.dp))
    }
    val checkToggleDescription = if (item.isChecked) {
      stringResource(R.string.cd_mark_as_not_done)
    } else {
      stringResource(R.string.cd_mark_as_done)
    }
    TooltipIconButton(contentDescription = checkToggleDescription) {
      IconButton(
        onClick = {
          if (hapticFeedbackEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
          }
          onCheckClick()
        },
        modifier = Modifier
          .size(40.dp)
          .semantics { contentDescription = checkToggleDescription }
          .testTag(shopItemCheckTestTag(item.uuId)),
      ) {
        AnimatedVisibility(
          visible = item.isChecked,
          enter = scaleIn(tween(CHECK_ANIMATION_MS)) + fadeIn(tween(CHECK_ANIMATION_MS)),
          exit = scaleOut(tween(CHECK_ANIMATION_MS)) + fadeOut(tween(CHECK_ANIMATION_MS)),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_checkbox_checked),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
        AnimatedVisibility(
          visible = !item.isChecked,
          enter = scaleIn(tween(CHECK_ANIMATION_MS)) + fadeIn(tween(CHECK_ANIMATION_MS)),
          exit = scaleOut(tween(CHECK_ANIMATION_MS)) + fadeOut(tween(CHECK_ANIMATION_MS)),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_checkbox_unchecked),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
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
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else LocalContentColor.current,
          textDecoration =
            if (item.isChecked) {
              androidx.compose.ui.text.style.TextDecoration.LineThrough
            } else {
              null
            },
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { if (text.isNotEmpty()) onEnterPressed() }),
      )
    }
    if (isFocused && text.isNotEmpty()) {
      TooltipIconButton(contentDescription = stringResource(R.string.cd_remove)) {
        IconButton(
          onClick = onRemoveClick,
          modifier = Modifier
            .size(40.dp)
            .testTag(shopItemRemoveTestTag(item.uuId)),
        ) {
          Icon(
            modifier = Modifier
              .fillMaxSize()
              .padding(12.dp),
            painter = AppIcons.Fluent.Dismiss,
            contentDescription = stringResource(R.string.cd_remove),
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun CompletedHeaderRow(
  count: Int,
  expanded: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "completedChevron")
  Row(
    modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = AppIcons.Builder.ChevronDown,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .size(18.dp)
        .graphicsLayer { rotationZ = rotation },
    )
    Text(
      text = stringResource(R.string.todo_completed_count, count),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 8.dp),
    )
  }
}

@Composable
private fun AllDoneRow(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = AppIcons.Fluent.CheckboxChecked,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier.size(18.dp),
    )
    Text(
      text = stringResource(R.string.todo_all_done),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier.padding(start = 8.dp),
    )
  }
}
