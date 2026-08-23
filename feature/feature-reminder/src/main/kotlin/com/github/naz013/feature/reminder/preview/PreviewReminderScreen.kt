package com.github.naz013.feature.reminder.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.AttachmentFile
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.AttachmentType
import com.github.naz013.feature.reminder.note.UiNoteList
import com.github.naz013.feature.reminder.preview.data.UiCalendarEventList
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.reminder.UiAppTarget
import com.github.naz013.ui.reminder.UiCallTarget
import com.github.naz013.ui.reminder.UiEmailTarget
import com.github.naz013.ui.reminder.UiReminderStatus
import com.github.naz013.ui.reminder.UiReminderType
import com.github.naz013.ui.reminder.UiSmsTarget
import com.github.naz013.ui.tag.TagChipRow
import com.github.naz013.ui.tag.TagChipState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreviewReminderScreen(
  modifier: Modifier = Modifier,
  state: PreviewReminderState,
  // True when shown as a two-pane detail pane rather than pushed full-screen - only changes the
  // leading icon (close vs. back), onBackClick pops the entry either way.
  renderAsDetailPane: Boolean = false,
  onBackClick: () -> Unit,
  onToggleClick: () -> Unit,
  onEditClick: () -> Unit,
  onShareClick: () -> Unit,
  onCopyClick: () -> Unit,
  onPinClick: () -> Unit,
  onSyncToCloudClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  onSubTaskCheck: (String) -> Unit,
  onSubTaskRemove: (String) -> Unit,
  onNoteClick: () -> Unit,
  onGoogleTaskClick: () -> Unit,
  onCalendarOpenClick: (UiCalendarEventList) -> Unit,
  onCalendarRemoveClick: (UiCalendarEventList) -> Unit,
  onTargetActionClick: () -> Unit,
  mapContent: @Composable () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.details)) },
        navigationIcon = {
          MenuIconButton(
            icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
            contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else null,
            onClick = onBackClick,
          )
        },
        actions = {
          OverflowMenu(
            canCopy = state.canCopy,
            // canDelete is only true once the reminder is already in the trash - pinning a
            // trashed reminder makes no sense since it no longer shows on the Agenda list.
            canPin = !state.canDelete,
            isPinned = state.isPinned,
            showSyncToCloud = state.showSyncToCloud,
            canDelete = state.canDelete,
            onEditClick = onEditClick,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onPinClick = onPinClick,
            onSyncToCloudClick = onSyncToCloudClick,
            onDeleteClick = onDeleteClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    if (state.isLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      item { HeaderCard(state = state, onToggleClick = onToggleClick) }

      item { SectionHeader(text = stringResource(R.string.details)) }
      item { DetailsCard(rows = detailRows(state)) }

      if (state.targetType != null) {
        item { TargetInfoSection(state = state, onClick = onTargetActionClick) }
      }

      if (state.attachments.isNotEmpty()) {
        item { SectionHeader(text = stringResource(R.string.builder_attachments)) }
        items(state.attachments, key = { it.uri.toString() + it.name }) { file -> AttachmentRow(file) }
      }

      if (state.subTasks.isNotEmpty()) {
        item {
          SubTasksSection(
            subTasks = state.subTasks,
            onCheck = onSubTaskCheck,
            onRemove = onSubTaskRemove,
          )
        }
      }

      if (state.places.isNotEmpty()) {
        item { MapSection(state = state, mapContent = mapContent) }
      }

      item { adsContent() }

      state.note?.let { note -> item { NoteRow(note = note, onClick = onNoteClick) } }
      state.googleTask?.let { task -> item { GoogleTaskRow(task = task, onClick = onGoogleTaskClick) } }

      if (state.calendarEvents.isNotEmpty()) {
        item { SectionHeader(text = stringResource(R.string.events)) }
        items(state.calendarEvents, key = { it.localId.ifEmpty { it.id.toString() } }) { event ->
          CalendarEventRow(
            event = event,
            onOpenClick = { onCalendarOpenClick(event) },
            onRemoveClick = { onCalendarRemoveClick(event) },
          )
        }
      }
    }
  }

  if (state.showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = onDeleteDismiss,
      text = { Text(stringResource(if (state.canDelete) R.string.delete else R.string.move_to_the_archive)) },
      confirmButton = { TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) } },
      dismissButton = { TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.no)) } },
    )
  }
}

