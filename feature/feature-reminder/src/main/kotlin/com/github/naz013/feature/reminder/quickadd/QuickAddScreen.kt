package com.github.naz013.feature.reminder.quickadd

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.naz013.common.Permissions
import com.github.naz013.common.speech.SpeechEngine
import com.github.naz013.common.speech.SpeechEngineCallback
import com.github.naz013.common.speech.SpeechError
import com.github.naz013.common.speech.SpeechText
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetHeader
import com.github.naz013.ui.common.compose.foundation.component.GradientHighlightTextField
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.TextHighlight
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset

private val MIC_BUTTON_SIZE = 48.dp

/** Mirrors `TextInputValueEditor`'s speech UI states - each screen that embeds
 * [com.github.naz013.common.speech.SpeechEngine] keeps its own copy since the engine reports state
 * purely through callbacks, not a shared state holder. */
internal enum class QuickAddSpeechState { IDLE, STARTED, SPEAKING, STOPPED }

/**
 * The quick-add bottom sheet: a single free-text field that [QuickAddViewModel] live-parses,
 * highlighting the recognized date/time/recurrence phrase in [matchedRanges] as the user types.
 * Below it, [preview] shows what will actually be saved (title/date/time/repeat) and lets the user
 * fine-tune the date/time/recurrence the parser guessed before saving. Also supports voice
 * dictation into the text field, mirroring `TextInputValueEditor`'s mic button (idle/recording
 * icons, Lottie waveform while actively speaking, gradient-highlighting the just-recognized speech
 * chunk).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddScreen(
  modifier: Modifier = Modifier,
  input: String,
  matchedRanges: List<IntRange>,
  canSave: Boolean,
  preview: QuickAddViewModel.QuickAddPreview?,
  onInputChange: (String) -> Unit,
  onDateSelected: (LocalDate) -> Unit,
  onTimeSelected: (LocalTime) -> Unit,
  onRepeatOptionSelected: (QuickAddRepeatOption) -> Unit,
  onSaveClick: () -> Unit,
  onEditManuallyClick: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current
  val speechEngine = remember { SpeechEngine(context) }
  val supportsSpeech = remember { speechEngine.supportsRecognition() }
  var speechState by remember { mutableStateOf(QuickAddSpeechState.IDLE) }
  var speechHighlight by remember { mutableStateOf<TextHighlight?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var repeatMenuExpanded by remember { mutableStateOf(false) }

  val greenAccent = colorResource(R.color.greenAccent)
  val redAccent = colorResource(R.color.redAccent)

  val speechCallback = remember {
    object : SpeechEngineCallback() {
      override fun onStarted() {
        speechState = QuickAddSpeechState.STARTED
      }

      override fun onStopped() {
        speechState = QuickAddSpeechState.IDLE
      }

      override fun onSpeechStarted() {
        speechState = QuickAddSpeechState.SPEAKING
      }

      override fun onSpeechEnded() {
        speechState = QuickAddSpeechState.STOPPED
      }

      override fun onSpeechError(error: SpeechError) {
        speechState = QuickAddSpeechState.IDLE
      }

      override fun onSpeechResult(speechText: SpeechText) {
        onInputChange(speechText.text)
        speechHighlight = speechText.newText?.let { newText ->
          TextHighlight(
            range = newText.startIndex..newText.endIndex,
            brush = Brush.horizontalGradient(listOf(greenAccent, redAccent)),
            bold = true,
          )
        }
      }
    }
  }

  val permissionRequester = rememberPermissionRequesterRationale()

  DisposableEffect(speechEngine) {
    onDispose { speechEngine.stopListening() }
  }

  AppModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      BottomSheetHeader(title = stringResource(R.string.quick_add))

      val highlightBrush = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
      )
      val parseHighlights = matchedRanges.map { TextHighlight(range = it, brush = highlightBrush, bold = true) }
      Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        GradientHighlightTextField(
          value = input,
          onValueChange = { newText ->
            speechHighlight = null
            speechEngine.setText(newText)
            onInputChange(newText)
          },
          highlights = parseHighlights + listOfNotNull(speechHighlight),
          textStyle = MaterialTheme.typography.titleMedium,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 56.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .padding(12.dp),
        )
        if (supportsSpeech) {
          Spacer(modifier = Modifier.width(8.dp))
          val micButtonDescription = if (speechState == QuickAddSpeechState.IDLE) {
            stringResource(R.string.cd_start_voice_input)
          } else {
            stringResource(R.string.cd_stop_voice_input)
          }
          TooltipIconButton(contentDescription = micButtonDescription) {
            IconButton(
              modifier = Modifier
                .size(MIC_BUTTON_SIZE)
                .semantics { contentDescription = micButtonDescription },
              onClick = {
                if (speechEngine.isStarted()) {
                  speechEngine.stopListening()
                } else {
                  permissionRequester.request(
                    Permissions.RECORD_AUDIO,
                    onGranted = { speechEngine.startListening(speechCallback) },
                  )
                }
              },
            ) {
              when (speechState) {
                QuickAddSpeechState.SPEAKING -> {
                  val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.mic_speaking_waves),
                  )
                  LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(MIC_BUTTON_SIZE),
                  )
                }

                QuickAddSpeechState.STARTED, QuickAddSpeechState.STOPPED -> {
                  Icon(
                    painter = AppIcons.Fluent.RecordingStop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                  )
                }

                QuickAddSpeechState.IDLE -> {
                  Icon(
                    painter = AppIcons.Builder.MicOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }
      }

      if (preview != null) {
        QuickAddPreviewSection(
          preview = preview,
          onDateClick = { showDatePicker = true },
          onTimeClick = { showTimePicker = true },
          onRepeatClick = { repeatMenuExpanded = true },
          repeatMenuExpanded = repeatMenuExpanded,
          onRepeatMenuDismiss = { repeatMenuExpanded = false },
          onRepeatOptionSelected = {
            repeatMenuExpanded = false
            onRepeatOptionSelected(it)
          },
        )
      } else if (input.isEmpty()) {
        Text(
          text = stringResource(R.string.quick_add_hint),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(onClick = onEditManuallyClick) {
          Text(stringResource(R.string.quick_add_continue_in_editor))
        }
        Button(onClick = onSaveClick, enabled = canSave) {
          Text(stringResource(R.string.save))
        }
      }
    }
  }

  if (showDatePicker && preview != null) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = preview.startDateTime.toLocalDate().toUtcMillis(),
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { onDateSelected(it.toUtcLocalDate()) }
            showDatePicker = false
          },
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text(stringResource(R.string.cancel))
        }
      },
    ) {
      DatePicker(state = datePickerState, showModeToggle = false)
    }
  }

  if (showTimePicker && preview != null) {
    val timePickerState = rememberTimePickerState(
      initialHour = preview.startDateTime.hour,
      initialMinute = preview.startDateTime.minute,
    )
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            showTimePicker = false
          },
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showTimePicker = false }) {
          Text(stringResource(R.string.cancel))
        }
      },
      text = { TimePicker(state = timePickerState) },
    )
  }
}

@Composable
private fun QuickAddPreviewSection(
  preview: QuickAddViewModel.QuickAddPreview,
  onDateClick: () -> Unit,
  onTimeClick: () -> Unit,
  onRepeatClick: () -> Unit,
  repeatMenuExpanded: Boolean,
  onRepeatMenuDismiss: () -> Unit,
  onRepeatOptionSelected: (QuickAddRepeatOption) -> Unit,
) {
  Column(modifier = Modifier.padding(top = 8.dp)) {
    SettingsItem(
      title = stringResource(R.string.builder_date),
      icon = AppIcons.Fluent.Calendar,
      trailing = { Text(preview.dateText, style = MaterialTheme.typography.titleMedium) },
      onClick = onDateClick,
    )
    SettingsItem(
      title = stringResource(R.string.time),
      icon = AppIcons.Builder.Time,
      trailing = { Text(preview.timeText, style = MaterialTheme.typography.titleMedium) },
      onClick = onTimeClick,
    )
    Box {
      SettingsItem(
        title = stringResource(R.string.repeat),
        icon = AppIcons.Fluent.ArrowRepeatAll,
        trailing = { Text(preview.repeatText, style = MaterialTheme.typography.titleMedium) },
        onClick = onRepeatClick,
      )
      val repeatOptionLabels = listOf(
        QuickAddRepeatOption.NONE to stringResource(R.string.quick_add_does_not_repeat),
        QuickAddRepeatOption.DAILY to stringResource(R.string.recur_daily),
        QuickAddRepeatOption.WEEKLY to stringResource(R.string.recur_weekly),
        QuickAddRepeatOption.MONTHLY to stringResource(R.string.recur_monthly),
        QuickAddRepeatOption.YEARLY to stringResource(R.string.recur_yearly),
      )
      AppDropdownMenu(
        expanded = repeatMenuExpanded,
        onDismissRequest = onRepeatMenuDismiss,
        items = repeatOptionLabels.mapIndexed { index, (_, label) ->
          PopupMenuItem(id = index, title = label, iconRes = DrawableCatalog.Fluent.ArrowRepeatAll)
        },
        onItemClick = { id -> onRepeatOptionSelected(repeatOptionLabels[id].first) },
      )
    }
  }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
