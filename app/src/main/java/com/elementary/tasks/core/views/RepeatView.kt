package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.databinding.ViewRepeatBinding
import com.github.naz013.logging.Logger

class RepeatView : LinearLayout, TextWatcher {

  private lateinit var binding: ViewRepeatBinding
  var onRepeatChangeListener: OnRepeatChangeListener? = null
  private var mImm: InputMethodManager? = null

  private var mState = DateTimeManager.MultiplierType.DAY.index
  private var mRepeatValue: Int = 0
  private var mIsLocked = false

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

  var defaultValue: Int = 0
    set(value) {
      field = value
      setDefaultField()
    }
  var repeat: Long
    get() {
      return if (mState == MONTHS) {
        mRepeatValue.toLong()
      } else {
        mRepeatValue * multiplier
      }
    }
    set(mills) {
      if (mills == 0L) {
        setProgress(0)
        return
      }
      val repeatTime = DateTimeManager.parseRepeatTime(mills)
      setProgress(repeatTime.value.toInt())
      selectState(repeatTime.type.index)
    }

  private fun selectState(state: Int) {
    if (state < binding.repeatType.adapter.count) {
      binding.repeatType.setSelection(state)
    }
  }

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_repeat, this)
    orientation = HORIZONTAL
    binding = ViewRepeatBinding.bind(this)

    mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    binding.repeatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
        if (!mIsLocked) {
          setState(i)
        }
      }

      override fun onNothingSelected(adapterView: AdapterView<*>) {
      }
    }
    binding.repeatTitle.addTextChangedListener(this)
    binding.repeatTitle.setOnFocusChangeListener { _, hasFocus ->
      if (mImm == null) return@setOnFocusChangeListener
      if (!hasFocus) {
        mImm?.hideSoftInputFromWindow(binding.repeatTitle.windowToken, 0)
      } else {
        mImm?.showSoftInput(binding.repeatTitle, 0)
      }
    }
    binding.repeatTitle.setOnClickListener {
      if (mImm == null) return@setOnClickListener
      if (mImm?.isActive(binding.repeatTitle) == false) {
        mImm?.showSoftInput(binding.repeatTitle, 0)
      }
    }
    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.RepeatView, 0, 0)
      try {
        mState = a.getInt(
          /* index = */ R.styleable.RepeatView_repeatType,
          /* defValue = */ DateTimeManager.MultiplierType.DAY.index
        )
        mIsLocked = a.getBoolean(R.styleable.RepeatView_isLocked, false)
      } catch (e: Exception) {
        Logger.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
    mRepeatValue = defaultValue
    setDefaultField()
    if (mState == MONTHS && mIsLocked) {
      binding.repeatType.adapter = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_item,
        resources.getStringArray(R.array.repeat_times_month)
      )
      binding.repeatType.isEnabled = false
    } else {
      binding.repeatType.adapter = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_item,
        resources.getStringArray(R.array.repeat_times)
      )
      binding.repeatType.isEnabled = true
    }
    setState(mState)
  }

  private fun setState(state: Int) {
    if (mState == state) return
    this.mState = state
    onRepeatChangeListener?.onChanged(repeat)
  }

  private fun updateEditField() {
    binding.repeatTitle.setSelection(binding.repeatTitle.text.toString().length)
  }

  private fun setProgress(i: Int) {
    if (mState == MONTHS && mIsLocked) {
      if (i < defaultValue) {
        setDefaultField()
      } else {
        mRepeatValue = i
        binding.repeatTitle.setText(i.toString())
        updateEditField()
      }
    } else {
      mRepeatValue = i
      binding.repeatTitle.setText(i.toString())
      updateEditField()
    }
  }

  override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    try {
      mRepeatValue = Integer.parseInt(s.toString())
      if (mRepeatValue > 0 && s.toString().startsWith("0")) {
        binding.repeatTitle.setText(mRepeatValue.toString())
        binding.repeatTitle.setSelection(binding.repeatTitle.text.toString().length)
        return
      }
      if (mState == MONTHS && mRepeatValue < defaultValue) {
        setDefaultField()
        return
      }
    } catch (e: NumberFormatException) {
      setDefaultField()
    }
    onRepeatChangeListener?.onChanged(repeat)
  }

  private fun setDefaultField() {
    mRepeatValue = defaultValue
    binding.repeatTitle.setText(defaultValue.toString())
    binding.repeatTitle.setSelection(binding.repeatTitle.text.toString().length)
  }

  override fun afterTextChanged(s: Editable) {
  }

  interface OnRepeatChangeListener {
    fun onChanged(repeat: Long)
  }

  companion object {
    private const val MONTHS = 5
  }
}