@Composable
private fun OverflowMenu(
  canCopy: Boolean,
  canPin: Boolean,
  isPinned: Boolean,
  showSyncToCloud: Boolean,
  canDelete: Boolean,
  onEditClick: () -> Unit,
  onShareClick: () -> Unit,
  onCopyClick: () -> Unit,
  onPinClick: () -> Unit,
  onSyncToCloudClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val items = buildList {
    add(
      PopupMenuItem(
        id = OverflowAction.EDIT.ordinal,
        title = stringResource(R.string.edit),
        iconRes = R.drawable.ic_fluent_edit
      )
    )
    add(
      PopupMenuItem(
        id = OverflowAction.SHARE.ordinal,
        title = stringResource(R.string.share),
        iconRes = R.drawable.ic_fluent_share_android,
      ),
    )
    if (canCopy) {
      add(
        PopupMenuItem(
          id = OverflowAction.COPY.ordinal,
          title = stringResource(R.string.copy),
          iconRes = R.drawable.ic_fluent_copy
        )
      )
    }
    if (canPin) {
      add(
        PopupMenuItem(
          id = OverflowAction.PIN.ordinal,
          title = stringResource(if (isPinned) R.string.unpin else R.string.pin),
          iconRes = if (isPinned) DrawableCatalog.Fluent.PinOff else DrawableCatalog.Fluent.Pin,
        ),
      )
    }
    if (showSyncToCloud) {
      add(
        PopupMenuItem(
          id = OverflowAction.SYNC_TO_CLOUD.ordinal,
          title = stringResource(R.string.sync_to_cloud),
          iconRes = DrawableCatalog.Fluent.CloudBackup,
        ),
      )
    }
    add(
      PopupMenuItem(
        id = OverflowAction.DELETE.ordinal,
        title = stringResource(if (canDelete) R.string.delete else R.string.move_to_the_archive),
        iconRes = if (canDelete) R.drawable.ic_fluent_delete else R.drawable.ic_fluent_archive
      )
    )
  }
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = items,
      onItemClick = { id ->
        when (OverflowAction.entries[id]) {
          OverflowAction.EDIT -> onEditClick()
          OverflowAction.SHARE -> onShareClick()
          OverflowAction.COPY -> onCopyClick()
          OverflowAction.PIN -> onPinClick()
          OverflowAction.SYNC_TO_CLOUD -> onSyncToCloudClick()
          OverflowAction.DELETE -> onDeleteClick()
        }
      },
    )
  }
}

private enum class OverflowAction {
  EDIT,
  SHARE,
  COPY,
  PIN,
  SYNC_TO_CLOUD,
  DELETE,
}

@Composable
private fun HeaderCard(
  state: PreviewReminderState,
  onToggleClick: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      if (state.summary.isNotEmpty()) {
        Text(text = state.summary, style = MaterialTheme.typography.titleLarge)
      }
      state.description?.takeIf { it.isNotEmpty() }?.let { text ->
        Text(
          text = text,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      state.status?.let { status ->
        Spacer(modifier = Modifier.height(16.dp))
        StatusRow(status = status, onToggleClick = onToggleClick)
      }
    }
  }
}

