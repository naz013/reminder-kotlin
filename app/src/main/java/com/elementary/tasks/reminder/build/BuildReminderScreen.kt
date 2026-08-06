package com.elementary.tasks.reminder.build

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPrediction
import com.elementary.tasks.reminder.build.quickstart.QuickStartOption
import com.github.naz013.domain.Tag
import com.github.naz013.tags.compose.TagChipPicker
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.BuilderItemStatus
import com.github.naz013.ui.common.compose.foundation.component.BuilderListItemCard
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import androidx.compose.ui.text.font.Typeface as ComposeTypeface

private const val OVERFLOW_ITEM_HELP = 1
private const val OVERFLOW_ITEM_REPORT_ISSUE = 2

/**
 * The reminder builder screen: its own Material 3 [Scaffold]/[TopAppBar] (mirroring
 * `R.menu.fragment_reminder_builder`'s save/delete/configure/help/report-issue actions) wrapping
 * the list of configured builder items (or an empty state when there are none), a forecast row
 * predicting when the reminder will fire, an optional "save as preset" row, and a FAB to add a new
 * item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildReminderScreen(
  builderItems: List<UiBuilderItem>,
  prediction: ReminderPrediction?,
  canSave: Boolean,
  canRemove: Boolean,
  canSaveAsPreset: Boolean,
  saveAsPresetChecked: Boolean,
  presetName: String,
  quickStartOptions: List<QuickStartOption>,
  allTags: List<Tag>,
  selectedTagIds: Set<String>,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onHelpClick: () -> Unit,
  onReportIssueClick: () -> Unit,
  onSaveAsPresetChange: (Boolean) -> Unit,
  onPresetNameChange: (String) -> Unit,
  onItemClick: (Int, BuilderItem<*>) -> Unit,
  onItemRemove: (Int, BuilderItem<*>) -> Unit,
  onAddClick: () -> Unit,
  onQuickStartClick: (QuickStartOption) -> Unit,
  onTagToggle: (Tag) -> Unit,
  onManageTagsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {},
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(painter = AppIcons.Builder.ArrowLeft, contentDescription = null)
          }
        },
        actions = {
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = canSave,
            onClick = onSaveClick,
          )
          if (canRemove) {
            IconButton(onClick = onDeleteClick) {
              Icon(
                painter = painterResource(R.drawable.ic_fluent_delete),
                contentDescription = stringResource(R.string.delete),
              )
            }
          }
          var overflowExpanded by remember { mutableStateOf(false) }
          IconButton(onClick = { overflowExpanded = true }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
          }
          AppDropdownMenu(
            expanded = overflowExpanded,
            onDismissRequest = { overflowExpanded = false },
            items =
              listOf(
                PopupMenuItem(id = OVERFLOW_ITEM_HELP, title = stringResource(R.string.help)),
                PopupMenuItem(
                  id = OVERFLOW_ITEM_REPORT_ISSUE,
                  title = stringResource(R.string.report_an_issue),
                ),
              ),
            onItemClick = { id ->
              overflowExpanded = false
              when (id) {
                OVERFLOW_ITEM_HELP -> onHelpClick()
                OVERFLOW_ITEM_REPORT_ISSUE -> onReportIssueClick()
              }
            },
          )
        },
        colors = TopAppbarColor,
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(
          painter = painterResource(R.drawable.ic_builder_add_circle),
          contentDescription = stringResource(R.string.acc_add),
        )
      }
    },
  ) { padding ->
    if (builderItems.isEmpty()) {
      BuilderEmptyState(
        quickStartOptions = quickStartOptions,
        onQuickStartClick = onQuickStartClick,
        onMoreOptionsClick = onAddClick,
        modifier = Modifier.fillMaxSize().padding(padding),
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
          ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(builderItems, key = { _, item -> item.key }) { index, item ->
          BuilderItemRow(
            item = item,
            onClick = { onItemClick(index, item.builderItem) },
            onRemoveClick = { onItemRemove(index, item.builderItem) },
          )
        }

        item(key = "tags") {
          TagsRow(
            allTags = allTags,
            selectedTagIds = selectedTagIds,
            onToggle = onTagToggle,
            onManageTagsClick = onManageTagsClick,
          )
        }

        if (prediction != null) {
          item(key = "forecast") { ForecastRow(prediction) }
        }

        if (canSaveAsPreset) {
          item(key = "save_as_preset") {
            SaveAsPresetRow(
              checked = saveAsPresetChecked,
              onCheckedChange = onSaveAsPresetChange,
              presetName = presetName,
              onPresetNameChange = onPresetNameChange,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BuilderItemRow(
  item: UiBuilderItem,
  onClick: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val status =
    when (item.state) {
      is UiListBuilderItemState.EmptyState -> BuilderItemStatus.EMPTY
      is UiListBuilderItemState.DoneState -> BuilderItemStatus.DONE
      is UiListBuilderItemState.ErrorState -> BuilderItemStatus.ERROR
    }
  val errorText = if (item.state is UiListBuilderItemState.ErrorState) item.errorText else null

  when (item) {
    is UiListNoteBuilderItem -> {
      BuilderListItemCard(
        icon = painterResource(item.builderItem.iconRes),
        title = item.builderItem.title,
        status = status,
        onClick = onClick,
        onRemoveClick = onRemoveClick,
        errorText = errorText,
        modifier = modifier,
        value = { NoteBuilderItemPreview(noteData = item.noteData, fallbackText = item.value) },
      )
    }

    is UiListBuilderItem -> {
      BuilderListItemCard(
        icon = painterResource(item.builderItem.iconRes),
        title = item.builderItem.title,
        value = item.value,
        status = status,
        onClick = onClick,
        onRemoveClick = onRemoveClick,
        errorText = errorText,
        modifier = modifier,
      )
    }
  }
}

/** Value-area content for a note builder item: note text + image thumbnails on the note's own
 *  background color, matching `list_item_reminder_builder_note_preview.xml`. Falls back to a
 *  plain value line while the referenced note hasn't loaded yet. */
