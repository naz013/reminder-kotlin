package com.elementary.tasks.reminder.build.valuedialog.controller

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.speech.SpeechEngineCallback
import com.elementary.tasks.core.speech.SpeechError
import com.elementary.tasks.core.speech.SpeechText
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.core.utils.ui.readText
import com.elementary.tasks.core.utils.ui.singleClick
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.BuilderItemSummaryBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class TextInputController(
  builderItem: BuilderItem<String>,
  private val inputMethodManager: InputMethodManager,
  private val speechEngine: SpeechEngine,
  private val permissionFlow: PermissionFlow
) : AbstractBindingValueController<String, BuilderItemSummaryBinding>(builderItem) {

  private val speechEngineCallback = object : SpeechEngineCallback() {
    override fun onStarted() {
      super.onStarted()
      updateSpeechState(SpeechState.STARTED)
    }

    override fun onStopped() {
      super.onStopped()
      updateSpeechState(SpeechState.IDLE)
      updateValue(binding.inputEditText.readText())
    }

    override fun onSpeechStarted() {
      super.onSpeechStarted()
      updateSpeechState(SpeechState.SPEAKING)
    }

    override fun onSpeechEnded() {
      super.onSpeechEnded()
      updateSpeechState(SpeechState.STOPPED)
    }

    override fun onSpeechError(error: SpeechError) {
      super.onSpeechError(error)
      updateSpeechState(SpeechState.IDLE)
    }

    override fun onSpeechResult(speechText: SpeechText) {
      super.onSpeechResult(speechText)
      binding.inputEditText.clearSections()
      binding.inputEditText.setText(speechText.text)
      speechText.newText?.also { newText ->
        binding.inputEditText.addGradientSection(
          startIndex = newText.startIndex,
          endIndex = newText.endIndex + 1,
          startColor = ContextCompat.getColor(getContext(), R.color.greenAccent),
          endColor = ContextCompat.getColor(getContext(), R.color.redAccent)
        )
        binding.inputEditText.addBoldSection(
          startIndex = newText.startIndex,
          endIndex = newText.endIndex + 1
        )
      }
      binding.inputEditText.setSelection(binding.inputEditText.readText().length)
    }
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemSummaryBinding {
    return BuilderItemSummaryBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.voiceInputFrame.visibleGone(speechEngine.supportsRecognition())
    binding.voiceInputFrame.singleClick { micClick() }
    updateSpeechState(SpeechState.IDLE)

    binding.inputEditText.isFocusableInTouchMode = true
    binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
      } else {
        inputMethodManager.showSoftInput(binding.inputEditText, 0)
      }
    }
    binding.inputEditText.setOnClickListener {
      if (!inputMethodManager.isActive(binding.inputEditText)) {
        inputMethodManager.showSoftInput(binding.inputEditText, 0)
      }
    }
    binding.inputEditText.setImeOptions(EditorInfo.IME_ACTION_DONE)
    binding.inputEditText.setOnKeyListener { _, keyCode, event ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }

    binding.inputEditText.hint = builderItem.title
    binding.inputEditText.onTextChanged {
      if (!speechEngine.isStarted()) {
        updateValue(it)
        speechEngine.setText(it ?: "")
      }
    }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    binding.inputEditText.setText(data)
  }

  override fun onStop() {
    super.onStop()
    speechEngine.stopListening()
  }

  private fun micClick() {
    if (speechEngine.isStarted()) {
      speechEngine.stopListening()
    } else {
      permissionFlow.askPermission(Permissions.RECORD_AUDIO) {
        speechEngine.startListening(speechEngineCallback)
      }
    }
  }

  private fun updateSpeechState(state: SpeechState) {
    when (state) {
      SpeechState.IDLE -> {
        binding.voiceInputMic.visible()
        binding.voiceSpeakAnimation.gone()
        binding.voiceInputStop.gone()
      }
      SpeechState.STARTED -> {
        binding.voiceInputMic.gone()
        binding.voiceSpeakAnimation.gone()
        binding.voiceInputStop.visible()
      }
      SpeechState.SPEAKING -> {
        binding.voiceInputMic.gone()
        binding.voiceSpeakAnimation.visible()
        binding.voiceInputStop.gone()
      }
      SpeechState.STOPPED -> {
        binding.voiceInputMic.gone()
        binding.voiceSpeakAnimation.gone()
        binding.voiceInputStop.visible()
      }
    }
  }

  private enum class SpeechState {
    IDLE,
    STARTED,
    SPEAKING,
    STOPPED
  }
}
