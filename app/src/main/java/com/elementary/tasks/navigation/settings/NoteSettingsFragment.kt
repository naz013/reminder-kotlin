package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_notes.*
import java.util.*

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class NoteSettingsFragment : BaseSettingsFragment() {

    override fun layoutRes(): Int = R.layout.fragment_settings_notes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNoteReminderPrefs()
        initNoteTime()
        initTextSizePrefs()
        initNoteColorRememberPrefs()
        initColorOpacityPrefs()
    }

    private fun initNoteColorRememberPrefs() {
        noteColorRememberPrefs.onClick { changeNoteColorRemembering() }
        noteColorRememberPrefs.isChecked = prefs.isNoteColorRememberingEnabled
    }

    private fun changeNoteColorRemembering() {
        val isChecked = noteColorRememberPrefs.isChecked
        noteColorRememberPrefs.isChecked = !isChecked
        prefs.isNoteColorRememberingEnabled = !isChecked
    }

    private fun initColorOpacityPrefs() {
        noteColorOpacity.onClick { showOpacityPickerDialog() }
        showNoteColorSaturation()
    }

    private fun initTextSizePrefs() {
        textSize.onClick { showTextSizePickerDialog() }
        showTextSize()
    }

    private fun showTextSize() {
        textSize.setDetailText(String.format(Locale.getDefault(), "%d pt", prefs.noteTextSize + 12))
    }

    private fun initNoteReminderPrefs() {
        noteReminderPrefs.onClick { changeNoteReminder() }
        noteReminderPrefs.isChecked = prefs.isNoteReminderEnabled
    }

    private fun initNoteTime() {
        noteReminderTime.onClick { showTimePickerDialog() }
        noteReminderTime.setDependentView(noteReminderPrefs)
        showNoteTime()
    }

    private fun showNoteTime() {
        noteReminderTime.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.noteReminderTime.toString()))
    }

    private fun showNoteColorSaturation() {
        noteColorOpacity.setDetailText(String.format(Locale.getDefault(), "%d%%", prefs.noteColorOpacity))
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.notes))
            callback?.onFragmentSelect(this)
        }
    }

    private fun changeNoteReminder() {
        val isChecked = noteReminderPrefs.isChecked
        noteReminderPrefs.isChecked = !isChecked
        prefs.isNoteReminderEnabled = !isChecked
    }

    private fun showTextSizePickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.text_size)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
            prefs.noteTextSize = b.seekBar.progress
            showTextSize()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showTimePickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.time)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
            prefs.noteReminderTime = b.seekBar.progress
            showNoteTime()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showOpacityPickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.color_saturation)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
            prefs.noteColorOpacity = b.seekBar.progress
            showNoteColorSaturation()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}
