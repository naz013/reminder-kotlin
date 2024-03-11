package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.os.datapicker.MelodyPicker
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.SoundStackHolder
import com.elementary.tasks.core.utils.ui.transparent
import com.elementary.tasks.core.utils.ui.visibleInvisible
import com.elementary.tasks.databinding.BuilderItemMelodyBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class MelodyController(
  builderItem: BuilderItem<String>,
  private val melodyPicker: MelodyPicker,
  private val soundStackHolder: SoundStackHolder
) : AbstractBindingValueController<String, BuilderItemMelodyBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemMelodyBinding {
    return BuilderItemMelodyBinding.inflate(layoutInflater, parent, false)
  }

  override fun onDestroy() {
    super.onDestroy()
    stopPlayback()
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.playPauseButton.transparent()
    binding.playPauseButton.setOnClickListener {
      if (soundStackHolder.sound?.isPlaying == true) {
        stopPlayback()
      } else {
        builderItem.modifier.getValue()?.also { path ->
          soundStackHolder.initParams()
          soundStackHolder.sound?.play(path)
        }
      }
    }

    binding.removeButton.setOnClickListener {
      stopPlayback()
      updateValue(null)
      updatePlayButtonVisibility()
      updateFileName()
    }

    binding.selectButton.setOnClickListener {
      melodyPicker.pickMelody { path ->
        updateValue(path)
        updatePlayButtonVisibility()
        updateFileName()
      }
    }

    soundStackHolder.playbackCallback = object : Sound.PlaybackCallback {
      override fun onFinish() {
        binding.playPauseButton.setIconResource(R.drawable.ic_builder_play_melody)
      }

      override fun onStart() {
        binding.playPauseButton.setIconResource(R.drawable.ic_builder_stop_melody)
      }
    }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    updatePlayButtonVisibility()
    updatePlayPauseButton()
    updateFileName()
  }

  private fun stopPlayback() {
    if (soundStackHolder.sound?.isPlaying == true) {
      soundStackHolder.sound?.stop(true)
    }
  }

  private fun updateFileName() {
    binding.melodyTitleView.text = builderItem.modifier.getValue()
      ?: getContext().getString(R.string.not_selected)
  }

  private fun updatePlayButtonVisibility() {
    binding.playPauseButton.visibleInvisible(!builderItem.modifier.getValue().isNullOrEmpty())
    binding.removeButton.visibleInvisible(!builderItem.modifier.getValue().isNullOrEmpty())
  }

  private fun updatePlayPauseButton() {
    if (soundStackHolder.sound?.isPlaying == true) {
      binding.playPauseButton.setIconResource(R.drawable.ic_builder_stop_melody)
    } else {
      binding.playPauseButton.setIconResource(R.drawable.ic_builder_play_melody)
    }
  }
}