@Composable
private fun StatusRow(
  status: UiReminderStatus,
  onToggleClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(if (status.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
      )
      Text(
        text = if (status.active) stringResource(R.string.enabled4) else stringResource(R.string.disabled),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(start = 8.dp),
      )
    }
    FilledTonalButton(
      onClick = onToggleClick,
      enabled = status.canToggle,
      colors = if (status.active) {
        ButtonDefaults.filledTonalButtonColors()
      } else {
        ButtonDefaults.filledTonalButtonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      },
    ) {
      Text(text = stringResource(if (status.active) R.string.turn_off else R.string.turn_on))
    }
  }
}

@Composable
private fun SectionHeader(text: String) {
  if (text.isEmpty()) return
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
  )
}

@Composable
private fun DetailRow(
  icon: Int,
  text: String,
  modifier: Modifier = Modifier,
  textDecoration: TextDecoration = TextDecoration.None,
  isWarning: Boolean = false,
) {
  val tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      textDecoration = textDecoration,
      color = if (isWarning) MaterialTheme.colorScheme.error else Color.Unspecified,
      modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp),
    )
  }
}

private data class DetailItem(
  val icon: Int,
  val text: String,
  val textDecoration: TextDecoration = TextDecoration.None,
)

private fun detailRows(state: PreviewReminderState): List<@Composable () -> Unit> =
  buildList {
    state.dueDateTime?.let { text -> add { DetailRow(icon = DrawableCatalog.Builder.ByMonthday, text = text) } }
    state.before?.let { text -> add { DetailRow(icon = DrawableCatalog.Builder.ByMonthday, text = text) } }
    add { DetailRow(icon = DrawableCatalog.Fluent.ArrowRepeatAll, text = state.repeat) }
    state.repeatLimitText?.let { text ->
      add {
        DetailRow(
          icon = DrawableCatalog.Builder.RepeatLimit,
          text = text,
          isWarning = state.isRepeatLimitReached,
        )
      }
    }
    state.repeatUntilText?.let { text -> add { DetailRow(icon = DrawableCatalog.Builder.ByMonthday, text = text) } }
    state.remaining?.let { text -> add { DetailRow(icon = DrawableCatalog.Builder.ByMonthday, text = text) } }
    state.triggeredCountText?.let { text -> add { DetailRow(icon = DrawableCatalog.Fluent.History, text = text) } }
    state.snoozedCountText?.let { text -> add { DetailRow(icon = DrawableCatalog.Fluent.Snooze, text = text) } }
    state.groupTitle?.let { text -> add { DetailRow(icon = DrawableCatalog.Fluent.Group, text = text) } }
    add { DetailRow(icon = DrawableCatalog.Fluent.Star, text = state.priorityTitle) }
    if (state.isOfflineOnly) {
      add { DetailRow(icon = DrawableCatalog.Fluent.Cloud, text = stringResource(R.string.offline_only_reminder_description)) }
    }
    if (state.tags.isNotEmpty()) {
      add { TagsDetailRow(tags = state.tags) }
    }
  }

@Composable
private fun TagsDetailRow(tags: List<TagChipState>) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
  ) {
    Icon(
      painter = AppIcons.Builder.Group,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(24.dp),
    )
    TagChipRow(tags = tags, modifier = Modifier.padding(start = 16.dp))
  }
}

private fun targetInfoItems(state: PreviewReminderState): List<DetailItem> {
  val type = state.targetType ?: return emptyList()
  return buildList {
    when {
      type.isCall() -> {
        (state.target as? UiCallTarget)?.name?.let { add(DetailItem(DrawableCatalog.Fluent.Person, it)) }
        add(DetailItem(DrawableCatalog.Builder.AddCall, state.rawTarget))
      }

      type.isSms() -> {
        (state.target as? UiSmsTarget)?.name?.let { add(DetailItem(DrawableCatalog.Fluent.Person, it)) }
        add(DetailItem(DrawableCatalog.Builder.SendMessage, state.rawTarget))
      }

      type.isApp() -> {
        val name = (state.target as? UiAppTarget)?.name
        add(DetailItem(DrawableCatalog.Builder.AddApp, name ?: state.rawTarget))
      }

      type.isLink() -> {
        add(DetailItem(DrawableCatalog.Builder.WebAddress, state.rawTarget, TextDecoration.Underline))
      }

      type.isEmail() -> {
        val emailTarget = state.target as? UiEmailTarget
        emailTarget?.name?.let { add(DetailItem(DrawableCatalog.Fluent.Person, it)) }
        add(DetailItem(DrawableCatalog.Builder.EmailAddress, state.rawTarget, TextDecoration.Underline))
        emailTarget
          ?.subject
          ?.takeIf { it.isNotEmpty() }
          ?.let { add(DetailItem(DrawableCatalog.Builder.EmailSubject, it, TextDecoration.Underline)) }
      }
    }
  }
}

