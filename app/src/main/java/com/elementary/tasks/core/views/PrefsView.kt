package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import kotlinx.android.synthetic.main.view_prefs.view.*
import java.util.*

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
class PrefsView : RelativeLayout {

    private lateinit var checkBox: CheckBox
    private lateinit var switchView: SwitchCompat
    private lateinit var title: TextView
    private lateinit var detail: TextView
    private lateinit var prefsValue: TextView
    private lateinit var dividerTop: View
    private lateinit var dividerBottom: View
    private lateinit var prefsView: View

    var isChecked: Boolean = false
        set(checked) {
            field = checked
            if (viewType == CHECK)
                checkBox.isChecked = checked
            else if (viewType == SWITCH) switchView.isChecked = checked
            refreshDetailText()
            for (listener in mOnCheckedListeners) {
                listener.onCheckedChange(checked)
            }
        }
    private var isForPro: Boolean = false
    private var isTest: Boolean = false
    private var viewType = CHECK
    private var mOnText: String? = null
    private var mOffText: String? = null
    private var mSecondaryText: String? = null
    private val mDependencyViews = ArrayList<PrefsView>()
    private val mReverseDependencyViews = ArrayList<PrefsView>()
    private val mOnCheckedListeners = ArrayList<OnCheckedListener>()

