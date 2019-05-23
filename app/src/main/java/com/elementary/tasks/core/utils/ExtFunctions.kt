package com.elementary.tasks.core.utils

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.views.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.*

fun <T> ViewModel.mutableLiveDataOf() = MutableLiveData<T>()

fun <T : ViewDataBinding> FragmentActivity.activityBinding(@LayoutRes resId: Int) = ActivityBindingProperty<T>(resId)

fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}

fun <ViewT : View> View.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnSynchronized {
        findViewById<ViewT>(idRes)
    }
}

fun <ViewT : View> Activity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnSynchronized {
        findViewById<ViewT>(idRes)
    }
}

fun Date?.toHm(): TimeUtil.HM {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    if (this != null) calendar.time = this
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return TimeUtil.HM(hour, minute)
}

fun TimeUtil.HM.toDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    return calendar.time
}

fun TimeUtil.HM.toMillis(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    return calendar.timeInMillis
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isNotVisible(): Boolean = visibility == View.INVISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.transparent() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun <T> lazyUnSynchronized(initializer: () -> T): Lazy<T> =
        lazy(LazyThreadSafetyMode.NONE, initializer)

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T)
        : T = withContext(Dispatchers.Main, block)

fun launchDefault(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit)
        : Job = GlobalScope.launch(Dispatchers.Default, start, block)

fun launchIo(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit)
        : Job = GlobalScope.launch(Dispatchers.IO, start, block)

fun EditText.onChanged(function: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            function.invoke(s.toString().trim())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
        }
    })
}

fun Long.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar
}

fun TuneExtraView.Extra.fromReminder(reminder: Reminder): TuneExtraView.Extra {
    this.useGlobal = reminder.useGlobal
    this.vibrate = reminder.vibrate
    this.repeatNotification = reminder.repeatNotification
    this.notifyByVoice = reminder.notifyByVoice
    this.awake = reminder.awake
    this.unlock = reminder.unlock
    this.auto = reminder.auto
    return this
}

fun TuneExtraView.Extra.toReminder(reminder: Reminder): Reminder {
    reminder.useGlobal = this.useGlobal
    reminder.vibrate = this.vibrate
    reminder.repeatNotification = this.repeatNotification
    reminder.notifyByVoice = this.notifyByVoice
    reminder.awake = this.awake
    reminder.unlock = this.unlock
    reminder.auto = this.auto
    return reminder
}

fun Reminder.copyExtra(reminder: Reminder) {
    this.useGlobal = reminder.useGlobal
    this.vibrate = reminder.vibrate
    this.repeatNotification = reminder.repeatNotification
    this.notifyByVoice = reminder.notifyByVoice
    this.awake = reminder.awake
    this.unlock = reminder.unlock
    this.auto = reminder.auto
}

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
            Timber.d("onChanged: $repeat")
            listener.invoke(repeat)
        }
    }
}

fun BeforePickerView.bindProperty(value: Long, listener: ((Long) -> Unit)?) {
    this.setBefore(value)
    this.onBeforeChangedListener = object : BeforePickerView.OnBeforeChangedListener {
        override fun onChanged(beforeMills: Long) {
            listener?.invoke(beforeMills)
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
    this.content = value
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


fun LedPickerView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
    this.led = value
    this.onLedChangeListener = {
        listener.invoke(it)
    }
}

fun TuneExtraView.bindProperty(value: Reminder, listener: ((Reminder) -> Unit)) {
    this.extra = TuneExtraView.Extra().fromReminder(value)
    this.onExtraUpdateListener = {
        listener.invoke(it.toReminder(value))
    }
}

fun ExclusionPickerView.bindProperty(v1: List<Int>, v2: String, v3: String, listener: ((List<Int>, String, String) -> Unit)) {
    this.setHours(v1)
    this.setRangeHours(v2, v3)
    this.onExclusionUpdateListener = { a1, a2, a3 ->
        listener.invoke(a1, a2, a3)
    }
}