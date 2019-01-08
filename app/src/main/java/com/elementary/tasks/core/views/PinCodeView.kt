package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.view_pin_code.view.*

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
class PinCodeView : LinearLayout, View.OnClickListener {

    private var pinString = ""
    var callback: ((String) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        orientation = LinearLayout.VERTICAL
        View.inflate(context, R.layout.view_pin_code, this)
        deleteButton.setOnClickListener {
            pinString = pinString.substring(0, pinString.length - 1)
            updateTimeView()
        }
        deleteButton.setOnLongClickListener {
            clearPin()
            true
        }
        initButtons()
        updateTimeView()
    }

    fun clearPin() {
        pinString = ""
        updateTimeView()
    }

    private fun initButtons() {
        if (b1 != null) {
            b1.id = Integer.valueOf(101)
            b2.id = Integer.valueOf(102)
            b3.id = Integer.valueOf(103)
            b4.id = Integer.valueOf(104)
            b5.id = Integer.valueOf(105)
            b6.id = Integer.valueOf(106)
            b7.id = Integer.valueOf(107)
            b8.id = Integer.valueOf(108)
            b9.id = Integer.valueOf(109)
            b0.id = Integer.valueOf(100)
            b1.setOnClickListener(this)
            b2.setOnClickListener(this)
            b3.setOnClickListener(this)
            b4.setOnClickListener(this)
            b5.setOnClickListener(this)
            b6.setOnClickListener(this)
            b7.setOnClickListener(this)
            b8.setOnClickListener(this)
            b9.setOnClickListener(this)
            b0.setOnClickListener(this)
        }
    }

    private fun updateTimeView() {
        deleteButton.isEnabled = pinString.isNotEmpty()
        clearBirds()
        showBirds()
    }

    private fun showBirds() {
        for(i in 0 until pinString.length) {
            birdsView.getChildAt(i)?.visibility = View.VISIBLE
        }
    }

    private fun clearBirds() {
        for(child in birdsView.children) {
            child.visibility = View.INVISIBLE
        }
    }

    override fun onClick(view: View) {
        val ids = view.id
        if (ids in 100..109) {
            if (pinString.length < 6) {
                pinString += (ids - 100).toString()
                updateTimeView()
                callback?.invoke(pinString)
            }
        }
    }
}
