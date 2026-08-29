package com.github.naz013.feature.note.create

import android.content.ClipDescription
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.dragAndDropHighlight
import com.github.naz013.ui.note.NoteFontProvider
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteEditScreen(
  state: NoteEditState,
  onTextFieldValueChange: (TextFieldValue) -> Unit,
  supportsSpeech: Boolean,
  actions: NoteEditActions,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  val targetBackground = state.noteColors.background
  val targetContent = state.noteColors.content
  val backgroundColor = if (targetBackground.isSpecified) {
    animateColorAsState(targetBackground, label = "noteBackgroundColor").value
  } else targetBackground
  val contentColor = if (targetContent.isSpecified) {
    animateColorAsState(targetContent, label = "noteContentColor").value
  } else targetContent
  val sliderColors = state.sliderColors
  val dropHighlightColor = MaterialTheme.colorScheme.primary

  BoxWithConstraints(
    modifier = modifier
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
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            iconColor = contentColor,
            onClick = actions.onBackClick,
          )
        },
        title = {},
        actions = {
          MenuTextButton(
            text = stringResource(R.string.save),
            color = contentColor,
            onClick = actions.onSaveClick,
          )
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_share_android),
            iconColor = contentColor,
            contentDescription = stringResource(R.string.share),
            onClick = actions.onShareClick,
          )
          if (state.canDelete) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_delete),
              iconColor = contentColor,
              contentDescription = stringResource(R.string.delete),
              onClick = actions.onDeleteClick,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color.Transparent,
          navigationIconContentColor = contentColor,
          actionIconContentColor = contentColor,
          titleContentColor = contentColor,
        ),
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
          }
          .padding(horizontal = 16.dp),
      ) {
        val context = LocalContext.current
        val noteFontProvider = koinInject<NoteFontProvider>()
        val fontFamily = remember(state.fontStyle) {
          noteFontProvider.getTypeface(context, state.fontStyle)?.let { FontFamily(it) }
            ?: FontFamily.Default
        }
        val visualTransformation = remember(state.textFieldValue.text, state.spans, state.fontSize) {
          noteEditVisualTransformation(
            document = NoteDocument(state.textFieldValue.text, state.spans),
            baseFontSizeSp = state.fontSize,
          ) { code -> noteFontProvider.getTypeface(context, code)?.let { FontFamily(it) } }
        }
        TextField(
          value = state.textFieldValue,
          onValueChange = onTextFieldValueChange,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = contentColor,
            fontSize = state.fontSize.sp,
            fontFamily = fontFamily,
            lineHeight = TextUnit.Unspecified,
          ),
          placeholder = { Text(stringResource(R.string.note)) },
          visualTransformation = visualTransformation,
          colors = TextFieldDefaults.colors(
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
          modifier = Modifier
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
      items = noteEditBarItems(
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
      modifier = Modifier
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
            modifier = Modifier
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

    val textFormatDescription = stringResource(R.string.acc_select_text_format)
    add(
      NoteEditBarItem(
        id = "textFormat",
        contentDescription = textFormatDescription,
        selected = state.expandedTab == EditTab.TEXT_FORMAT,
        onClick = actions.onTextFormatTabClick,
        icon = {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_text),
            contentDescription = textFormatDescription,
            tint = contentColor,
          )
        },
        bubbleContent = { TextFormatPanel(state, state.activeFormat, contentColor, barColor, actions) },
        bubbleWidth = barMaxWidth,
      ),
    )

    val textColorDescription = stringResource(R.string.acc_select_text_color)
    add(
      NoteEditBarItem(
        id = "textColor",
        contentDescription = textColorDescription,
        selected = state.expandedTab == EditTab.TEXT_COLOR,
        onClick = actions.onTextColorTabClick,
        icon = {
          Text(
            text = "A",
            color = sliderColors.getOrElse(state.colorIndex) { contentColor },
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.titleMedium,
          )
        },
        bubbleContent = {
          TextColorPanel(
            colors = state.sliderColors,
            contentColor = contentColor,
            onColorSelected = actions.onApplySolidColor,
            onGradientSelected = actions.onApplyGradient,
            hapticFeedbackEnabled = state.hapticFeedbackEnabled,
          )
        },
        bubbleWidth = barMaxWidth,
      ),
    )
  }
