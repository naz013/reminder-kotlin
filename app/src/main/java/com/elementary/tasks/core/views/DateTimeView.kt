package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.DateTimeViewBinding
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class DateTimeView : LinearLayout, KoinComponent {

  private val dateTimeManager by inject<DateTimeManager>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  private lateinit var binding: DateTimeViewBinding
  private var date: LocalDate = LocalDate.now()
  private var time: LocalTime = LocalTime.now()
  private var isSingleMode = false
  private var onSelectListener: OnSelectListener? = null
  var onDateChangeListener: OnDateChangeListener? = null
  private var dateTimeFormatter = dateTimeManager.fullDateFormatter()

  var selectedDateTime: LocalDateTime
    get() = LocalDateTime.of(date, time)
    set(value) = updateDateTime(value)

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  fun setDateFormat(formatter: DateTimeFormatter) {
    this.dateTimeFormatter = formatter
    this.invalidate()
  }

  fun setDateTime(dateTime: String) {
    updateDateTime(dateTimeManager.fromGmtToLocal(dateTime) ?: LocalDateTime.now())
  }

  fun setOnSelectListener(listener: OnSelectListener) {
    onSelectListener = listener
  }

  private fun init(context: Context) {
    orientation = VERTICAL
    View.inflate(context, R.layout.view_date_time, this)
    binding = DateTimeViewBinding(this)

    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
    val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    layoutParams = params
    setDateFormat(dateTimeManager.fullDateFormatter())

    binding.dateField.setOnClickListener { selectDate() }
    binding.timeField.setOnClickListener { selectTime() }
    updateDateTime(LocalDateTime.now())
  }

  override fun setOnClickListener(l: OnClickListener?) {
    if (isSingleMode) binding.dateField.setOnClickListener(l)
  }

  override fun setOnLongClickListener(l: OnLongClickListener?) {
    binding.dateField.setOnLongClickListener(l)
    binding.timeField.setOnLongClickListener(l)
  }

  private fun updateDateTime(localDateTime: LocalDateTime) {
    date = localDateTime.toLocalDate()
    time = localDateTime.toLocalTime()
    updateTime(time)
    updateDate(date)
  }

  private fun updateDate(localDate: LocalDate) {
    binding.dateField.text = localDate.format(dateTimeFormatter)
    onSelectListener?.onDateSelect(localDate)
    onDateChangeListener?.onChanged(LocalDateTime.of(date, time))
  }

  private fun updateTime(localTime: LocalTime) {
    binding.timeField.text = dateTimeManager.getTime(localTime)
    onSelectListener?.onTimeSelect(localTime)
    onDateChangeListener?.onChanged(LocalDateTime.of(date, time))
  }

  private fun selectDate() {
    dateTimePickerProvider.showDatePicker(context, date) {
      this.date = it
      updateDate(it)
    }
  }

  private fun selectTime() {
    dateTimePickerProvider.showTimePicker(context, time) {
      this.time = it
      updateTime(it)
    }
  }

  interface OnSelectListener {
    fun onDateSelect(date: LocalDate)

    fun onTimeSelect(time: LocalTime)
  }

  interface OnDateChangeListener {
    fun onChanged(dateTime: LocalDateTime)
  }
}
