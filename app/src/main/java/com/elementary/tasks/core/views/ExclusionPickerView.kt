package com.elementary.tasks.core.views

import android.annotation.SuppressLint
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
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.Dialogues
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.LocalTime
import java.util.*

class ExclusionPickerView : LinearLayout, KoinComponent {

  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val dialogues by inject<Dialogues>()

  private lateinit var binding: ExclusionPickerViewBinding
  var onExclusionUpdateListener: ((hours: List<Int>, from: String, to: String) -> Unit)? = null
  private val mHours = mutableListOf<Int>()
  private var mFrom: String = ""
  private var mTo: String = ""

  private var fromTime: LocalTime = LocalTime.now()
  private var toTime: LocalTime = fromTime.plusHours(3)

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
      val binding = DialogExclusionPickerBinding(
        LayoutInflater.from(context).inflate(R.layout.dialog_exclusion_picker, null)
      )
      binding.selectInterval.isChecked = true

      showFromTime(binding.from, fromTime)
      showToTime(binding.to, toTime)

      binding.from.setOnClickListener { fromTime(binding.from) }
      binding.to.setOnClickListener { toTime(binding.to) }

      initButtons(binding)
      if (mFrom.isNotEmpty() && mTo.isNotEmpty()) {
        fromTime = dateTimeManager.toLocalTime(mFrom) ?: LocalTime.now()
        toTime = dateTimeManager.toLocalTime(mTo) ?: LocalTime.now()
        binding.selectInterval.isChecked = true
      }
      if (mHours.isNotEmpty()) {
        binding.selectHours.isChecked = true
      }
      return binding
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

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
    fromTime = LocalTime.now()
    toTime = fromTime.plusHours(3)
    showNoExclusion()
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
        mFrom = getHour(fromTime)
        mTo = getHour(toTime)
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

  private fun getHour(time: LocalTime): String {
    return dateTimeManager.to24HourString(time)
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

  @SuppressLint("SetTextI18n")
  private fun fromTime(textView: TextView) {
    dateTimePickerProvider.showTimePicker(context, fromTime) {
      fromTime = it
      showFromTime(textView, it)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun toTime(textView: TextView) {
    dateTimePickerProvider.showTimePicker(context, toTime) {
      toTime = it
      showToTime(textView, it)
    }
  }

  private fun showFromTime(textView: TextView, time: LocalTime) {
    showTime(textView, context.getString(R.string.from), time)
  }

  private fun showToTime(textView: TextView, time: LocalTime) {
    showTime(textView, context.getString(R.string.to), time)
  }

  @SuppressLint("SetTextI18n")
  private fun showTime(textView: TextView, prefix: String, time: LocalTime) {
    textView.text = "$prefix ${dateTimeManager.getTime(time)}"
  }
}
