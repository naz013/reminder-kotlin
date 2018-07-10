package com.elementary.tasks.core.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeUtil

import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatRadioButton

import android.view.Gravity.CENTER_HORIZONTAL

class IconRadioButton : AppCompatRadioButton {

    private var isDark = false
    private var isCheckable = false
    private var selectedBg: Int = 0
    private var icon: Int = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val themeUtil = ThemeUtil.getInstance(context)
        isDark = themeUtil.isDark
        selectedBg = resources.getColor(themeUtil.colorAccent())
        if (Module.isMarshmallow) {
            setTextAppearance(android.R.style.TextAppearance_Small)
        } else {
            setTextAppearance(context, android.R.style.TextAppearance_Small)
        }
        maxLines = 1
        setButtonDrawable(android.R.color.transparent)
        gravity = CENTER_HORIZONTAL
        setPadding(31, 0, 0, 0)
        setSingleLine(true)
        var isChecked = false
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.IconRadioButton, 0, 0)
            var titleText: String? = ""
            try {
                titleText = a.getString(R.styleable.IconRadioButton_ir_text)
                val textSize = a.getDimension(R.styleable.IconRadioButton_ir_text_size, 16f)
                isChecked = a.getBoolean(R.styleable.IconRadioButton_ir_checked, false)
                isCheckable = a.getBoolean(R.styleable.IconRadioButton_ir_checkable, true)
                if (isDark) {
                    icon = a.getResourceId(R.styleable.IconRadioButton_ir_icon_light, 0)
                } else {
                    icon = a.getResourceId(R.styleable.IconRadioButton_ir_icon, 0)
                }
                setTextSize(textSize)
            } catch (e: Exception) {
                LogUtil.d(TAG, "There was an error loading attributes.")
            } finally {
                a.recycle()
            }
            text = titleText
        }
        if (isCheckable) {
            setChecked(isChecked)
        }
        if (icon != 0) {
            setTopIcon()
        }
        refreshView()
    }

    private fun setTopIcon() {
        val drawableTop: Drawable?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawableTop = context.getDrawable(icon)
        } else {
            drawableTop = AppCompatResources.getDrawable(context, icon)
        }
        setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
    }

    fun getIcon(): Int {
        return icon
    }

    fun setIcon(icon: Int) {
        if (icon == 0) {
            return
        }
        this.icon = icon
        setTopIcon()
    }

    private fun refreshView() {
        if (!isEnabled) {
            setTextColor(resources.getColor(R.color.material_divider))
            return
        }
        if (isChecked) {
            setTextColor(selectedBg)
        } else {
            if (isDark) {
                setTextColor(resources.getColor(R.color.material_white))
            } else {
                setTextColor(resources.getColor(R.color.material_grey))
            }
        }
    }

    override fun setChecked(checked: Boolean) {
        if (!isCheckable) {
            return
        }
        super.setChecked(checked)
        refreshView()
    }

    override fun toggle() {
        if (!isCheckable) {
            return
        }
        if (!isChecked) {
            isChecked = true
        }
    }

    companion object {

        private val TAG = "IconRadioButton"
    }
}
