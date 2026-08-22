package com.github.naz013.feature.reminder.build.valuedialog.editor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.common.speech.SpeechEngine
import com.github.naz013.common.speech.SpeechEngineCallback
import com.github.naz013.common.speech.SpeechError
import com.github.naz013.common.speech.SpeechText
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.GradientHighlightTextField
import com.github.naz013.ui.common.compose.foundation.component.TextHighlight

private const val MAX_CHARACTERS = 1000
private val MIC_BUTTON_SIZE = 40.dp

private enum class SpeechUiState { IDLE, STARTED, SPEAKING, STOPPED }

/**
 * Multiline text field with a mic button for live speech-to-text (the just-recognized portion is
 * gradient-highlighted, mirroring the legacy Shader-span behavior). Replaces `TextInputController`.
 *
 * The Lottie waveform, mic idle/stop icons, and the gradient gap between recognized text are all
 * reused as-is; only the field's decoration is intentionally simplified from a Material
 * `TextInputLayout` outline to a plain bordered box, since [GradientHighlightTextField] is a bare
 * `BasicTextField` under the hood (no Material chrome).
 */
@Composable
internal fun TextInputValueEditor(
  builderItem: BuilderItem<String>,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val context = LocalContext.current
  val speechEngine = remember { SpeechEngine(context) }
  val supportsSpeech = remember { speechEngine.supportsRecognition() }

  var text by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue() ?: "") }
  var highlight by remember(builderItem) { mutableStateOf<TextHighlight?>(null) }
  var speechState by remember { mutableStateOf(SpeechUiState.IDLE) }

  val greenAccent = colorResource(R.color.greenAccent)
  val redAccent = colorResource(R.color.redAccent)

  val callback = remember {
    object : SpeechEngineCallback() {
      override fun onStarted() {
        speechState = SpeechUiState.STARTED
      }

      override fun onStopped() {
        speechState = SpeechUiState.IDLE
        builderItem.modifier.update(text)
        onValueChange(builderItem)
      }

      override fun onSpeechStarted() {
        speechState = SpeechUiState.SPEAKING
      }

      override fun onSpeechEnded() {
        speechState = SpeechUiState.STOPPED
      }

      override fun onSpeechError(error: SpeechError) {
        speechState = SpeechUiState.IDLE
      }

      override fun onSpeechResult(speechText: SpeechText) {
        text = speechText.text
        highlight = speechText.newText?.let { newText ->
          TextHighlight(
            range = newText.startIndex..newText.endIndex,
            brush = Brush.horizontalGradient(listOf(greenAccent, redAccent)),
            bold = true,
          )
        }
      }
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { granted -> if (granted) speechEngine.startListening(callback) }

  DisposableEffect(speechEngine) {
    onDispose { speechEngine.stopListening() }
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
      GradientHighlightTextField(
        value = text,
        onValueChange = { newText ->
          if (newText.length <= MAX_CHARACTERS) {
            text = newText
            highlight = null
            speechEngine.setText(newText)
            builderItem.modifier.update(newText)
            onValueChange(builderItem)
          }
        },
        highlights = highlight?.let { listOf(it) } ?: emptyList(),
        textStyle = MaterialTheme.typography.titleMedium,
        modifier = Modifier
          .weight(1f)
          .heightIn(min = 96.dp)
          .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
          .padding(12.dp),
      )
      if (supportsSpeech) {
        Spacer(modifier = Modifier.width(8.dp))
        TooltipIconButton(
          contentDescription = if (speechState == SpeechUiState.IDLE) {
            stringResource(R.string.cd_start_voice_input)
          } else {
            stringResource(R.string.cd_stop_voice_input)
          },
        ) {
          IconButton(
            modifier = Modifier.size(MIC_BUTTON_SIZE),
            onClick = {
              when {
                speechEngine.isStarted() -> speechEngine.stopListening()

                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                  PackageManager.PERMISSION_GRANTED
                -> speechEngine.startListening(callback)

                else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            },
          ) {
            when (speechState) {
              SpeechUiState.SPEAKING -> {
                val composition by rememberLottieComposition(
                  LottieCompositionSpec.RawRes(R.raw.mic_speaking_waves),
                )
                LottieAnimation(
                  composition = composition,
                  iterations = LottieConstants.IterateForever,
                  modifier = Modifier.size(MIC_BUTTON_SIZE),
                )
              }

              SpeechUiState.STARTED, SpeechUiState.STOPPED -> {
                Icon(
                  painter = painterResource(R.drawable.ic_fluent_recording_stop),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                )
              }

              SpeechUiState.IDLE -> {
                Icon(
                  painter = painterResource(R.drawable.ic_builder_mic_on),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    }
    Text(
      text = "${text.length}/$MAX_CHARACTERS",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp),
    )
  }
}
