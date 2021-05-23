package com.elementary.tasks.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotesBinding
import java.util.*

class NoteSettingsFragment : BaseSettingsFragment<FragmentSettingsNotesBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsNotesBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    initNoteReminderPrefs()
    initNoteTime()
    initTextSizePrefs()
    initNoteColorRememberPrefs()
    initColorOpacityPrefs()
  }

  private fun initNoteColorRememberPrefs() {
    binding.noteColorRememberPrefs.setOnClickListener { changeNoteColorRemembering() }
    binding.noteColorRememberPrefs.isChecked = prefs.isNoteColorRememberingEnabled
  }

  private fun changeNoteColorRemembering() {
    val isChecked = binding.noteColorRememberPrefs.isChecked
    binding.noteColorRememberPrefs.isChecked = !isChecked
    prefs.isNoteColorRememberingEnabled = !isChecked
  }

  private fun initColorOpacityPrefs() {
    binding.noteColorOpacity.setOnClickListener { showOpacityPickerDialog() }
    showNoteColorSaturation()
  }

  private fun initTextSizePrefs() {
    binding.textSize.setOnClickListener { showTextSizePickerDialog() }
    showTextSize()
  }

  private fun showTextSize() {
    binding.textSize.setDetailText(String.format(Locale.getDefault(), "%d pt", prefs.noteTextSize + 12))
  }

  private fun initNoteReminderPrefs() {
    binding.noteReminderPrefs.setOnClickListener { changeNoteReminder() }
    binding.noteReminderPrefs.isChecked = prefs.isNoteReminderEnabled
  }

  private fun initNoteTime() {
    binding.noteReminderTime.setOnClickListener { showTimePickerDialog() }
    binding.noteReminderTime.setDependentView(binding.noteReminderPrefs)
    showNoteTime()
  }

  private fun showNoteTime() {
    binding.noteReminderTime.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
      prefs.noteReminderTime.toString()))
  }

  private fun showNoteColorSaturation() {
    binding.noteColorOpacity.setDetailText(String.format(Locale.getDefault(), "%d%%", prefs.noteColorOpacity))
  }

  override fun getTitle(): String = getString(R.string.notes)

  private fun changeNoteReminder() {
    val isChecked = binding.noteReminderPrefs.isChecked
    binding.noteReminderPrefs.isChecked = !isChecked
    prefs.isNoteReminderEnabled = !isChecked
  }

  private fun showTextSizePickerDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.text_size)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
      b.seekBar.max = 18
      b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          b.titleView.text = String.format(Locale.getDefault(), "%d pt", progress + 12)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
      })
      val textSize = prefs.noteTextSize
      b.seekBar.progress = textSize
      b.titleView.text = String.format(Locale.getDefault(), "%d pt", textSize + 12)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
        prefs.noteTextSize = b.seekBar.progress
        showTextSize()
        dialogInterface.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  private fun showTimePickerDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.time)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
      b.seekBar.max = 120
      b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
            progress.toString())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
      })
      val time = prefs.noteReminderTime
      b.seekBar.progress = time
      b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
        time.toString())
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
        prefs.noteReminderTime = b.seekBar.progress
        showNoteTime()
        dialogInterface.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  private fun showOpacityPickerDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.color_saturation)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
      b.seekBar.max = 100
      b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          b.titleView.text = String.format(Locale.getDefault(), "%d%%", progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
      })
      val opacity = prefs.noteColorOpacity
      b.seekBar.progress = opacity
      b.titleView.text = String.format(Locale.getDefault(), "%d%%", opacity)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
        prefs.noteColorOpacity = b.seekBar.progress
        showNoteColorSaturation()
        dialogInterface.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }
}
