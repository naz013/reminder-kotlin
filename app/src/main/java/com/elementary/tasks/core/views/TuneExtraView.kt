package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.fromReminder
import kotlinx.android.synthetic.main.dialog_select_extra.view.*
import kotlinx.android.synthetic.main.view_group.view.*

/**
 * Copyright 2018 Nazar Suhovich
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
class TuneExtraView : LinearLayout {

    var onExtraUpdateListener: ((extra: Extra) -> Unit)? = null
    var extra: Extra = Extra()
        set(value) {
            field = value
            if (!value.useGlobal) {
                field = value
                text.text = fromExtra(value)
                onExtraUpdateListener?.invoke(value)
            } else {
                noExtra()
            }
        }
    var hint: String = ""
    var hasAutoExtra: Boolean = false
        set(value) {
            field = value
            text.text = fromExtra(extra)
            onExtraUpdateListener?.invoke(extra)
        }
    var dialogues: Dialogues?  = null

    private val customizationView: View
        get() {
            val binding = LayoutInflater.from(context).inflate(R.layout.dialog_select_extra, null)
            binding.extraSwitch.setOnCheckedChangeListener { _, isChecked ->
                binding.autoCheck.isEnabled = !isChecked
                binding.repeatCheck.isEnabled = !isChecked
                binding.unlockCheck.isEnabled = !isChecked
                binding.vibrationCheck.isEnabled = !isChecked
                binding.voiceCheck.isEnabled = !isChecked
                binding.wakeCheck.isEnabled = !isChecked
            }
            binding.voiceCheck.isChecked = extra.notifyByVoice
            binding.vibrationCheck.isChecked = extra.vibrate
            binding.unlockCheck.isChecked = extra.unlock
            binding.repeatCheck.isChecked = extra.repeatNotification
            binding.autoCheck.isChecked = extra.auto
            binding.wakeCheck.isChecked = extra.awake
            binding.extraSwitch.isChecked = extra.useGlobal

            binding.autoCheck.isEnabled = !extra.useGlobal
            binding.repeatCheck.isEnabled = !extra.useGlobal
            binding.unlockCheck.isEnabled = !extra.useGlobal
            binding.vibrationCheck.isEnabled = !extra.useGlobal
            binding.voiceCheck.isEnabled = !extra.useGlobal
            binding.wakeCheck.isEnabled = !extra.useGlobal
            if (hasAutoExtra && hint != "") {
                binding.autoCheck.visibility = View.VISIBLE
                binding.autoCheck.text = hint
            } else {
                binding.autoCheck.visibility = View.GONE
            }
            return binding
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun fromExtra(extra: Extra): String {
        if (extra.useGlobal) {
            return context.getString(R.string.default_string)
        }
        var res = ""
        res += toSign(extra.vibrate)
        res += toSign(extra.notifyByVoice)
        res += toSign(extra.awake)
        res += toSign(extra.unlock)
        res += toSign(extra.repeatNotification)
        if (hasAutoExtra) res += toSign(extra.auto)
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
        text.text = context.getString(R.string.default_string)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_tune_extra, this)
        orientation = LinearLayout.VERTICAL
        text.setOnClickListener {
            openCustomizationDialog()
        }
        extra = Extra().fromReminder(Reminder())
    }

    private fun openCustomizationDialog() {
        val dialogues = dialogues ?: return
        val builder = dialogues.getDialog(context)
        builder.setTitle(R.string.personalization)
        val b = customizationView
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ -> saveExtraResults(b) }
        builder.create().show()
    }

    private fun saveExtraResults(b: View) {
        val extra = extra
        extra.useGlobal = b.extraSwitch.isChecked
        extra.auto = b.autoCheck.isChecked
        extra.awake = b.wakeCheck.isChecked
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
            var awake: Boolean = false,
            var unlock: Boolean = false,
            var auto: Boolean = false
    )
}