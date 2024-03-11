package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.dialogs.DialogSelectExtraBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.fromReminder
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.databinding.ViewTuneExtraBinding

class TuneExtraView : LinearLayout {

  private lateinit var binding: ViewTuneExtraBinding
  var onExtraUpdateListener: ((extra: Extra) -> Unit)? = null
  var extra: Extra = Extra()
    set(value) {
      field = value
      if (!value.useGlobal) {
        field = value
        binding.text.text = fromExtra(value)
        onExtraUpdateListener?.invoke(value)
      } else {
        noExtra()
      }
    }
  var hint: String = ""
  var hasAutoExtra: Boolean = false
    set(value) {
      field = value
      binding.text.text = fromExtra(extra)
      onExtraUpdateListener?.invoke(extra)
    }
  var dialogues: Dialogues? = null

  private val customizationView: DialogSelectExtraBinding
    get() {
      val binding = DialogSelectExtraBinding(
        LayoutInflater.from(context).inflate(R.layout.dialog_select_extra, null)
      )
      binding.extraSwitch.setOnCheckedChangeListener { _, isChecked ->
        binding.autoCheck.isEnabled = !isChecked
        binding.repeatCheck.isEnabled = !isChecked
        binding.unlockCheck.isEnabled = !isChecked
        binding.vibrationCheck.isEnabled = !isChecked
        binding.voiceCheck.isEnabled = !isChecked
      }
      binding.voiceCheck.isChecked = extra.notifyByVoice
      binding.vibrationCheck.isChecked = extra.vibrate
      binding.unlockCheck.isChecked = extra.unlock
      binding.repeatCheck.isChecked = extra.repeatNotification
      binding.autoCheck.isChecked = extra.auto
      binding.extraSwitch.isChecked = extra.useGlobal

      binding.autoCheck.isEnabled = !extra.useGlobal
      binding.repeatCheck.isEnabled = !extra.useGlobal
      binding.unlockCheck.isEnabled = !extra.useGlobal
      binding.vibrationCheck.isEnabled = !extra.useGlobal
      binding.voiceCheck.isEnabled = !extra.useGlobal
      if (Module.is10) {
        binding.autoCheck.gone()
        binding.unlockCheck.gone()
      } else {
        if (hasAutoExtra && hint != "") {
          binding.autoCheck.visibility = View.VISIBLE
          binding.autoCheck.text = hint
        } else {
          binding.autoCheck.visibility = View.GONE
        }
      }
      return binding
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  private fun fromExtra(extra: Extra): String {
    if (extra.useGlobal) {
      return context.getString(R.string.default_string)
    }
    var res = ""
    res += toSign(extra.vibrate)
    res += toSign(extra.notifyByVoice)
    if (!Module.is10) {
      res += toSign(extra.unlock)
    }
    res += toSign(extra.repeatNotification)
    if (!Module.is10) {
      if (hasAutoExtra) res += toSign(extra.auto)
    }
    res = res.trim()
    if (res.endsWith(",")) res = res.substring(0, res.length - 1)
    return res
  }

  private fun toSign(b: Boolean): String {
    return if (b) {
      "[+], "
    } else {
      "[-], "
    }
  }

  private fun noExtra() {
    binding.text.text = context.getString(R.string.default_string)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_tune_extra, this)
    orientation = VERTICAL
    binding = ViewTuneExtraBinding.bind(this)

    binding.selectButton.setOnClickListener {
      openCustomizationDialog()
    }
    extra = Extra().fromReminder(Reminder())
  }

  private fun openCustomizationDialog() {
    val dialogues = dialogues ?: return
    val builder = dialogues.getMaterialDialog(context)
    builder.setTitle(R.string.personalization)
    val b = customizationView
    builder.setView(b.view)
    builder.setPositiveButton(R.string.ok) { _, _ -> saveExtraResults(b) }
    builder.create().show()
  }

  private fun saveExtraResults(b: DialogSelectExtraBinding) {
    val extra = extra
    extra.useGlobal = b.extraSwitch.isChecked
    extra.auto = b.autoCheck.isChecked
    extra.unlock = b.unlockCheck.isChecked
    extra.repeatNotification = b.repeatCheck.isChecked
    extra.notifyByVoice = b.voiceCheck.isChecked
    extra.vibrate = b.vibrationCheck.isChecked
    this.extra = extra
  }

  data class Extra(
    var useGlobal: Boolean = false,
    var vibrate: Boolean = false,
    var repeatNotification: Boolean = false,
    var notifyByVoice: Boolean = false,
    var unlock: Boolean = false,
    var auto: Boolean = false
  )
}
