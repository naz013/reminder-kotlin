package com.elementary.tasks.core.utils

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.TimeUtil.toGmt
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.AttachmentView
import com.elementary.tasks.core.views.BeforePickerView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.core.views.ExclusionPickerView
import com.elementary.tasks.core.views.GroupView
import com.elementary.tasks.core.views.LedPickerView
import com.elementary.tasks.core.views.LoudnessPickerView
import com.elementary.tasks.core.views.MelodyView
import com.elementary.tasks.core.views.PriorityPickerView
import com.elementary.tasks.core.views.RepeatLimitView
import com.elementary.tasks.core.views.RepeatView
import com.elementary.tasks.core.views.TuneExtraView
import com.elementary.tasks.core.views.WindowTypeView
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setHourOfDay
import com.github.naz013.calendarext.setMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.*

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

fun Fragment.startActivity(clazz: Class<*>, intent: ((Intent) -> Unit)? = null) {
  requireActivity().startActivity(clazz, intent)
}

fun Activity.startActivity(clazz: Class<*>, intent: ((Intent) -> Unit)? = null) {
  startActivity(Intent(this, clazz).also { intent?.invoke(it) })
}

fun Activity.finishWith(clazz: Class<*>, intent: ((Intent) -> Unit)? = null) {
  startActivity(Intent(this, clazz).also { intent?.invoke(it) })
  finish()
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
  val darkness = 1 - (0.299 * android.graphics.Color.red(this) + 0.587
    * android.graphics.Color.green(this) + 0.114
    * android.graphics.Color.blue(this)) / 255
  Timber.d("isColorDark: $darkness")
  return darkness >= 0.5
}

// Check of opacity of Color
fun Int.isAlmostTransparent(): Boolean {
  return this < 25
}

fun Fragment.colorOf(@ColorRes color: Int) = ContextCompat.getColor(requireContext(), color)

fun AppCompatActivity.colorOf(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.colorOf(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun View.colorOf(@ColorRes color: Int) = ContextCompat.getColor(context, color)

fun AppCompatEditText.onTextChanged(f: (String?) -> Unit) {
  doOnTextChanged { text, _, _, _ -> f.invoke(text?.toString()) }
}

fun View.inflater() = LayoutInflater.from(context)

fun Activity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, message, duration).show()
}

fun Activity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, message, duration).show()
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(requireContext(), message, duration).show()
}

fun <T> ViewModel.mutableLiveDataOf() = MutableLiveData<T>()

fun File.copyInputStreamToFile(inputStream: InputStream) {
  inputStream.use { input ->
    this.outputStream().use { fileOut ->
      input.copyTo(fileOut)
    }
  }
}

fun <ViewT : View> View.bindView(@IdRes idRes: Int): Lazy<ViewT> {
  return lazyUnSynchronized {
    findViewById(idRes)
  }
}

fun <ViewT : View> Activity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
  return lazyUnSynchronized {
    findViewById(idRes)
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

fun TimeUtil.HM.toDate(): Date = newCalendar()
  .setHourOfDay(hour)
  .setMinute(minute)
  .time

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

fun View.visibleGone(value: Boolean) {
  if (value && !isVisible()) show()
  else if (!value && !isGone()) hide()
}

fun View.visibleInvisible(value: Boolean) {
  if (value && !isVisible()) show()
  else if (!value && !isNotVisible()) transparent()
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

fun Long.toGmt() = toCalendar().toGmt()

fun Long.toCalendar() = newCalendar(this)

fun Long.daysAfter(): Int {
  val days = (System.currentTimeMillis() - this) / AlarmManager.INTERVAL_DAY
  return days.toInt()
}

fun Long.hoursAfter(): Int {
  val hours = (System.currentTimeMillis() - this) / AlarmManager.INTERVAL_HOUR
  return hours.toInt()
}

fun TuneExtraView.Extra.fromReminder(reminder: Reminder): TuneExtraView.Extra {
  this.useGlobal = reminder.useGlobal
  this.vibrate = reminder.vibrate
  this.repeatNotification = reminder.repeatNotification
  this.notifyByVoice = reminder.notifyByVoice
  this.unlock = reminder.unlock
  this.auto = reminder.auto
  return this
}

fun TuneExtraView.Extra.toReminder(reminder: Reminder): Reminder {
  reminder.useGlobal = this.useGlobal
  reminder.vibrate = this.vibrate
  reminder.repeatNotification = this.repeatNotification
  reminder.notifyByVoice = this.notifyByVoice
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

fun <T> Calendar.map(func: (Calendar) -> T): T {
  return func.invoke(this)
}