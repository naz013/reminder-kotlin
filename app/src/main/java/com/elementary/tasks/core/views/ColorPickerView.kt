package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module

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

class ColorPickerView : LinearLayout {

    private var red: ImageButton? = null
    private var green: ImageButton? = null
    private var blue: ImageButton? = null
    private var yellow: ImageButton? = null
    private var greenLight: ImageButton? = null
    private var blueLight: ImageButton? = null
    private var cyan: ImageButton? = null
    private var purple: ImageButton? = null
    private var amber: ImageButton? = null
    private var orange: ImageButton? = null
    private var pink: ImageButton? = null
    private var teal: ImageButton? = null
    private var deepPurple: ImageButton? = null
    private var deepOrange: ImageButton? = null
    private var indigo: ImageButton? = null
    private var lime: ImageButton? = null
    private var prevId: Int = 0
    var selectedCode: Int = 0
        private set
    private var mColorListener: OnColorListener? = null

    private val listener = { v -> themeColorSwitch(v.getId()) }

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
        View.inflate(context, R.layout.view_color_picker, this)
        orientation = LinearLayout.VERTICAL
        red = findViewById(R.id.red_checkbox)
        purple = findViewById(R.id.violet_checkbox)
        green = findViewById(R.id.green_checkbox)
        greenLight = findViewById(R.id.light_green_checkbox)
        blue = findViewById(R.id.blue_checkbox)
        blueLight = findViewById(R.id.light_blue_checkbox)
        yellow = findViewById(R.id.yellow_checkbox)
        orange = findViewById(R.id.orange_checkbox)
        cyan = findViewById(R.id.grey_checkbox)
        pink = findViewById(R.id.pink_checkbox)
        teal = findViewById(R.id.sand_checkbox)
        amber = findViewById(R.id.brown_checkbox)

        deepPurple = findViewById(R.id.deepPurple)
        indigo = findViewById(R.id.indigoCheckbox)
        lime = findViewById(R.id.limeCheckbox)
        deepOrange = findViewById(R.id.deepOrange)

        val themeGroupPro = findViewById<LinearLayout>(R.id.themeGroupPro)
        if (Module.isPro) {
            themeGroupPro.visibility = View.VISIBLE
        } else
            themeGroupPro.visibility = View.GONE

        setOnClickListener(red, green, blue, yellow, greenLight, blueLight, cyan, purple,
                amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime)
    }

    fun setSelectedColor(code: Int) {
        when (code) {
            0 -> red!!.isSelected = true
            1 -> purple!!.isSelected = true
            2 -> greenLight!!.isSelected = true
            3 -> green!!.isSelected = true
            4 -> blueLight!!.isSelected = true
            5 -> blue!!.isSelected = true
            6 -> yellow!!.isSelected = true
            7 -> orange!!.isSelected = true
            8 -> cyan!!.isSelected = true
            9 -> pink!!.isSelected = true
            10 -> teal!!.isSelected = true
            11 -> amber!!.isSelected = true
            else -> if (Module.isPro) {
                when (code) {
                    12 -> deepPurple!!.isSelected = true
                    13 -> deepOrange!!.isSelected = true
                    14 -> lime!!.isSelected = true
                    15 -> indigo!!.isSelected = true
                    else -> blue!!.isSelected = true
                }
            }
        }
    }

    private fun setOnClickListener(vararg views: View) {
        for (view in views) {
            view.setOnClickListener(listener)
        }
    }

    fun setListener(listener: OnColorListener) {
        this.mColorListener = listener
    }

    private fun themeColorSwitch(radio: Int) {
        if (radio == prevId) return
        prevId = radio
        disableAll()
        setSelected(radio)
        when (radio) {
            R.id.red_checkbox -> selectColor(0)
            R.id.violet_checkbox -> selectColor(1)
            R.id.light_green_checkbox -> selectColor(2)
            R.id.green_checkbox -> selectColor(3)
            R.id.light_blue_checkbox -> selectColor(4)
            R.id.blue_checkbox -> selectColor(5)
            R.id.yellow_checkbox -> selectColor(6)
            R.id.orange_checkbox -> selectColor(7)
            R.id.grey_checkbox -> selectColor(8)
            R.id.pink_checkbox -> selectColor(9)
            R.id.sand_checkbox -> selectColor(10)
            R.id.brown_checkbox -> selectColor(11)
            R.id.deepPurple -> selectColor(12)
            R.id.deepOrange -> selectColor(13)
            R.id.limeCheckbox -> selectColor(14)
            R.id.indigoCheckbox -> selectColor(15)
        }
    }

    private fun selectColor(code: Int) {
        this.selectedCode = code
        if (mColorListener != null) {
            mColorListener!!.onColorSelect(code)
        }
    }

    private fun setSelected(radio: Int) {
        findViewById<View>(radio).isSelected = true
    }

    private fun disableAll() {
        red!!.isSelected = false
        purple!!.isSelected = false
        greenLight!!.isSelected = false
        green!!.isSelected = false
        blueLight!!.isSelected = false
        blue!!.isSelected = false
        yellow!!.isSelected = false
        orange!!.isSelected = false
        cyan!!.isSelected = false
        pink!!.isSelected = false
        teal!!.isSelected = false
        amber!!.isSelected = false
        deepOrange!!.isSelected = false
        deepPurple!!.isSelected = false
        lime!!.isSelected = false
        indigo!!.isSelected = false
    }

    interface OnColorListener {
        fun onColorSelect(code: Int)
    }
}
