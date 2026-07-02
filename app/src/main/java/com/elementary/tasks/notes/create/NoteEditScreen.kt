package com.elementary.tasks.notes.create

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.io.AssetsUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
  state: NoteEditState,
  speechState: SpeechUiState,
  supportsSpeech: Boolean,
  textFieldValue: TextFieldValue,
  onTextFieldValueChange: (TextFieldValue) -> Unit,
  boldRange: IntRange?,
  backgroundColor: Color,
  contentColor: Color,
  sliderColors: IntArray,
  activeDialog: NoteEditDialog?,
  colorsForPalette: (Int) -> IntArray,
  actions: NoteEditActions,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current
  val canDelete = state.isNoteEdited && !state.isFromFile

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundColor)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = actions.onBackClick) {
            Icon(
              painter = painterResource(R.drawable.ic_builder_arrow_left),
              contentDescription = null,
              tint = contentColor
            )
          }
        },
        title = {},
        actions = {
          IconButton(onClick = actions.onSaveClick) {
            Icon(
              painter = painterResource(R.drawable.ic_fluent_checkmark),
              contentDescription = stringResource(R.string.save),
              tint = contentColor
            )
          }
          IconButton(onClick = actions.onShareClick) {
            Icon(
              painter = painterResource(R.drawable.ic_fluent_share_android),
              contentDescription = stringResource(R.string.share),
              tint = contentColor
            )
          }
          if (canDelete) {
            IconButton(onClick = actions.onDeleteClick) {
              Icon(
                painter = painterResource(R.drawable.ic_fluent_delete),
                contentDescription = stringResource(R.string.delete),
                tint = contentColor
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color.Transparent,
          navigationIconContentColor = contentColor,
          actionIconContentColor = contentColor,
          titleContentColor = contentColor
        )
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
          }
          .padding(horizontal = 16.dp)
      ) {
        val context = LocalContext.current
        val fontFamily = remember(state.fontStyle) {
          AssetsUtil.getTypeface(context, state.fontStyle)?.let { FontFamily(it) }
            ?: FontFamily.Default
        }
        TextField(
          value = textFieldValue,
          onValueChange = onTextFieldValueChange,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = contentColor,
            fontSize = state.fontSize.sp,
            fontFamily = fontFamily
          ),
          placeholder = { Text(stringResource(R.string.note)) },
          visualTransformation = boldRangeVisualTransformation(boldRange),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = contentColor,
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor
          )
        )

        NoteEditImageGrid(
          images = state.images,
          onImageClick = actions.onImageOpen,
          onRemoveClick = actions.onImageRemove,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        )

        // Reserve space so the floating bottom bar never overlaps the last content row.
        Box(modifier = Modifier.height(112.dp))
      }
    }

    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .wrapContentHeight(),
      shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
      color = backgroundColor,
      tonalElevation = 4.dp,
      shadowElevation = 4.dp
    ) {
      NoteEditBottomBar(
        state = state,
        speechState = speechState,
        supportsSpeech = supportsSpeech,
        contentColor = contentColor,
        sliderColors = sliderColors,
        actions = actions,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
      )
    }
  }

  when (activeDialog) {
    NoteEditDialog.FONT_STYLE -> FontStyleDialog(
      selected = state.fontStyle,
      onDismiss = actions.onDialogDismiss,
      onSelected = actions.onFontStyleSelected
    )

    NoteEditDialog.PALETTE -> PaletteDialog(
      currentPalette = state.palette,
      colorsForPalette = colorsForPalette,
      onDismiss = actions.onDialogDismiss,
      onConfirm = actions.onPaletteSelected
    )

    NoteEditDialog.DELETE -> DeleteNoteDialog(
      onDismiss = actions.onDialogDismiss,
      onConfirm = actions.onDeleteConfirmed
    )

    NoteEditDialog.SAME_NOTE -> SameNoteDialog(
      onDismiss = actions.onDialogDismiss,
      onKeep = actions.onSameNoteKeep,
      onReplace = actions.onSameNoteReplace
    )

    null -> Unit
  }
}

private fun boldRangeVisualTransformation(range: IntRange?): VisualTransformation =
  VisualTransformation { text ->
    if (range == null || range.first < 0 || range.last >= text.length || range.first > range.last) {
      TransformedText(text, OffsetMapping.Identity)
    } else {
      val annotated = AnnotatedString.Builder(text).apply {
        addStyle(SpanStyle(fontWeight = FontWeight.Bold), range.first, range.last + 1)
      }.toAnnotatedString()
      TransformedText(annotated, OffsetMapping.Identity)
    }
  }
