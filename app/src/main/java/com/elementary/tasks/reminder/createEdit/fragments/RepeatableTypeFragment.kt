package com.elementary.tasks.reminder.createEdit.fragments

import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.views.roboto.RoboTextView
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*

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
abstract class RepeatableTypeFragment : TypeFragment() {

    protected fun changeLimit() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.repeat_limit)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
        b.seekBar.max = 366
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setRepeatTitle(b.titleView, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = if (reminderInterface!!.repeatLimit != -1) reminderInterface!!.repeatLimit else 0
        setRepeatTitle(b.titleView, reminderInterface!!.repeatLimit)
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ -> saveLimit(b.seekBar.progress) }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun setRepeatTitle(textView: RoboTextView, progress: Int) {
        when {
            progress <= 0 -> textView.text = getString(R.string.no_limits)
            progress == 1 -> textView.setText(R.string.once)
            else -> textView.text = progress.toString() + " " + getString(R.string.times)
        }
    }

    protected fun getZeroedInt(v: Int): String {
        return if (v < 9) {
            "0$v"
        } else {
            v.toString()
        }
    }

    private fun saveLimit(progress: Int) {
        var repeatLimit = progress
        if (progress == 0) repeatLimit = -1
        reminderInterface!!.repeatLimit = repeatLimit
    }
}
