package com.elementary.tasks.core.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.MainThread
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.AttachmentView
import com.elementary.tasks.core.views.BeforePickerView
import com.elementary.tasks.core.views.ExclusionPickerView
import com.elementary.tasks.core.views.ExportToCalendarView
import com.elementary.tasks.core.views.ExportToGoogleTasksView
import com.elementary.tasks.core.views.LedPickerView
import com.elementary.tasks.core.views.PriorityPickerView
import com.elementary.tasks.core.views.RepeatLimitView
import com.elementary.tasks.core.views.RepeatView
import com.elementary.tasks.core.views.TuneExtraView
import com.github.naz013.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.InputStream
import java.util.Calendar

fun <T> LiveData<List<T>>.getNonNullList(): List<T> {
  return value ?: emptyList()
}

fun <K, V> LiveData<Map<K, V>>.getNonNullMap(): Map<K, V> {
  return value ?: emptyMap()
}

fun LocalDateTime.minusMillis(millis: Long): LocalDateTime {
  return minusSeconds(millis / 1000L)
}

fun LocalDateTime.plusMillis(millis: Long): LocalDateTime {
  return plusSeconds(millis / 1000L)
}

fun List<String>.append(): String {
  val stringBuilder = StringBuilder()
  for (string in this) {
    stringBuilder.append(string)
  }
  return stringBuilder.toString()
}

fun listOfNotEmpty(vararg items: String?): List<String> = items.filterNotNull().filterNotEmpty()

fun List<String?>.filterNotEmpty() = filterNotNull().filter { it.isNotEmpty() }

fun String.normalizeSummary(): String {
  return if (length > Configs.MAX_REMINDER_SUMMARY_LENGTH) {
    substring(0, Configs.MAX_REMINDER_SUMMARY_LENGTH)
  } else {
    this
  }
}

@ColorInt
fun Int.adjustAlpha(@IntRange(from = 0, to = 100) factor: Int): Int {
  val alpha = 255f * (factor.toFloat() / 100f)
  val red = android.graphics.Color.red(this)
  val green = android.graphics.Color.green(this)
  val blue = android.graphics.Color.blue(this)
  return android.graphics.Color.argb(alpha.toInt(), red, green, blue)
}

// Check if Color is Dark
fun Int.isColorDark(): Boolean {
  val darkness = 1 - (
    0.299 * android.graphics.Color.red(this) +
      0.587 * android.graphics.Color.green(this) +
      0.114 * android.graphics.Color.blue(this)
    ) / 255
  return darkness >= 0.5
}

// Check of opacity of Color
fun Int.isAlmostTransparent(): Boolean {
  return this < 25
}

fun <T> mutableLiveDataOf() = MutableLiveData<T>()

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this

fun File.copyInputStreamToFile(inputStream: InputStream) {
  inputStream.use { input ->
    this.outputStream().use { fileOut ->
      input.copyTo(fileOut)
    }
  }
}

fun <T> lazyUnSynchronized(initializer: () -> T): Lazy<T> =
  lazy(LazyThreadSafetyMode.NONE, initializer)

suspend fun <T> withUIContext(
  block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.Main, block)

@Deprecated("Use class scope for coroutine")
fun launchDefault(
  start: CoroutineStart = CoroutineStart.DEFAULT,
  block: suspend CoroutineScope.() -> Unit
): Job = GlobalScope.launch(Dispatchers.Default, start, block)

@Deprecated("Use class scope for coroutine")
fun launchIo(
  start: CoroutineStart = CoroutineStart.DEFAULT,
  block: suspend CoroutineScope.() -> Unit
): Job = GlobalScope.launch(Dispatchers.IO, start, block)

@Deprecated("Will be removed in the future")
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

@Deprecated("Will be removed in the future")
fun TuneExtraView.Extra.fromReminder(reminder: Reminder): TuneExtraView.Extra {
  this.useGlobal = reminder.useGlobal
  this.vibrate = reminder.vibrate
  this.repeatNotification = reminder.repeatNotification
  this.notifyByVoice = reminder.notifyByVoice
  return this
}

