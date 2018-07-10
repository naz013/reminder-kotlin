package com.elementary.tasks.core.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.ThemeUtil

import androidx.appcompat.widget.AppCompatImageButton

/**
 * Copyright 2017 Nazar Suhovich
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
class ThemedImageButton : AppCompatImageButton {
    private var mAttrs: AttributeSet? = null
    private var mContext: Context? = null

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
        this.mAttrs = attrs
        this.mContext = context
        restore()
    }

    fun restore() {
        if (mAttrs != null) {
            val a = mContext!!.theme.obtainStyledAttributes(mAttrs, R.styleable.ThemedImageButton, 0, 0)
            try {
                val icon: Int
                if (ThemeUtil.getInstance(mContext).isDark) {
                    icon = a.getResourceId(R.styleable.ThemedImageButton_tb_icon_light, 0)
                } else {
                    icon = a.getResourceId(R.styleable.ThemedImageButton_tb_icon, 0)
                }
                setImageResource(icon)
                val message = a.getString(R.styleable.ThemedImageButton_tb_message)
                if (message != null) {
                    setOnLongClickListener { v -> showMessage(message) }
                }
            } catch (e: Exception) {
                LogUtil.d(TAG, "There was an error loading attributes.")
            } finally {
                a.recycle()
            }
        }
    }

    private fun showMessage(message: String?): Boolean {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        return true
    }

    companion object {

        private val TAG = "ThemedImageButton"
    }
}
