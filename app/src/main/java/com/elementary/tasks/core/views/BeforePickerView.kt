package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.databinding.ViewRemindBeforeBinding
import com.github.naz013.logging.Logger

class BeforePickerView : LinearLayout, TextWatcher, AdapterView.OnItemSelectedListener {

  private lateinit var binding: ViewRemindBeforeBinding

  private var mImm: InputMethodManager? = null
  var onBeforeChangedListener: OnBeforeChangedListener? = null

  private var mState = DateTimeManager.MultiplierType.MINUTE.index
  private var mRepeatValue: Int = 0

  private val multiplier: Long
    get() {
      return when (mState) {
        DateTimeManager.MultiplierType.SECOND.index -> DateTimeManager.SECOND
        DateTimeManager.MultiplierType.MINUTE.index -> DateTimeManager.MINUTE
        DateTimeManager.MultiplierType.HOUR.index -> DateTimeManager.HOUR
        DateTimeManager.MultiplierType.DAY.index -> DateTimeManager.DAY
        DateTimeManager.MultiplierType.WEEK.index -> DateTimeManager.DAY * 7
        DateTimeManager.MultiplierType.MONTH.index -> DateTimeManager.DAY * 30
        else -> DateTimeManager.DAY
      }
    }

  private val beforeValue: Long
    get() {
      val rep = mRepeatValue * multiplier
      Logger.d("getBeforeValue: $rep")
      return rep
    }

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_remind_before, this)
    orientation = VERTICAL
    binding = ViewRemindBeforeBinding.bind(this)

    mImm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

    binding.beforeTypeView.onItemSelectedListener = this
    binding.beforeValueView.addTextChangedListener(this)
    binding.beforeValueView.setOnFocusChangeListener { _, hasFocus ->
      if (mImm == null) return@setOnFocusChangeListener
      if (!hasFocus) {
        mImm?.hideSoftInputFromWindow(binding.beforeValueView.windowToken, 0)
      } else {
        mImm?.showSoftInput(binding.beforeValueView, 0)
      }
    }
    binding.beforeValueView.setOnClickListener {
      if (mImm == null) return@setOnClickListener
      if (!mImm!!.isActive(binding.beforeValueView)) {
        mImm?.showSoftInput(binding.beforeValueView, 0)
      }
    }
    mRepeatValue = 0
    binding.beforeValueView.setText(mRepeatValue.toString())
    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.BeforePickerView, 0, 0)
      try {
        mState = a.getInt(
          /* index = */ R.styleable.BeforePickerView_before_type,
          /* defValue = */ DateTimeManager.MultiplierType.MINUTE.index
        )
      } catch (e: Exception) {
        Logger.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
    binding.beforeTypeView.setSelection(mState)
  }

  private fun setState(state: Int) {
    this.mState = state
    onBeforeChangedListener?.onChanged(beforeValue)
  }

  private fun updateEditField() {
    binding.beforeValueView.setSelection(binding.beforeValueView.text.toString().length)
  }

  fun setBefore(mills: Long) {
    if (mills == 0L) {
      setProgress(0)
      return
    }
    val beforeTime = DateTimeManager.parseBeforeTime(mills)
    setProgress(beforeTime.value.toInt())
    binding.beforeTypeView.setSelection(beforeTime.type.index)
  }

  private fun setProgress(i: Int) {
    mRepeatValue = i
    binding.beforeValueView.setText(i.toString())
    updateEditField()
  }

  override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    try {
      mRepeatValue = Integer.parseInt(s.toString())
      if (mRepeatValue > 0 && s.toString().startsWith("0")) {
        binding.beforeValueView.setText(mRepeatValue.toString())
        binding.beforeValueView.setSelection(binding.beforeValueView.text.toString().length)
        return
      }
    } catch (e: NumberFormatException) {
      binding.beforeValueView.setText("0")
    }
    onBeforeChangedListener?.onChanged(beforeValue)
  }

  override fun afterTextChanged(s: Editable) {
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
  }

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    setState(position)
  }

  interface OnBeforeChangedListener {
    fun onChanged(beforeMills: Long)
  }
}