@Deprecated("Will be removed in the future")
fun TuneExtraView.Extra.toReminder(reminder: Reminder): Reminder {
  reminder.useGlobal = this.useGlobal
  reminder.vibrate = this.vibrate
  reminder.repeatNotification = this.repeatNotification
  reminder.notifyByVoice = this.notifyByVoice
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

@Deprecated("Will be removed in the future")
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

@Deprecated("Will be removed in the future")
fun ExportToCalendarView.bindProperty(
  enabled: Boolean,
  calendarId: Long,
  listener: ((Boolean, Long) -> Unit)
) {
  this.calendarState = if (enabled) {
    ExportToCalendarView.State.YES
  } else {
    ExportToCalendarView.State.NO
  }
  this.calendarId = calendarId
  this.listener = object : ExportToCalendarView.SelectionListener {
    override fun onChanged(enabled: Boolean, calendarId: Long) {
      listener.invoke(enabled, calendarId)
    }
  }
}

@Deprecated("Will be removed in the future")
fun ExportToGoogleTasksView.bindProperty(
  enabled: Boolean,
  listId: String?,
  listener: ((Boolean, String) -> Unit)
) {
  this.tasksState = if (enabled) {
    ExportToGoogleTasksView.State.YES
  } else {
    ExportToGoogleTasksView.State.NO
  }
  this.taskListId = listId
  this.listener = object : ExportToGoogleTasksView.SelectionListener {
    override fun onChanged(enabled: Boolean, taskListId: String) {
      listener.invoke(enabled, taskListId)
    }
  }
}

@Deprecated("Will be removed in the future")
fun RepeatView.bindProperty(value: Long, listener: ((Long) -> Unit)) {
  this.repeat = value
  this.onRepeatChangeListener = object : RepeatView.OnRepeatChangeListener {
    override fun onChanged(repeat: Long) {
      Logger.d("onChanged: $repeat")
      listener.invoke(repeat)
    }
  }
}

@Deprecated("Will be removed in the future")
fun BeforePickerView.bindProperty(value: Long, listener: ((Long) -> Unit)?) {
  this.setBefore(value)
  this.onBeforeChangedListener = object : BeforePickerView.OnBeforeChangedListener {
    override fun onChanged(beforeMills: Long) {
      listener?.invoke(beforeMills)
    }
  }
}

@Deprecated("Will be removed in the future")
fun PriorityPickerView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
  this.priority = value
  this.onPriorityChaneListener = {
    listener.invoke(it)
  }
}

@Deprecated("Will be removed in the future")
fun ActionView.bindProperty(value: String, listener: ((String) -> Unit)) {
  this.number = value
  this.setListener(object : ActionView.OnActionListener {
    override fun onStateChanged(state: ActionView.ActionState, phone: String) {
      listener.invoke(phone)
    }
  })
}

@Deprecated("Will be removed in the future")
fun AttachmentView.bindProperty(value: String, listener: ((String) -> Unit)) {
  this.content = value
  this.onFileUpdateListener = {
    listener.invoke(it)
  }
}

@Deprecated("Will be removed in the future")
fun RepeatLimitView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
  this.setLimit(value)
  this.onLevelUpdateListener = {
    listener.invoke(it)
  }
}

@Deprecated("Will be removed in the future")
fun LedPickerView.bindProperty(value: Int, listener: ((Int) -> Unit)) {
  this.led = value
  this.onLedChangeListener = {
    listener.invoke(it)
  }
}

@Deprecated("Will be removed in the future")
fun TuneExtraView.bindProperty(value: Reminder, listener: ((Reminder) -> Unit)) {
  this.extra = TuneExtraView.Extra().fromReminder(value)
  this.onExtraUpdateListener = {
    listener.invoke(it.toReminder(value))
  }
}

@Deprecated("Will be removed in the future")
fun ExclusionPickerView.bindProperty(
  v1: List<Int>,
  v2: String,
  v3: String,
  listener: ((List<Int>, String, String) -> Unit)
) {
  this.setHours(v1)
  this.setRangeHours(v2, v3)
  this.onExclusionUpdateListener = { a1, a2, a3 ->
    listener.invoke(a1, a2, a3)
  }
}

fun <T> Calendar.map(func: (Calendar) -> T): T {
  return func.invoke(this)
}

fun <T> LiveData<T>.nonNullObserve(owner: LifecycleOwner, observer: Observer<T>) {
  this.observe(owner) { o: T? ->
    if (o != null) {
      observer.onChanged(o)
    }
  }
}

fun <T> LiveData<out T?>.nullObserve(owner: LifecycleOwner, observer: Observer<T>) {
  this.observe(owner) { o: T? ->
    if (o != null) {
      observer.onChanged(o)
    }
  }
}

@MainThread
@CheckResult
fun <X, Y> LiveData<X?>.mapNullable(
  transform: (@JvmSuppressWildcards X) -> (@JvmSuppressWildcards Y)
): LiveData<Y> {
  val result = MediatorLiveData<Y>()
  result.addSource(this) { x ->
    if (x != null) {
      result.value = transform(x)
    }
  }
  return result
}
