package com.elementary.tasks.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotesBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import java.util.Locale

class NoteSettingsFragment : BaseSettingsFragment<FragmentSettingsNotesBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsNotesBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initNoteColorRememberPrefs()
    initColorOpacityPrefs()
    initFontSizePrefs()
    initFontStylePrefs()
  }

  private fun initFontSizePrefs() {
    binding.noteFontSizeRememberPrefs.setOnClickListener { changeFontSizePrefs() }
    binding.noteFontSizeRememberPrefs.isChecked = prefs.isNoteFontSizeRememberingEnabled
  }

  private fun changeFontSizePrefs() {
    prefs.isNoteFontSizeRememberingEnabled = !prefs.isNoteFontSizeRememberingEnabled
    binding.noteFontSizeRememberPrefs.isChecked = prefs.isNoteFontSizeRememberingEnabled
  }

  private fun initFontStylePrefs() {
    binding.noteFontStyleRememberPrefs.setOnClickListener { changeFontStylePrefs() }
    binding.noteFontStyleRememberPrefs.isChecked = prefs.isNoteFontStyleRememberingEnabled
  }

  private fun changeFontStylePrefs() {
    prefs.isNoteFontStyleRememberingEnabled = !prefs.isNoteFontStyleRememberingEnabled
    binding.noteFontStyleRememberPrefs.isChecked = prefs.isNoteFontStyleRememberingEnabled
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

  private fun showNoteColorSaturation() {
    binding.noteColorOpacity.setDetailText(
      String.format(
        Locale.getDefault(),
        "%d%%",
        prefs.noteColorOpacity
      )
    )
  }

  override fun getTitle(): String = getString(R.string.notes)

  private fun showOpacityPickerDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.color_saturation)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = String.format(Locale.getDefault(), "%d%%", value.toInt())
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 100f

      val opacity = prefs.noteColorOpacity
      b.seekBar.value = opacity.toFloat()

      b.titleView.text = String.format(Locale.getDefault(), "%d%%", opacity)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
        prefs.noteColorOpacity = b.seekBar.value.toInt()
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
