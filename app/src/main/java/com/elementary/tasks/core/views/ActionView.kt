package com.elementary.tasks.core.views

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RadioGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.views.roboto.RoboCheckBox
import com.elementary.tasks.core.views.roboto.RoboEditText
import com.elementary.tasks.core.views.roboto.RoboRadioButton

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

class ActionView : LinearLayout {

    private var actionCheck: RoboCheckBox? = null
    private var actionBlock: LinearLayout? = null
    private var radioGroup: RadioGroup? = null
    private var callAction: RoboRadioButton? = null
    private var messageAction: RoboRadioButton? = null
    private var selectNumber: ThemedImageButton? = null
    private var numberView: RoboEditText? = null
    private var mImm: InputMethodManager? = null

    private var mActivity: Activity? = null

    private var listener: OnActionListener? = null

    var type: Int
        get() = if (hasAction()) {
            if (callAction!!.isChecked) {
                TYPE_CALL
            } else {
                TYPE_MESSAGE
            }
        } else {
            0
        }
        set(type) = if (type == TYPE_CALL) {
            callAction!!.isChecked = true
        } else {
            messageAction!!.isChecked = true
        }

    var number: String
        get() = numberView!!.text!!.toString().trim { it <= ' ' }
        set(number) = numberView!!.setText(number)

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
        View.inflate(context, R.layout.view_action, this)
        orientation = LinearLayout.VERTICAL
        actionBlock = findViewById(R.id.actionBlock)
        actionBlock!!.visibility = View.GONE
        selectNumber = findViewById(R.id.selectNumber)

        numberView = findViewById(R.id.numberView)
        numberView!!.isFocusableInTouchMode = true
        numberView!!.setOnFocusChangeListener { _, hasFocus ->
            mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!hasFocus) {
                mImm!!.hideSoftInputFromWindow(numberView!!.windowToken, 0)
            } else {
                mImm!!.showSoftInput(numberView, 0)
            }
        }
        numberView!!.setOnClickListener {
            mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!mImm!!.isActive(numberView)) {
                mImm!!.showSoftInput(numberView, 0)
            }
        }
        radioGroup = findViewById(R.id.radioGroup)
        radioGroup!!.setOnCheckedChangeListener { _, i -> buttonClick(i) }
        callAction = findViewById(R.id.callAction)
        callAction!!.isChecked = true
        messageAction = findViewById(R.id.messageAction)
        actionCheck = findViewById(R.id.actionCheck)
        actionCheck!!.setOnCheckedChangeListener { _, b ->
            if (!Permissions.checkPermission(mActivity!!, Permissions.READ_CONTACTS)) {
                actionCheck!!.isChecked = false
                Permissions.requestPermission(mActivity!!, REQ_CONTACTS, Permissions.READ_CONTACTS)
                return@setOnCheckedChangeListener
            }
            if (b) {
                openAction()
            } else {
                ViewUtils.hideOver(actionBlock!!)
            }
            if (listener != null) {
                listener!!.onActionChange(b)
            }
        }
        if (actionCheck!!.isChecked) {
            openAction()
        }
    }

    private fun openAction() {
        ViewUtils.showOver(actionBlock!!)
        refreshState()
    }

    private fun refreshState() {
        buttonClick(radioGroup!!.checkedRadioButtonId)
    }

    private fun buttonClick(i: Int) {
        when (i) {
            R.id.callAction -> if (listener != null) {
                listener!!.onTypeChange(false)
            }
            R.id.messageAction -> if (listener != null) {
                listener!!.onTypeChange(true)
            }
        }
    }

    fun setActivity(activity: Activity) {
        this.mActivity = activity
    }

    fun setContactClickListener(contactClickListener: View.OnClickListener) {
        selectNumber!!.setOnClickListener(contactClickListener)
    }

    fun setListener(listener: OnActionListener) {
        this.listener = listener
    }

    fun hasAction(): Boolean {
        return actionCheck!!.isChecked
    }

    fun setAction(action: Boolean) {
        actionCheck!!.isChecked = action
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        when (requestCode) {
            REQ_CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionCheck!!.isChecked = true
            }
        }
    }

    interface OnActionListener {
        fun onActionChange(hasAction: Boolean)

        fun onTypeChange(isMessageType: Boolean)
    }

    companion object {

        const val TYPE_CALL = 1
        const val TYPE_MESSAGE = 2
        private const val REQ_CONTACTS = 32564
    }
}
