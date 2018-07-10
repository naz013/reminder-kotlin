package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotesBinding

import java.util.Locale

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

    private var binding: FragmentSettingsNotesBinding? = null
    private val mNoteReminderClick = { view -> changeNoteReminder() }
    private val mNoteColorRememberClick = { view -> changeNoteColorRemembering() }
    private val mNoteTimeClick = { view -> showTimePickerDialog() }
    private val mNoteTextSizeClick = { view -> showTextSizePickerDialog() }
    private val mNoteColorOpacityClick = { view -> showOpacityPickerDialog() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsNotesBinding.inflate(inflater, container, false)
        initNoteReminderPrefs()
        initNoteTime()
        initTextSizePrefs()
        initNoteColorRememberPrefs()
        initColorOpacityPrefs()
        return binding!!.root
    }

    private fun initNoteColorRememberPrefs() {
        binding!!.noteColorRememberPrefs.setOnClickListener(mNoteColorRememberClick)
        binding!!.noteColorRememberPrefs.isChecked = prefs!!.isNoteColorRememberingEnabled
    }

    private fun changeNoteColorRemembering() {
        val isChecked = binding!!.noteColorRememberPrefs.isChecked
        binding!!.noteColorRememberPrefs.isChecked = !isChecked
        prefs!!.isNoteColorRememberingEnabled = !isChecked
    }

    private fun initColorOpacityPrefs() {
        binding!!.noteColorOpacity.setOnClickListener(mNoteColorOpacityClick)
        showNoteColorSaturation()
    }

    private fun initTextSizePrefs() {
        binding!!.textSize.setOnClickListener(mNoteTextSizeClick)
        showTextSize()
    }

    private fun showTextSize() {
        binding!!.textSize.setDetailText(String.format(Locale.getDefault(), "%d pt", prefs!!.noteTextSize + 12))
    }

    private fun initNoteReminderPrefs() {
        binding!!.noteReminderPrefs.setOnClickListener(mNoteReminderClick)
        binding!!.noteReminderPrefs.isChecked = prefs!!.isNoteReminderEnabled
    }

    private fun initNoteTime() {
        binding!!.noteReminderTime.setOnClickListener(mNoteTimeClick)
        binding!!.noteReminderTime.setDependentView(binding!!.noteReminderPrefs)
        showNoteTime()
    }

    private fun showNoteTime() {
        binding!!.noteReminderTime.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs!!.noteReminderTime.toString()))
    }

    private fun showNoteColorSaturation() {
        binding!!.noteColorOpacity.setDetailText(String.format(Locale.getDefault(), "%d%%", prefs!!.noteColorOpacity))
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.notes))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun changeNoteReminder() {
        val isChecked = binding!!.noteReminderPrefs.isChecked
        binding!!.noteReminderPrefs.isChecked = !isChecked
        prefs!!.isNoteReminderEnabled = !isChecked
    }

    private fun showTextSizePickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.text_size)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val textSize = prefs!!.noteTextSize
        b.seekBar.progress = textSize
        b.titleView.text = String.format(Locale.getDefault(), "%d pt", textSize + 12)
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.noteTextSize = b.seekBar.progress
            showTextSize()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showTimePickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.time)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val time = prefs!!.noteReminderTime
        b.seekBar.progress = time
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                time.toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.noteReminderTime = b.seekBar.progress
            showNoteTime()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showOpacityPickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.color_saturation)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val opacity = prefs!!.noteColorOpacity
        b.seekBar.progress = opacity
        b.titleView.text = String.format(Locale.getDefault(), "%d%%", opacity)
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.noteColorOpacity = b.seekBar.progress
            showNoteColorSaturation()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }
}
