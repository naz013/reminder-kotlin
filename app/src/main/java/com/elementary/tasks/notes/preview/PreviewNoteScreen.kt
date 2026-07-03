package com.elementary.tasks.notes.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.PopupMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import kotlinx.coroutines.delay

private const val CONTENT_ANIMATION_DURATION_MS = 250
private const val CONTENT_ITEM_STAGGER_DELAY_MS = 60L

private const val OVERFLOW_ITEM_SHARE = 0
private const val OVERFLOW_ITEM_ARCHIVE = 1
private const val OVERFLOW_ITEM_DELETE = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewNoteScreen(
  state: PreviewNoteState,
  colors: NotePreviewColors,
  actions: PreviewNoteActions,
  adsBanner: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(colors.background),
  ) {
    TopAppBar(
      title = { },
      navigationIcon = {
        IconButton(onClick = actions.onBackClick) {
          Icon(
            painter = painterResource(R.drawable.ic_builder_arrow_left),
            contentDescription = stringResource(R.string.cd_back),
            tint = colors.content,
          )
        }
      },
      actions = {
        IconButton(onClick = actions.onEditClick) {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_edit),
            contentDescription = stringResource(R.string.edit),
            tint = colors.content,
          )
        }
        IconButton(onClick = actions.onStatusClick) {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_heart),
            contentDescription = stringResource(R.string.show_in_status_bar),
            tint = colors.content,
          )
        }
        var overflowExpanded by remember { mutableStateOf(false) }
        IconButton(onClick = { overflowExpanded = true }) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = colors.content,
          )
        }
        PopupMenu(
          expanded = overflowExpanded,
          onDismissRequest = { overflowExpanded = false },
          items = listOf(
            PopupMenuItem(
              id = OVERFLOW_ITEM_SHARE,
              title = stringResource(R.string.share),
              iconRes = R.drawable.ic_fluent_share_android,
            ),
            PopupMenuItem(
              id = OVERFLOW_ITEM_ARCHIVE,
              title = stringResource(
                if (state.isArchived) {
                  R.string.notes_unarchive
                } else {
                  R.string.notes_move_to_archive
                },
              ),
              iconRes = R.drawable.ic_fluent_archive,
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
      if (state.title.isNotEmpty()) {
        AnimatedContentItem(index = 0) {
          Text(
            text = state.title,
            style =
              MaterialTheme.typography.bodyLarge.copy(
                color = colors.content,
                fontSize = state.titleTextSize.sp,
                fontFamily = state.titleTypeface?.let { FontFamily(it) } ?: FontFamily.Default,
                lineHeight = TextUnit.Unspecified,
              ),
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
          )
        }
      }
      AnimatedContentItem(index = if (state.title.isNotEmpty()) 1 else 0) {
        Text(
          text = state.text,
          style =
            MaterialTheme.typography.bodyLarge.copy(
              color = colors.content,
              fontSize = state.textSize.sp,
              fontFamily = state.typeface?.let { FontFamily(it) } ?: FontFamily.Default,
              lineHeight = TextUnit.Unspecified,
            ),
          modifier =
            Modifier.padding(
              start = 24.dp,
              top = if (state.title.isNotEmpty()) 24.dp else 16.dp,
              end = 24.dp,
            ),
        )
      }

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

  if (state.activeDialog == PreviewNoteDialog.DELETE) {
    AlertDialog(
      onDismissRequest = actions.onDialogDismiss,
      text = { Text(stringResource(R.string.delete_this_note)) },
      confirmButton = {
        TextButton(onClick = actions.onDeleteConfirmed) {
          Text(stringResource(R.string.yes))
        }
      },
      dismissButton = {
        TextButton(onClick = actions.onDialogDismiss) {
          Text(stringResource(R.string.no))
        }
      },
    )
  }
}

/** Fades and slides a content block in, staggered by [index] — same entrance pattern used for
 *  [com.elementary.tasks.home.ChronologicalHomeScreen]'s list items. */
@Composable
private fun AnimatedContentItem(
  index: Int,
  content: @Composable () -> Unit,
) {
  val visibleState = remember { MutableTransitionState(false) }
  LaunchedEffect(Unit) {
    delay(index * CONTENT_ITEM_STAGGER_DELAY_MS)
    visibleState.targetState = true
  }
  AnimatedVisibility(
    visibleState = visibleState,
    enter =
      fadeIn(animationSpec = tween(CONTENT_ANIMATION_DURATION_MS)) +
        slideInVertically(
          animationSpec = tween(CONTENT_ANIMATION_DURATION_MS),
        ) { fullHeight -> fullHeight / 6 },
  ) {
    content()
  }
}
