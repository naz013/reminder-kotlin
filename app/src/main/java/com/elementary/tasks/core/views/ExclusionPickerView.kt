package com.elementary.tasks.core.views

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.dialogs.DialogExclusionPickerBinding
import com.elementary.tasks.core.binding.views.ExclusionPickerViewBinding
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.datetime.TimeUtil
import java.util.*

class ExclusionPickerView : LinearLayout {

  private lateinit var binding: ExclusionPickerViewBinding
  var onExclusionUpdateListener: ((hours: List<Int>, from: String, to: String) -> Unit)? = null
  private val mHours = mutableListOf<Int>()
  private var mFrom: String = ""
  private var mTo: String = ""
  private var fromHour: Int = 0
  private var fromMinute: Int = 0
  private var toHour: Int = 0
  private var toMinute: Int = 0
  private val buttons = mutableListOf<ToggleButton>()

  private val selectedList: MutableList<Int>
    @SuppressLint("ResourceType")
    get() {
      val ids = ArrayList<Int>()
      for (button in buttons) {
        if (button.isChecked) ids.add(button.id - 100)
      }
      return ids
    }

  private val customizationView: DialogExclusionPickerBinding
    get() {
      val binding = DialogExclusionPickerBinding(LayoutInflater.from(context).inflate(R.layout.dialog_exclusion_picker, null))
      binding.selectInterval.isChecked = true
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = System.currentTimeMillis()
      fromHour = calendar.get(Calendar.HOUR_OF_DAY)
      fromMinute = calendar.get(Calendar.MINUTE)
      binding.from.text = context.getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true, lang())
      calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_HOUR * 3
      toHour = calendar.get(Calendar.HOUR_OF_DAY)
      toMinute = calendar.get(Calendar.MINUTE)
      binding.to.text = context.getString(R.string.to) + " " + TimeUtil.getTime(calendar.time, true, lang())
      binding.from.setOnClickListener { fromTime(binding.from) }
      binding.to.setOnClickListener { toTime(binding.to) }
      initButtons(binding)
      if (mFrom != "" && mTo != "") {
        calendar.time = TimeUtil.getDate(mFrom) ?: Date()
        fromHour = calendar.get(Calendar.HOUR_OF_DAY)
        fromMinute = calendar.get(Calendar.MINUTE)
        calendar.time = TimeUtil.getDate(mTo) ?: Date()
        toHour = calendar.get(Calendar.HOUR_OF_DAY)
        toMinute = calendar.get(Calendar.MINUTE)
        binding.selectInterval.isChecked = true
      }
      if (mHours.isNotEmpty()) {
        binding.selectHours.isChecked = true
      }
      return binding
    }

  var dialogues: Dialogues? = null
  var prefs: Prefs? = null

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  private fun lang(): Int = prefs?.appLanguage ?: 0

  fun setRangeHours(fromHour: String, toHour: String) {
    mFrom = fromHour
    mTo = toHour
    showRange()
  }

  fun setHours(hours: List<Int>) {
    mHours.clear()
    mHours.addAll(hours)
    showHours()
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_exclusion_picker, this)
    orientation = VERTICAL
    binding = ExclusionPickerViewBinding(this)

    binding.text.setOnClickListener {
      openExclusionDialog()
    }
    binding.hintIcon.setOnLongClickListener {
      Toast.makeText(context, context.getString(R.string.exclusion), Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.exclusion))
  }

  private fun openExclusionDialog() {
    val dialogues = dialogues ?: return
    val builder = dialogues.getMaterialDialog(context)
    builder.setTitle(R.string.exclusion)
    val b = customizationView
    builder.setView(b.view)
    builder.setPositiveButton(R.string.ok) { _, _ -> saveExclusion(b) }
    builder.setNegativeButton(R.string.remove_exclusion) { _, _ -> clearExclusion() }
    builder.create().show()
  }

  private fun clearExclusion() {
    mHours.clear()
    mFrom = ""
    mTo = ""
    onExclusionUpdateListener?.invoke(mHours, mFrom, mTo)
  }

  private fun saveExclusion(b: DialogExclusionPickerBinding) {
    when {
      b.selectHours.isChecked -> {
        mHours.clear()
        mHours.addAll(selectedList)
        showHours()
        onExclusionUpdateListener?.invoke(mHours, mFrom, mTo)
      }
      b.selectInterval.isChecked -> {
        mFrom = getHour(fromHour, fromMinute)
        mTo = getHour(toHour, toMinute)
        showRange()
        onExclusionUpdateListener?.invoke(mHours, mFrom, mTo)
      }
      else -> {
        clearExclusion()
        showNoExclusion()
      }
    }
  }

  private fun showNoExclusion() {
    if (mHours.isEmpty() && mFrom == "" && mTo == "") {
      binding.text.text = context.getString(R.string.not_selected)
    }
  }

  private fun showRange() {
    if (mFrom != "" && mTo != "") {
      var message = context.getString(R.string.from) + " "
      message += "$mFrom "
      message += context.getString(R.string.to) + " "
      message += mTo
      binding.text.text = message
    } else {
      showNoExclusion()
    }
  }

  private fun showHours() {
    if (mHours.isNotEmpty()) {
      val message = mHours.joinToString(separator = ", ")
      binding.text.text = message
    } else {
      showNoExclusion()
    }
  }

  private fun getHour(hour: Int, minute: Int): String {
    return "$hour:$minute"
  }

  private fun initButtons(b: DialogExclusionPickerBinding) {
    setId(b.zero, b.one, b.two, b.three, b.four, b.five, b.six, b.seven, b.eight, b.nine, b.ten,
      b.eleven, b.twelve, b.thirteen, b.fourteen, b.fifteen, b.sixteen, b.seventeen,
      b.eighteen, b.nineteen, b.twenty, b.twentyOne, b.twentyThree, b.twentyTwo)
  }

  private fun setId(vararg buttons: ToggleButton) {
    var i = 100
    this.buttons.clear()
    val selected = mutableListOf<Int>()
    selected.addAll(mHours)
    for (button in buttons) {
      button.id = i
      button.setBackgroundResource(R.drawable.toggle_blue)
      this.buttons.add(button)
      if (selected.contains(i - 100)) button.isChecked = true
      i++
    }
  }

  private fun fromTime(textView: TextView) {
    val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
      fromHour = hourOfDay
      fromMinute = minute
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = System.currentTimeMillis()
      calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
      calendar.set(Calendar.MINUTE, minute)
      textView.text = context.getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true, lang())
    }
    TimeUtil.showTimePicker(context, prefs?.is24HourFormat ?: false, fromHour, fromMinute, listener)
  }

  private fun toTime(textView: TextView) {
    val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
      toHour = hourOfDay
      toMinute = minute
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = System.currentTimeMillis()
      calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
      calendar.set(Calendar.MINUTE, minute)
      textView.text = context.getString(R.string.to) + " " + TimeUtil.getTime(calendar.time, true, lang())
    }
    TimeUtil.showTimePicker(context, prefs?.is24HourFormat ?: false, toHour, toMinute, listener)
  }
}