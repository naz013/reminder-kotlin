package com.elementary.tasks.core.dialogs

import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding

class VolumeDialog : BaseDialog() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.loudness)
    val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater, null, false)

    val loudness = prefs.loudness
    b.seekBar.addOnChangeListener { _, value, _ ->
      b.titleView.text = value.toInt().toString()
    }
    b.seekBar.stepSize = 1f
    b.seekBar.valueFrom = 0f
    b.seekBar.valueTo = 25f
    b.seekBar.value = loudness.toFloat()

    b.titleView.text = loudness.toString()

    builder.setView(b.root)
    builder.setPositiveButton(R.string.ok) { _, _ -> prefs.loudness = b.seekBar.value.toInt() }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.setOnCancelListener { finish() }
    dialog.setOnDismissListener { finish() }
    dialog.show()
    Dialogues.setFullWidthDialog(dialog, this)
  }
}
