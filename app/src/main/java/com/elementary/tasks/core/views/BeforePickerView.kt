package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.BeforePickerViewBinding
import com.elementary.tasks.core.utils.TimeCount
import timber.log.Timber

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
class BeforePickerView : LinearLayout, TextWatcher, AdapterView.OnItemSelectedListener {

    private lateinit var binding: BeforePickerViewBinding
    private val seconds = 0
    private val minutes = 1
    private val hours = 2
    private val days = 3
    private val weeks = 4

    private var mImm: InputMethodManager? = null
    var onBeforeChangedListener: OnBeforeChangedListener? = null

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

    private val beforeValue: Long
        get() {
            val rep = mRepeatValue * multiplier
            Timber.d("getBeforeValue: $rep")
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
        orientation = LinearLayout.HORIZONTAL
        binding = BeforePickerViewBinding(this)

        mImm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        binding.hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.before_time), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.before_time))

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
                mState = a.getInt(R.styleable.BeforePickerView_before_type, minutes)
            } catch (e: Exception) {
                Timber.d("init: ${e.message}")
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
        when {
            mills % (TimeCount.DAY * 7) == 0L -> {
                val progress = mills / (TimeCount.DAY * 7)
                setProgress(progress.toInt())
                binding.beforeTypeView.setSelection(weeks)
            }
            mills % TimeCount.DAY == 0L -> {
                val progress = mills / TimeCount.DAY
                setProgress(progress.toInt())
                binding.beforeTypeView.setSelection(days)
            }
            mills % TimeCount.HOUR == 0L -> {
                val progress = mills / TimeCount.HOUR
                setProgress(progress.toInt())
                binding.beforeTypeView.setSelection(hours)
            }
            mills % TimeCount.MINUTE == 0L -> {
                val progress = mills / TimeCount.MINUTE
                setProgress(progress.toInt())
                binding.beforeTypeView.setSelection(minutes)
            }
            mills % TimeCount.SECOND == 0L -> {
                val progress = mills / TimeCount.SECOND
                setProgress(progress.toInt())
                binding.beforeTypeView.setSelection(seconds)
            }
        }
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
