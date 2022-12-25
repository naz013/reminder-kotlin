package com.elementary.tasks.core.dialogs

import android.os.Bundle
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding

class VolumeDialog : BaseDialog() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.loudness)
    val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater, null, false)
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
    val loudness = prefs.loudness
    b.seekBar.progress = loudness
    b.titleView.text = loudness.toString()
    builder.setView(b.root)
    builder.setPositiveButton(R.string.ok) { _, _ -> prefs.loudness = b.seekBar.progress }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.setOnCancelListener { finish() }
    dialog.setOnDismissListener { finish() }
    dialog.show()
    Dialogues.setFullWidthDialog(dialog, this)
  }
}
