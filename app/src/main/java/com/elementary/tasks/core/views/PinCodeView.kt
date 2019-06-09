package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.PinCodeViewBinding

class PinCodeView : LinearLayout, View.OnClickListener {

    private lateinit var binding: PinCodeViewBinding
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
        orientation = VERTICAL
        View.inflate(context, R.layout.view_pin_code, this)
        binding = PinCodeViewBinding(this)

        binding.deleteButton.setOnClickListener {
            pinString = pinString.substring(0, pinString.length - 1)
            updateTimeView()
        }
        binding.deleteButton.setOnLongClickListener {
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
        binding.b1.id = Integer.valueOf(101)
        binding.b2.id = Integer.valueOf(102)
        binding.b3.id = Integer.valueOf(103)
        binding.b4.id = Integer.valueOf(104)
        binding.b5.id = Integer.valueOf(105)
        binding.b6.id = Integer.valueOf(106)
        binding.b7.id = Integer.valueOf(107)
        binding.b8.id = Integer.valueOf(108)
        binding.b9.id = Integer.valueOf(109)
        binding.b0.id = Integer.valueOf(100)
        binding.b1.setOnClickListener(this)
        binding.b2.setOnClickListener(this)
        binding.b3.setOnClickListener(this)
        binding.b4.setOnClickListener(this)
        binding.b5.setOnClickListener(this)
        binding.b6.setOnClickListener(this)
        binding.b7.setOnClickListener(this)
        binding.b8.setOnClickListener(this)
        binding.b9.setOnClickListener(this)
        binding.b0.setOnClickListener(this)
    }

    private fun updateTimeView() {
        binding.deleteButton.isEnabled = pinString.isNotEmpty()
        clearBirds()
        showBirds()
    }

    private fun showBirds() {
        for(i in 0 until pinString.length) {
            binding.birdsView.getChildAt(i)?.visibility = View.VISIBLE
        }
    }

    private fun clearBirds() {
        for(child in binding.birdsView.children) {
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
