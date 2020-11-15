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
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.RepeatViewBinding
import com.elementary.tasks.core.utils.TimeCount
import timber.log.Timber

class RepeatView : LinearLayout, TextWatcher {

  private lateinit var binding: RepeatViewBinding
  var onRepeatChangeListener: OnRepeatChangeListener? = null
  private var mImm: InputMethodManager? = null

  private var mState = DAYS
  private var mRepeatValue: Int = 0
  private var mIsLocked = false

  private val multiplier: Long
    get() {
      return when (mState) {
        SECONDS -> TimeCount.SECOND
        MINUTES -> TimeCount.MINUTE
        HOURS -> TimeCount.HOUR
        DAYS -> TimeCount.DAY
        WEEKS -> TimeCount.DAY * 7
        else -> TimeCount.DAY
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
      when {
        fitInterval(mills, TimeCount.DAY * 7) -> {
          val progress = mills / (TimeCount.DAY * 7)
          setProgress(progress.toInt())
          selectState(WEEKS)
        }
        fitInterval(mills, TimeCount.DAY) -> {
          val progress = mills / TimeCount.DAY
          setProgress(progress.toInt())
          selectState(DAYS)
        }
        fitInterval(mills, TimeCount.HOUR) -> {
          val progress = mills / TimeCount.HOUR
          setProgress(progress.toInt())
          selectState(HOURS)
        }
        fitInterval(mills, TimeCount.MINUTE) -> {
          val progress = mills / TimeCount.MINUTE
          setProgress(progress.toInt())
          selectState(MINUTES)
        }
        fitInterval(mills, TimeCount.SECOND) -> {
          val progress = mills / TimeCount.SECOND
          setProgress(progress.toInt())
          selectState(SECONDS)
        }
        else -> {
          setProgress(mills.toInt())
          selectState(0)
        }
      }
    }

  private fun selectState(state: Int) {
    if (state < binding.repeatType.adapter.count) {
      binding.repeatType.setSelection(state)
    }
  }

  private fun fitInterval(interval: Long, matcher: Long): Boolean {
    return interval > matcher && (interval % matcher == 0L)
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

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_repeat, this)
    orientation = HORIZONTAL
    binding = RepeatViewBinding(this)

    mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    binding.hintIcon.setOnLongClickListener {
      Toast.makeText(context, context.getString(R.string.repeat), Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.repeat))
    binding.repeatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
        if (!mIsLocked) setState(i)
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
        mState = a.getInt(R.styleable.RepeatView_repeatType, DAYS)
        mIsLocked = a.getBoolean(R.styleable.RepeatView_isLocked, false)
      } catch (e: Exception) {
        Timber.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
    mRepeatValue = defaultValue
    setDefaultField()
    if (mState == MONTHS && mIsLocked) {
      binding.repeatType.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.repeat_times_month))
      binding.repeatType.isEnabled = false
    } else {
      binding.repeatType.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.repeat_times))
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
    private const val SECONDS = 0
    private const val MINUTES = 1
    private const val HOURS = 2
    private const val DAYS = 3
    private const val WEEKS = 4
    private const val MONTHS = 5
  }
}