@Composable
private fun DetailsCard(rows: List<@Composable () -> Unit>, onClick: (() -> Unit)? = null) {
  if (rows.isEmpty()) return
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .let { if (onClick != null) it.clickable(onClick = onClick) else it },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    rows.forEachIndexed { index, row ->
      row()
      if (index != rows.lastIndex) {
        HorizontalDivider(
          modifier = Modifier.padding(start = 56.dp),
          color = MaterialTheme.colorScheme.outlineVariant,
        )
      }
    }
  }
}

@Composable
private fun TargetInfoSection(state: PreviewReminderState, onClick: () -> Unit) {
  val type = state.targetType ?: return
  SectionHeader(text = targetHeaderText(type))
  val rows: List<@Composable () -> Unit> =
    targetInfoItems(state).map { item ->
      { DetailRow(icon = item.icon, text = item.text, textDecoration = item.textDecoration) }
    }
  val isActionable = type.isCall() || type.isSms() || type.isApp() || type.isLink()
  DetailsCard(rows = rows, onClick = if (isActionable) onClick else null)
}

@Composable
private fun targetHeaderText(type: UiReminderType): String =
  when {
    type.isCall() -> stringResource(R.string.make_call)
    type.isSms() -> stringResource(R.string.send_sms)
    type.isApp() -> stringResource(R.string.open_app)
    type.isLink() -> stringResource(R.string.open_link)
    type.isEmail() -> stringResource(R.string.e_mail)
    else -> ""
  }

@Composable
private fun AttachmentRow(file: AttachmentFile) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    if (file.type == AttachmentType.IMAGE) {
      AsyncImage(
        model = file.uri,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(4.dp)),
      )
    } else {
      Icon(
        painter = painterResource(file.icon),
        contentDescription = null,
        modifier = Modifier.size(32.dp),
      )
    }
    Text(
      text = file.name,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp),
    )
  }
}

@Composable
private fun SubTasksSection(
  subTasks: List<UiPreviewSubTask>,
  onCheck: (String) -> Unit,
  onRemove: (String) -> Unit,
) {
  val checkedCount = subTasks.count { it.isChecked }
  Column(modifier = Modifier.padding(top = 16.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.todo_items),
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = "$checkedCount/${subTasks.size}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
      subTasks.forEachIndexed { index, subTask ->
        SubTaskRow(subTask = subTask, onCheck = onCheck, onRemove = onRemove)
        if (index != subTasks.lastIndex) {
          HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun SubTaskRow(
  subTask: UiPreviewSubTask,
  onCheck: (String) -> Unit,
  onRemove: (String) -> Unit,
) {
  val contentColor = if (subTask.isChecked) {
    MaterialTheme.colorScheme.onSurfaceVariant
  } else {
    MaterialTheme.colorScheme.onSurface
  }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 4.dp, end = 4.dp),
  ) {
    MenuIconButton(
      icon = painterResource(
        if (subTask.isChecked) {
          R.drawable.ic_fluent_checkbox_checked
        } else {
          R.drawable.ic_fluent_checkbox_unchecked
        },
      ),
      contentDescription = if (subTask.isChecked) {
        stringResource(R.string.cd_mark_as_not_done)
      } else {
        stringResource(R.string.cd_mark_as_done)
      },
      iconColor = if (subTask.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      onClick = { onCheck(subTask.id) },
    )
    Text(
      text = subTask.text,
      style = MaterialTheme.typography.bodyLarge,
      color = contentColor,
      textDecoration = if (subTask.isChecked) TextDecoration.LineThrough else TextDecoration.None,
      modifier = Modifier.weight(1f),
    )
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_delete),
      contentDescription = stringResource(R.string.delete),
      iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
      onClick = { onRemove(subTask.id) },
    )
  }
}

@Composable
private fun MapSection(
  state: PreviewReminderState,
  mapContent: @Composable () -> Unit,
) {
  SectionHeader(text = state.placesHeader)
  Text(
    text = state.places.joinToString("\n") { it.address.ifEmpty { "%.5f,%.5f".format(it.latitude, it.longitude) } },
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
  )
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .height(200.dp)
      .clip(MaterialTheme.shapes.medium),
  ) {
    mapContent()
  }
}

