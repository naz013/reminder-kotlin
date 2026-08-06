package com.elementary.tasks.notes.create

import android.content.ClipDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.dragAndDropHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
  state: NoteEditState,
  onTextFieldValueChange: (TextFieldValue) -> Unit,
  onTitleFieldValueChange: (TextFieldValue) -> Unit,
  supportsSpeech: Boolean,
  actions: NoteEditActions,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  val backgroundColor = state.noteColors.background
  val contentColor = state.noteColors.content
  val sliderColors = state.sliderColors
  val dropHighlightColor = MaterialTheme.colorScheme.primary

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .background(backgroundColor)
        .dragAndDropHighlight(
          dropHighlightColor,
          onDrop = actions.onDrop,
          mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN, UriUtil.ANY_MIME),
        ),
  ) {
    val barMaxWidth = maxWidth - 32.dp
    Column(modifier = Modifier.fillMaxSize()) {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = actions.onBackClick) {
            Icon(
              painter = AppIcons.Builder.ArrowLeft,
              contentDescription = null,
              tint = contentColor,
            )
          }
        },
        title = {},
        actions = {
          MenuTextButton(
            text = stringResource(R.string.save),
            color = contentColor,
            onClick = actions.onSaveClick,
          )
          IconButton(onClick = actions.onShareClick) {
            Icon(
              painter = painterResource(R.drawable.ic_fluent_share_android),
              contentDescription = stringResource(R.string.share),
              tint = contentColor,
            )
          }
          if (state.canDelete) {
            IconButton(onClick = actions.onDeleteClick) {
              Icon(
                painter = painterResource(R.drawable.ic_fluent_delete),
                contentDescription = stringResource(R.string.delete),
                tint = contentColor,
              )
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
            titleContentColor = contentColor,
          ),
      )

      Column(
        modifier =
          Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
              detectTapGestures(onTap = { focusManager.clearFocus() })
            }.padding(horizontal = 16.dp),
      ) {
        val context = LocalContext.current
        val titleFontFamily =
          remember(state.titleFontStyle) {
            AssetsUtil.getTypeface(context, state.titleFontStyle)?.let { FontFamily(it) }
              ?: FontFamily.Default
          }
        val fontFamily =
          remember(state.fontStyle) {
            AssetsUtil.getTypeface(context, state.fontStyle)?.let { FontFamily(it) }
              ?: FontFamily.Default
          }
        TextField(
          value = state.titleFieldValue,
          onValueChange = onTitleFieldValueChange,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(top = 16.dp)
              .onFocusChanged { if (it.isFocused) actions.onFieldFocused(NoteTextField.TITLE) },
          textStyle =
            MaterialTheme.typography.bodyLarge.copy(
              color = contentColor,
              fontSize = state.titleFontSize.sp,
              fontFamily = titleFontFamily,
              lineHeight = TextUnit.Unspecified,
            ),
          placeholder = { Text(stringResource(R.string.title)) },
          colors =
            TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              cursorColor = contentColor,
              focusedTextColor = contentColor,
              unfocusedTextColor = contentColor,
              focusedPlaceholderColor = contentColor.copy(alpha = 0.6f),
              unfocusedPlaceholderColor = contentColor.copy(alpha = 0.6f),
            ),
        )
        TextField(
          value = state.textFieldValue,
          onValueChange = onTextFieldValueChange,
          modifier =
            Modifier
              .fillMaxWidth()
              .onFocusChanged { if (it.isFocused) actions.onFieldFocused(NoteTextField.BODY) },
          textStyle =
            MaterialTheme.typography.bodyLarge.copy(
              color = contentColor,
              fontSize = state.fontSize.sp,
              fontFamily = fontFamily,
              lineHeight = TextUnit.Unspecified,
            ),
          placeholder = { Text(stringResource(R.string.note)) },
          visualTransformation = boldRangeVisualTransformation(state.boldRange),
          colors =
            TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              cursorColor = contentColor,
              focusedTextColor = contentColor,
              unfocusedTextColor = contentColor,
              focusedPlaceholderColor = contentColor.copy(alpha = 0.6f),
              unfocusedPlaceholderColor = contentColor.copy(alpha = 0.6f),
            ),
        )

        NoteEditImageGrid(
          images = state.images,
          onImageClick = actions.onImageOpen,
          onRemoveClick = actions.onImageRemove,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(top = 16.dp),
        )

        // Reserve space so the floating bar never overlaps the last content row.
        Box(modifier = Modifier.height(112.dp))
      }
    }

    val barContainerColor = MaterialTheme.colorScheme.primaryContainer
    val barContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    NoteEditFloatingBar(
      items =
        noteEditBarItems(
          state = state,
          supportsSpeech = supportsSpeech,
          contentColor = barContentColor,
          barColor = barContainerColor,
          barMaxWidth = barMaxWidth,
          sliderColors = sliderColors,
          actions = actions,
        ),
      containerColor = barContainerColor,
      contentColor = barContentColor,
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .imePadding()
          .padding(bottom = 24.dp)
          .widthIn(max = barMaxWidth),
    )
  }

  when (state.activeDialog) {
    NoteEditDialog.DELETE ->
      DeleteNoteDialog(
        onDismiss = actions.onDialogDismiss,
        onConfirm = actions.onDeleteConfirmed,
      )

    NoteEditDialog.SAME_NOTE ->
      SameNoteDialog(
        onDismiss = actions.onDialogDismiss,
        onKeep = actions.onSameNoteKeep,
        onReplace = actions.onSameNoteReplace,
      )

    null -> Unit
  }
}

