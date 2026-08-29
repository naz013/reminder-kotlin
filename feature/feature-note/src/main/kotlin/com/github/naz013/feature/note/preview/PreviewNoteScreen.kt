package com.github.naz013.feature.note.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.note.NoteFontProvider
import com.github.naz013.ui.note.toAnnotatedString
import com.github.naz013.ui.tag.TagChipRow
import org.koin.compose.koinInject

private const val OVERFLOW_ITEM_SHARE = 0
private const val OVERFLOW_ITEM_ARCHIVE = 1
private const val OVERFLOW_ITEM_PIN = 2
private const val OVERFLOW_ITEM_DELETE = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreviewNoteScreen(
  modifier: Modifier = Modifier,
  state: PreviewNoteState,
  actions: PreviewNoteActions,
  adsBanner: (@Composable () -> Unit)? = null,
) {
  Column(
    modifier =
    modifier
      .fillMaxSize()
      .background(state.background),
  ) {
    TopAppBar(
      title = { },
      navigationIcon = {
        MenuIconButton(
          icon = AppIcons.Builder.ArrowLeft,
          iconColor = state.content,
          contentDescription = stringResource(R.string.cd_back),
          onClick = actions.onBackClick,
        )
      },
      actions = {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_edit),
          iconColor = state.content,
          contentDescription = stringResource(R.string.edit),
          onClick = actions.onEditClick,
        )
        MenuIconButton(
          icon = AppIcons.Fluent.Heart,
          iconColor = state.content,
          contentDescription = stringResource(R.string.show_in_status_bar),
          onClick = actions.onStatusClick,
        )
        var overflowExpanded by remember { mutableStateOf(false) }
        MenuIconButton(
          icon = AppIcons.Fluent.MoreVertical,
          iconColor = state.content,
          contentDescription = stringResource(R.string.more_options),
          onClick = { overflowExpanded = true },
        )
        AppDropdownMenu(
          expanded = overflowExpanded,
          onDismissRequest = { overflowExpanded = false },
          items =
          listOf(
            PopupMenuItem(
              id = OVERFLOW_ITEM_SHARE,
              title = stringResource(R.string.share),
              iconRes = R.drawable.ic_fluent_share_android,
            ),
            PopupMenuItem(
              id = OVERFLOW_ITEM_ARCHIVE,
              title =
              stringResource(
                if (state.isArchived) {
                  R.string.notes_unarchive
                } else {
                  R.string.notes_move_to_archive
                },
              ),
              iconRes = R.drawable.ic_fluent_archive,
            ),
            PopupMenuItem(
              id = OVERFLOW_ITEM_PIN,
              title = stringResource(if (state.isPinned) R.string.unpin else R.string.pin),
              iconRes = if (state.isPinned) DrawableCatalog.Fluent.PinOff else DrawableCatalog.Fluent.Pin,
            ),
            PopupMenuItem(
              id = OVERFLOW_ITEM_DELETE,
              title = stringResource(R.string.delete),
              iconRes = R.drawable.ic_fluent_delete,
            ),
          ),
          onItemClick = { id ->
            when (id) {
              OVERFLOW_ITEM_SHARE -> actions.onShareClick()
              OVERFLOW_ITEM_ARCHIVE -> actions.onArchiveClick()
              OVERFLOW_ITEM_PIN -> actions.onPinClick()
              OVERFLOW_ITEM_DELETE -> actions.onDeleteClick()
            }
          },
        )
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      modifier = Modifier.statusBarsPadding(),
    )

    Column(
      modifier =
      Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
    ) {
      val context = LocalContext.current
      val noteFontProvider = koinInject<NoteFontProvider>()
      val bodyFontFamily = remember(state.fontStyle) {
        noteFontProvider.getTypeface(context, state.fontStyle)?.let { FontFamily(it) } ?: FontFamily.Default
      }
      val annotatedText = remember(state.document, state.fontSize) {
        state.document.toAnnotatedString(baseFontSizeSp = state.fontSize.toInt()) { code ->
          noteFontProvider.getTypeface(context, code)?.let { FontFamily(it) }
        }
      }
      Text(
        text = annotatedText,
        style =
        MaterialTheme.typography.bodyLarge.copy(
          color = state.content,
          fontSize = state.fontSize.sp,
          fontFamily = bodyFontFamily,
          lineHeight = TextUnit.Unspecified,
        ),
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
      )

      TagChipRow(
        tags = state.tags,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp),
      )

      PreviewNoteImageCarousel(
        images = state.images,
        onImageClick = actions.onImageOpen,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp),
      )

      adsBanner?.let { banner ->
        Column(modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp)) {
          banner()
        }
      }

      PreviewNoteReminderRow(
        reminders = state.reminders,
        onEditClick = actions.onReminderEditClick,
        onDetachClick = actions.onReminderDetachClick,
        modifier = Modifier.padding(top = 16.dp),
      )
    }
  }
}