@Composable
private fun NoteRow(
  note: UiNoteList,
  onClick: () -> Unit,
) {
  SectionHeader(text = stringResource(R.string.note))
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      if (note.title.isNotEmpty()) {
        Text(text = note.title, style = MaterialTheme.typography.titleMedium)
      }
      if (note.text.isNotEmpty()) {
        Text(
          text = note.text,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      Text(
        text = note.formattedDateTime,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
      )
    }
  }
}

@Composable
private fun GoogleTaskRow(
  task: GoogleTaskItemState,
  onClick: () -> Unit,
) {
  SectionHeader(text = stringResource(R.string.google_task))
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = task.text, style = MaterialTheme.typography.titleMedium)
      task.notes?.takeIf { it.isNotEmpty() }?.let {
        Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
      }
      task.dueDate?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun CalendarEventRow(
  event: UiCalendarEventList,
  onOpenClick: () -> Unit,
  onRemoveClick: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = event.title, style = MaterialTheme.typography.titleMedium)
      if (event.description.isNotEmpty()) {
        Text(
          text = event.description,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      event.calendarName?.takeIf { it.isNotEmpty() }?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      event.dateStartFormatted?.let {
        Text(text = it, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
      }
      event.dateEndFormatted?.let {
        Text(text = it, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 2.dp))
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentWidth(Alignment.End)
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        TextButton(onClick = onOpenClick) { Text(stringResource(R.string.open)) }
        TextButton(onClick = onRemoveClick) { Text(stringResource(R.string.delete)) }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PreviewReminderScreenPreview() {
  AppTheme {
    PreviewReminderScreen(
      state = PreviewReminderState(
        id = "1",
        isLoading = false,
        status = UiReminderStatus(title = "Enabled", active = true, removed = false),
        summary = "Buy milk",
        description = "2% milk, one gallon",
        dueDateTime = "Today, 18:00",
        repeat = "Once",
        repeatLimitText = "3 of 10 times · 7 left",
        priorityTitle = "Normal",
        subTasks = listOf(
          UiPreviewSubTask(id = "1", text = "Milk", isChecked = true),
          UiPreviewSubTask(id = "2", text = "Eggs", isChecked = false),
          UiPreviewSubTask(id = "3", text = "Bread", isChecked = false),
        ),
      ),
      onBackClick = {},
      onToggleClick = {},
      onEditClick = {},
      onShareClick = {},
      onCopyClick = {},
      onPinClick = {},
      onSyncToCloudClick = {},
      onDeleteClick = {},
      onDeleteConfirmed = {},
      onDeleteDismiss = {},
      onSubTaskCheck = {},
      onSubTaskRemove = {},
      onNoteClick = {},
      onGoogleTaskClick = {},
      onCalendarOpenClick = {},
      onCalendarRemoveClick = {},
      onTargetActionClick = {},
      mapContent = {},
      adsContent = {},
    )
  }
}
