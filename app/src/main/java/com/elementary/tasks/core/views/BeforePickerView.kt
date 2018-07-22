package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.views.roboto.RoboEditText

/**
 * Copyright 2016 Nazar Suhovich
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
class BeforePickerView : LinearLayout, TextWatcher {

    private val seconds = 0
    private val minutes = 1
    private val hours = 2
    private val days = 3
    private val weeks = 4

    private lateinit var mBeforeInput: RoboEditText
    private var mImm: InputMethodManager? = null
    private lateinit var beforeType: Spinner

    private var mState = minutes
    private var mRepeatValue: Int = 0

    private val multiplier: Long
        get() {
            return when (mState) {
                seconds -> TimeCount.SECOND
                minutes -> TimeCount.MINUTE
                hours -> TimeCount.HOUR
                days -> TimeCount.DAY
                weeks -> TimeCount.DAY * 7
                else -> TimeCount.DAY
            }
        }

    val beforeValue: Long
        get() {
            val rep = mRepeatValue * multiplier
            LogUtil.d(TAG, "getBeforeValue: $rep")
            return rep
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
        View.inflate(context, R.layout.view_remind_before, this)
        orientation = LinearLayout.VERTICAL
        mImm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        mBeforeInput = findViewById(R.id.before_value_view)
        beforeType = findViewById(R.id.before_type_view)
        beforeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                setState(i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
        mBeforeInput.addTextChangedListener(this)
        mBeforeInput.setOnFocusChangeListener { _, hasFocus ->
            if (mImm == null) return@setOnFocusChangeListener
            if (!hasFocus) {
                mImm?.hideSoftInputFromWindow(mBeforeInput.windowToken, 0)
            } else {
                mImm?.showSoftInput(mBeforeInput, 0)
            }
        }
        mBeforeInput.setOnClickListener {
            if (mImm == null) return@setOnClickListener
            if (!mImm!!.isActive(mBeforeInput)) {
                mImm?.showSoftInput(mBeforeInput, 0)
            }
        }
        mRepeatValue = 0
        mBeforeInput.setText(mRepeatValue.toString())
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.BeforePickerView, 0, 0)
            try {
                mState = a.getInt(R.styleable.BeforePickerView_before_type, minutes)
            } catch (e: Exception) {
                LogUtil.e(TAG, "There was an error loading attributes.", e)
            } finally {
                a.recycle()
            }
        }
        beforeType.setSelection(mState)
    }

    private fun setState(state: Int) {
        this.mState = state
    }

    private fun updateEditField() {
        mBeforeInput.setSelection(mBeforeInput.text.toString().length)
    }

    fun setBefore(mills: Long) {
        if (mills == 0L) {
            setProgress(0)
            return
        }
        when {
            mills % (TimeCount.DAY * 7) == 0L -> {
                val progress = mills / (TimeCount.DAY * 7)
                setProgress(progress.toInt())
                beforeType.setSelection(weeks)
            }
            mills % TimeCount.DAY == 0L -> {
                val progress = mills / TimeCount.DAY
                setProgress(progress.toInt())
                beforeType.setSelection(days)
            }
            mills % TimeCount.HOUR == 0L -> {
                val progress = mills / TimeCount.HOUR
                setProgress(progress.toInt())
                beforeType.setSelection(hours)
            }
            mills % TimeCount.MINUTE == 0L -> {
                val progress = mills / TimeCount.MINUTE
                setProgress(progress.toInt())
                beforeType.setSelection(minutes)
            }
            mills % TimeCount.SECOND == 0L -> {
                val progress = mills / TimeCount.SECOND
                setProgress(progress.toInt())
                beforeType.setSelection(seconds)
            }
        }
    }

    private fun setProgress(i: Int) {
        mRepeatValue = i
        mBeforeInput.setText(i.toString())
        updateEditField()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        try {
            mRepeatValue = Integer.parseInt(s.toString())
        } catch (e: NumberFormatException) {
            mBeforeInput.setText("0")
        }
    }

    override fun afterTextChanged(s: Editable) {
    }

    companion object {
        private const val TAG = "BeforePickerView"
    }
}
