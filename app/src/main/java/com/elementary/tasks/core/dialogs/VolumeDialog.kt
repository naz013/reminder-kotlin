package com.elementary.tasks.core.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.SeekBar

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding

/**
 * Copyright 2017 Nazar Suhovich
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

class VolumeDialog : BaseDialog() {

    private val mCancelListener = { dialogInterface -> finish() }
    private val mOnDismissListener = { dialogInterface -> finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.loudness)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(this))
        b.seekBar.max = 25
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        val loudness = Prefs.getInstance(this).loudness
        b.seekBar.progress = loudness
        b.titleView.text = loudness.toString()
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialog, which -> Prefs.getInstance(this).loudness = b.seekBar.progress }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        val alertDialog = builder.create()
        alertDialog.setOnCancelListener(mCancelListener)
        alertDialog.setOnDismissListener(mOnDismissListener)
        alertDialog.show()
    }
}
