package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.WindowTypeViewBinding
import com.google.android.material.chip.Chip

class WindowTypeView : LinearLayout {

    private lateinit var binding: WindowTypeViewBinding
    var onTypeChaneListener: ((Int) -> Unit)? = null
    var windowType: Int = 2
        set(value) {
            field = value
            binding.chipGroup.check(chipIdFromType(value))
        }
        get() {
            return typeFromChip(binding.chipGroup.checkedChipId)
        }
    private var mLastIdRes: Int = R.id.chipFullscreen

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun chipIdFromType(id: Int): Int {
        return when (id) {
            0 -> R.id.chipFullscreen
            1 -> R.id.chipSimple
            else -> R.id.chipFullscreen
        }
    }

    private fun typeFromChip(id: Int): Int {
        mLastIdRes = id
        return when (id) {
            R.id.chipFullscreen -> 0
            R.id.chipSimple -> 1
            else -> 0
        }
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_window_type, this)
        orientation = VERTICAL
        binding = WindowTypeViewBinding(this)

        binding.hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.notification_type), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.notification_type))
        binding.chipGroup.setOnCheckedChangeListener { _, id ->
            if (isAnyChecked()) {
                updateState(typeFromChip(id))
            } else {
                chipView(mLastIdRes).isChecked = true
                updateState(typeFromChip(mLastIdRes))
            }
        }
    }

    private fun chipView(@IdRes id: Int): Chip {
        return when (id) {
            R.id.chipFullscreen -> binding.chipFullscreen
            R.id.chipSimple -> binding.chipSimple
            else -> binding.chipFullscreen
        }
    }

    private fun isAnyChecked(): Boolean {
        return binding.chipFullscreen.isChecked || binding.chipSimple.isChecked
    }

    private fun updateState(type: Int) {
        onTypeChaneListener?.invoke(type)
    }
}
