package com.elementary.tasks.core.utils

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.temp.UI
import com.elementary.tasks.core.views.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.withContext

/**
 * Copyright 2018 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
suspend fun <T> withUIContext(start: CoroutineStart = CoroutineStart.DEFAULT,
                              block: suspend () -> T): T = withContext(UI, start, block)

fun AppCompatEditText.bindProperty(value: String, listener: ((String) -> Unit)) {
    this.setText(value)
    this.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null) {
                listener.invoke(s.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
        }
    })
}

fun AppCompatCheckBox.bindProperty(value: Boolean, listener: ((Boolean) -> Unit)) {
    this.isChecked = value
    this.setOnCheckedChangeListener { _, isChecked -> listener.invoke(isChecked) }
}

fun RepeatView.bindProperty(value: Long, listener: ((Long) -> Unit)) {
    this.repeat = value
    this.onRepeatChangeListener = object : RepeatView.OnRepeatChangeListener {
        override fun onChanged(repeat: Long) {
            listener.invoke(repeat)
        }
    }
}

fun BeforePickerView.bindProperty(value: Long, listener: ((Long) -> Unit)) {
    this.setBefore(value)
    this.onBeforeChangedListener = object : BeforePickerView.OnBeforeChangedListener {
        override fun onChanged(before: Long) {
            listener.invoke(before)
        }
    }
}

fun DateTimeView.bindProperty(value: String, listener: ((String) -> Unit)) {
    this.setDateTime(value)
    this.onDateChangeListener = object : DateTimeView.OnDateChangeListener {
        override fun onChanged(mills: Long) {
            listener.invoke(TimeUtil.getGmtFromDateTime(mills))
        }
    }
}

fun PriorityPickerView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
    this.priority = value
    this.onPriorityChaneListener = {
        listener.invoke(it)
    }
}

fun ActionView.bindProperty(value: String, listener: ((String) -> Unit)) {
    this.number = value
    this.setListener(object : ActionView.OnActionListener {
        override fun onStateChanged(hasAction: Boolean, type: Int, phone: String) {
            listener.invoke(phone)
        }
    })
}

fun MelodyView.bindProperty(value: String, listener: ((String) -> Unit)) {
    this.file = value
    this.onFileUpdateListener = {
        listener.invoke(it)
    }
}

fun AttachmentView.bindProperty(value: String, listener: ((String) -> Unit)) {
    this.file = value
    this.onFileUpdateListener = {
        listener.invoke(it)
    }
}

fun GroupView.bindProperty(value: ReminderGroup, listener: ((ReminderGroup) -> Unit)) {
    this.reminderGroup = value
    this.onGroupUpdateListener = {
        listener.invoke(it)
    }
}

fun LoudnessPickerView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
    this.setVolume(value)
    this.onLevelUpdateListener = {
        listener.invoke(it)
    }
}

fun RepeatLimitView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
    this.setLimit(value)
    this.onLevelUpdateListener = {
        listener.invoke(it)
    }
}

fun WindowTypeView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
    this.windowType = value
    this.onTypeChaneListener = {
        listener.invoke(it)
    }
}