@Composable
private fun NoteBuilderItemPreview(noteData: UiNoteList?, fallbackText: String) {
  if (noteData == null) {
    Text(
      text = fallbackText,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )
    return
  }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Color(noteData.backgroundColor), MaterialTheme.shapes.small)
        .padding(8.dp),
  ) {
    val bodyText =
      if (noteData.text.length > NOTE_BODY_MAX_CHARS) {
        noteData.text.substring(0, NOTE_BODY_MAX_CHARS) + "..."
      } else {
        noteData.text
      }
    if (bodyText.isNotEmpty()) {
      val fontFamily =
        remember(noteData.typeface) {
          noteData.typeface?.let { FontFamily(ComposeTypeface(it)) }
        }
      Text(
        text = bodyText,
        color = Color(noteData.textColor),
        fontFamily = fontFamily,
        fontSize = noteData.fontSize.sp,
        maxLines = NOTE_BODY_MAX_LINES,
        overflow = TextOverflow.Ellipsis,
      )
    }

    val images = noteData.images
    if (images.isNotEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      AsyncImage(
        model = images.first().filePath,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
          Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(MaterialTheme.shapes.small),
      )
      if (images.size > 1) {
        Row(
          modifier =
            Modifier
              .horizontalScroll(rememberScrollState())
              .padding(top = 4.dp),
        ) {
          images.drop(1).forEach { image ->
            AsyncImage(
              model = image.filePath,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier =
                Modifier
                  .size(72.dp)
                  .padding(end = 4.dp)
                  .clip(MaterialTheme.shapes.small),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ForecastRow(prediction: ReminderPrediction, modifier: Modifier = Modifier) {
  val (icon, message) =
    when (prediction) {
      is ReminderPrediction.SuccessPrediction -> prediction.icon to prediction.message
      is ReminderPrediction.FailedPrediction -> prediction.icon to prediction.message
    }
  Row(
    modifier = modifier.fillMaxWidth().padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(start = 8.dp).size(24.dp),
    )
    Text(
      text = message,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
    )
  }
}

@Composable
private fun TagsRow(
  allTags: List<Tag>,
  selectedTagIds: Set<String>,
  onToggle: (Tag) -> Unit,
  onManageTagsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = AppIcons.Builder.Tag,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(start = 8.dp).size(24.dp),
    )
    TagChipPicker(
      allTags = allTags,
      selectedTagIds = selectedTagIds,
      onToggle = onToggle,
      onManageTagsClick = onManageTagsClick,
      modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
    )
  }
}

@Composable
private fun SaveAsPresetRow(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  presetName: String,
  onPresetNameChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_builder_preset),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 8.dp),
      )
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = stringResource(R.string.recur_save_as_preset),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
      )
      Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    OutlinedTextField(
      value = presetName,
      onValueChange = onPresetNameChange,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      enabled = checked,
      label = { Text(stringResource(R.string.recur_preset_name_hint)) },
      singleLine = true,
    )
  }
}

@Composable
private fun BuilderEmptyState(
  quickStartOptions: List<QuickStartOption>,
  onQuickStartClick: (QuickStartOption) -> Unit,
  onMoreOptionsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat_empty_state))
    LottieAnimation(
      composition = composition,
      iterations = LottieConstants.IterateForever,
      modifier = Modifier.size(dimensionResource(R.dimen.empty_animation_size)),
    )
    Text(
      text = stringResource(R.string.builder_empty_message),
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      quickStartOptions.forEach { option ->
        QuickStartButton(
          text = stringResource(option.labelRes),
          onClick = { onQuickStartClick(option) },
        )
      }
      QuickStartButton(
        text = stringResource(R.string.builder_quick_start_more_options),
        onClick = onMoreOptionsClick,
      )
    }
    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun QuickStartButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  FilledTonalButton(
    onClick = onClick,
    shape = RoundedCornerShape(24.dp),
    modifier = modifier,
  ) {
    Text(text = text)
  }
}

private const val NOTE_BODY_MAX_CHARS = 500
private const val NOTE_BODY_MAX_LINES = 10

@Preview(showBackground = true, name = "Build reminder - empty")
@Composable
private fun PreviewBuildReminderScreenEmpty() {
  AppTheme {
    BuildReminderScreen(
      builderItems = emptyList(),
      prediction = null,
      canSave = false,
      canRemove = false,
      canSaveAsPreset = false,
      saveAsPresetChecked = false,
      presetName = "",
      quickStartOptions = QuickStartOption.entries,
      allTags = emptyList(),
      selectedTagIds = emptySet(),
      onBackClick = {},
      onSaveClick = {},
      onDeleteClick = {},
      onHelpClick = {},
      onReportIssueClick = {},
      onSaveAsPresetChange = {},
      onPresetNameChange = {},
      onItemClick = { _, _ -> },
      onItemRemove = { _, _ -> },
      onAddClick = {},
      onQuickStartClick = {},
      onTagToggle = {},
      onManageTagsClick = {},
    )
  }
}
