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
import com.elementary.tasks.core.utils.TimeCount
import kotlinx.android.synthetic.main.view_repeat.view.*
import timber.log.Timber

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class RepeatView : LinearLayout, TextWatcher {

    private var mRepeatListener: OnRepeatListener? = null
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
                    repeatType.setSelection(WEEKS)
                }
                fitInterval(mills, TimeCount.DAY) -> {
                    val progress = mills / TimeCount.DAY
                    setProgress(progress.toInt())
                    repeatType.setSelection(DAYS)
                }
                fitInterval(mills, TimeCount.HOUR) -> {
                    val progress = mills / TimeCount.HOUR
                    setProgress(progress.toInt())
                    repeatType.setSelection(HOURS)
                }
                fitInterval(mills, TimeCount.MINUTE) -> {
                    val progress = mills / TimeCount.MINUTE
                    setProgress(progress.toInt())
                    repeatType.setSelection(MINUTES)
                }
                fitInterval(mills, TimeCount.SECOND) -> {
                    val progress = mills / TimeCount.SECOND
                    setProgress(progress.toInt())
                    repeatType.setSelection(SECONDS)
                }
                else -> {
                    setProgress(mills.toInt())
                    repeatType.setSelection(0)
                }
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
        orientation = LinearLayout.HORIZONTAL
        mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        repeatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (!mIsLocked) setState(i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
        repeatTitle.addTextChangedListener(this)
        repeatTitle.setOnFocusChangeListener { _, hasFocus ->
            if (mImm == null) return@setOnFocusChangeListener
            if (!hasFocus) {
                mImm?.hideSoftInputFromWindow(repeatTitle.windowToken, 0)
            } else {
                mImm?.showSoftInput(repeatTitle, 0)
            }
        }
        repeatTitle.setOnClickListener {
            if (mImm == null) return@setOnClickListener
            if (!mImm!!.isActive(repeatTitle)) {
                mImm?.showSoftInput(repeatTitle, 0)
            }
        }
        mRepeatValue = 0
        repeatTitle.setText(mRepeatValue.toString())
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
        if (mState == MONTHS && mIsLocked) {
            val spinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.repeat_times_month))
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            repeatType.adapter = spinnerAdapter
            repeatType.isEnabled = false
            repeatTitle.setText("1")
        } else {
            repeatType.isEnabled = true
            repeatType.setSelection(mState)
        }
        setState(mState)
    }

    private fun setState(state: Int) {
        if (mState == state) return
        this.mState = state
    }

    fun setListener(listener: OnRepeatListener?) {
        this.mRepeatListener = listener
    }

    private fun updateEditField() {
        repeatTitle.setSelection(repeatTitle.text.toString().length)
    }

    private fun setProgress(i: Int) {
        mRepeatValue = i
        repeatTitle.setText(i.toString())
        updateEditField()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        try {
            mRepeatValue = Integer.parseInt(s.toString())
            if (mState == MONTHS && mRepeatValue <= 0) {
                repeatTitle.setText("1")
                repeatTitle.setSelection(repeatTitle.text.toString().length)
                return
            }
            mRepeatListener?.onProgress(mRepeatValue)
            onRepeatChangeListener?.onChanged(repeat)
        } catch (e: NumberFormatException) {
            if (mState == MONTHS) {
                repeatTitle.setText("1")
            } else {
                repeatTitle.setText("0")
            }
            repeatTitle.setSelection(repeatTitle.text.toString().length)
        }
    }

    override fun afterTextChanged(s: Editable) {
    }

    interface OnRepeatListener {
        fun onProgress(progress: Int)
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