    private val isCheckable: Boolean
        get() = viewType == CHECK || viewType == SWITCH

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
        View.inflate(context, R.layout.view_prefs, this)
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        title = findViewById(R.id.prefsPrimaryText)
        detail = findViewById(R.id.prefsSecondaryText)
        prefsValue = findViewById(R.id.prefsValue)
        checkBox = findViewById(R.id.prefsCheck)
        dividerTop = findViewById(R.id.dividerTop)
        dividerBottom = findViewById(R.id.dividerBottom)
        prefsView = findViewById(R.id.prefsView)
        switchView = findViewById(R.id.prefs_switch)
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs, R.styleable.PrefsView, 0, 0)
            var titleText = ""
            var valueText = ""
            var divTop = false
            var divBottom = false
            var res = 0
            var iconId = 0
            try {
                titleText = a.getString(R.styleable.PrefsView_prefs_primary_text) ?: ""
                mSecondaryText = a.getString(R.styleable.PrefsView_prefs_secondary_text)
                mOnText = a.getString(R.styleable.PrefsView_prefs_secondary_text_on)
                mOffText = a.getString(R.styleable.PrefsView_prefs_secondary_text_off)
                valueText = a.getString(R.styleable.PrefsView_prefs_value_text) ?: ""
                divTop = a.getBoolean(R.styleable.PrefsView_prefs_divider_top, false)
                divBottom = a.getBoolean(R.styleable.PrefsView_prefs_divider_bottom, false)
                isForPro = a.getBoolean(R.styleable.PrefsView_prefs_pro, false)
                isTest = a.getBoolean(R.styleable.PrefsView_prefs_isTest, false)
                viewType = a.getInt(R.styleable.PrefsView_prefs_type, CHECK)
                res = a.getInt(R.styleable.PrefsView_prefs_view_resource, 0)
                iconId = a.getResourceId(R.styleable.PrefsView_prefs_icon, 0)
                val primaryColor = a.getColor(R.styleable.PrefsView_prefs_primary_text_color, -1)
                if (primaryColor != -1) {
                    title.setTextColor(primaryColor)
                }
            } catch (e: Exception) {
                LogUtil.e("PrefsView", "There was an error loading attributes.", e)
            } finally {
                a.recycle()
            }
            if (iconId != 0 && SHOW_ICON) {
                iconView.visibility = View.VISIBLE
                iconView.setImageResource(iconId)
            } else {
                iconView.visibility = View.GONE
            }
            setTitleText(titleText)
            setDividerTop(divTop)
            setDividerBottom(divBottom)
            setView()
            setValueText(valueText)
            setViewResource(res)
        }
        setDetailText(mSecondaryText)
        isChecked = isChecked
        setVisible()
    }

    fun setOnCheckedListener(listener: OnCheckedListener) {
        this.mOnCheckedListeners.add(listener)
    }

    fun setForPro(forPro: Boolean) {
        isForPro = forPro
        setVisible()
    }

    fun setDependentView(view: PrefsView) {
        mDependencyViews.add(view)
        view.setOnCheckedListener(object : OnCheckedListener {
            override fun onCheckedChange(checked: Boolean) {
                checkDependency()
            }
        })
        checkDependency()
    }

    private fun checkDependency() {
        var enable = true
        for (prefsView in mDependencyViews) {
            if (!prefsView.isChecked) {
                enable = false
                break
            }
        }
        isEnabled = enable
    }

    fun setReverseDependentView(view: PrefsView) {
        mReverseDependencyViews.add(view)
        view.setOnCheckedListener(object : OnCheckedListener {
            override fun onCheckedChange(checked: Boolean) {
                checkReverseDependency()
            }
        })
        checkReverseDependency()
    }

    private fun checkReverseDependency() {
        var enable = true
        for (prefsView in mReverseDependencyViews) {
            if (prefsView.isChecked) {
                enable = false
                break
            }
        }
        isEnabled = enable
    }

    private fun setVisible() {
        visibility = if (isTest) {
            if (BuildConfig.DEBUG) {
                View.VISIBLE
            } else {
                View.GONE
            }
        } else if (isForPro) {
            if (Module.isPro) {
                View.VISIBLE
            } else {
                View.GONE
            }
        } else {
            View.VISIBLE
        }
    }

    private fun setView() {
        hideAll()
        when (viewType) {
            CHECK -> checkBox.visibility = View.VISIBLE
            SWITCH -> switchView.visibility = View.VISIBLE
            TEXT -> prefsValue.visibility = View.VISIBLE
            VIEW -> prefsView.visibility = View.VISIBLE
        }
    }

    private fun hideAll() {
        checkBox.visibility = View.GONE
        switchView.visibility = View.GONE
        prefsValue.visibility = View.GONE
        prefsView.visibility = View.GONE
    }

    private fun setTitleText(text: String) {
        title.text = text
    }

    fun setDetailText(text: String?) {
        if (isCheckable && hasOnOff()) {
            if (isChecked) {
                detail.text = mOnText
            } else {
                detail.text = mOffText
            }
            detail.visibility = View.VISIBLE
        } else {
            if (TextUtils.isEmpty(text)) {
                detail.visibility = View.GONE
            } else {
                detail.text = text
                detail.visibility = View.VISIBLE
            }
        }
    }

    fun setValue(value: Int) {
        prefsValue.text = value.toString()
    }

    fun setValueText(text: String) {
        prefsValue.text = text
    }

    fun setViewColor(@ColorInt color: Int) {
        if (color != 0) {
            prefsView.setBackgroundColor(color)
        }
    }

    fun setViewResource(@DrawableRes resource: Int) {
        if (resource != 0) {
            val drawableTop: Drawable? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.getDrawable(resource)
            } else {
                AppCompatResources.getDrawable(context, resource)
            }
            prefsView.background = drawableTop
        }
    }

    fun setViewDrawable(drawable: Drawable?) {
        if (drawable != null) {
            prefsView.background = drawable
        }
    }

    private fun refreshDetailText() {
        if (isCheckable && hasOnOff()) {
            setDetailText(mSecondaryText)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        switchView.isEnabled = enabled
        checkBox.isEnabled = enabled
        prefsView.isEnabled = enabled
        prefsValue.isEnabled = enabled
        detail.isEnabled = enabled
        title.isEnabled = enabled
        iconView.isEnabled = enabled
    }

    private fun setDividerTop(divider: Boolean) {
        if (divider) {
            dividerTop.visibility = View.VISIBLE
        } else {
            dividerTop.visibility = View.GONE
        }
    }

    private fun setDividerBottom(divider: Boolean) {
        if (divider) {
            dividerBottom.visibility = View.VISIBLE
        } else {
            dividerBottom.visibility = View.GONE
        }
    }

    private fun hasOnOff(): Boolean {
        return mOffText != null && mOnText != null
    }

    interface OnCheckedListener {
        fun onCheckedChange(checked: Boolean)
    }

    companion object {

        private const val CHECK = 0
        private const val SWITCH = 1
        private const val VIEW = 2
        private const val TEXT = 3
        private const val SHOW_ICON: Boolean = true
    }
}