/**
 * Builds the floating bar's item list. New tools go here as additional entries — the bar itself
 * ([NoteEditFloatingBar]) doesn't need to change.
 */
@Composable
private fun noteEditBarItems(
  state: NoteEditState,
  supportsSpeech: Boolean,
  contentColor: Color,
  barColor: Color,
  barMaxWidth: Dp,
  sliderColors: List<Color>,
  actions: NoteEditActions,
): List<NoteEditBarItem> =
  buildList {
    // Same order as the Phase 1 docked bar: mic, color, image, reminder, font.
    if (supportsSpeech) {
      add(
        NoteEditBarItem(
          id = "mic",
          contentDescription = stringResource(R.string.acc_type_by_voice),
          onClick = actions.onMicClick,
          icon = { MicIcon(state.speechState, contentColor) },
        ),
      )
    }

    val colorDescription = stringResource(R.string.acc_select_color)
    add(
      NoteEditBarItem(
        id = "color",
        contentDescription = colorDescription,
        selected = state.expandedTab == EditTab.COLOR,
        onClick = actions.onColorTabClick,
        icon = {
          val swatch = sliderColors.getOrNull(state.colorIndex) ?: contentColor
          Box(
            modifier =
              Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(swatch.copy(alpha = state.opacity / 100f))
                .border(2.dp, contentColor.copy(alpha = 0.6f), CircleShape)
                .semantics { contentDescription = colorDescription },
          )
        },
        bubbleContent = {
          ColorPanel(
            colors = state.sliderColors,
            selectedIndex = state.colorIndex,
            opacity = state.opacity,
            contentColor = contentColor,
            onColorSelected = actions.onColorSelected,
            onOpacityChanged = actions.onOpacityChanged,
            hapticFeedbackEnabled = state.hapticFeedbackEnabled,
          )
        },
        bubbleWidth = barMaxWidth,
      ),
    )

    val imageDescription = stringResource(R.string.acc_add_image_to_reminder)
    add(
      NoteEditBarItem(
        id = "image",
        contentDescription = imageDescription,
        selected = state.expandedTab == EditTab.IMAGE,
        onClick = actions.onImageTabClick,
        icon = {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_image),
            contentDescription = imageDescription,
            tint = contentColor,
          )
        },
        bubbleContent = {
          ImageSourcePanel(
            hasCamera = state.hasCamera,
            contentColor = contentColor,
            onGalleryClick = actions.onImagePickFromGallery,
            onCameraClick = actions.onImagePickFromCamera,
            onUrlClick = actions.onImagePickFromUrl,
          )
        },
      ),
    )

    val reminderDescription = stringResource(R.string.acc_add_reminder)
    add(
      NoteEditBarItem(
        id = "reminder",
        contentDescription = reminderDescription,
        selected = state.expandedTab == EditTab.REMINDER,
        showBadge = state.isReminderAttached,
        onClick = actions.onReminderTabClick,
        icon = {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_alert),
            contentDescription = reminderDescription,
            tint = contentColor,
          )
        },
        bubbleContent = { ReminderPanel(state, contentColor, actions) },
      ),
    )

    val tagsDescription = stringResource(R.string.tags)
    add(
      NoteEditBarItem(
        id = "tags",
        contentDescription = tagsDescription,
        selected = state.expandedTab == EditTab.TAGS,
        showBadge = state.selectedTagIds.isNotEmpty(),
        onClick = actions.onTagsTabClick,
        icon = {
          Icon(
            painter = AppIcons.Builder.Tag,
            contentDescription = tagsDescription,
            tint = contentColor,
          )
        },
        bubbleContent = { TagsPanel(state, actions) },
      ),
    )

    val fontDescription = stringResource(R.string.acc_change_text_font_style)
    add(
      NoteEditBarItem(
        id = "font",
        contentDescription = fontDescription,
        selected = state.expandedTab == EditTab.FONT,
        onClick = actions.onFontTabClick,
        icon = {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_text),
            contentDescription = fontDescription,
            tint = contentColor,
          )
        },
        bubbleContent = { FontPanel(state, contentColor, barColor, actions) },
      ),
    )
  }

private fun boldRangeVisualTransformation(range: IntRange?): VisualTransformation =
  VisualTransformation { text ->
    if (range == null || range.first < 0 || range.last >= text.length || range.first > range.last) {
      TransformedText(text, OffsetMapping.Identity)
    } else {
      val annotated =
        AnnotatedString
          .Builder(text)
          .apply {
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), range.first, range.last + 1)
          }.toAnnotatedString()
      TransformedText(annotated, OffsetMapping.Identity)
    }
  }
