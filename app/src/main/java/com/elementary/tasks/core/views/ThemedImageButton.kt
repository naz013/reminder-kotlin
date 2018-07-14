package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.ThemeUtil

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

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        this.mAttrs = attrs
        restore()
    }

    private fun restore() {
        if (mAttrs != null) {
            val a = context.theme.obtainStyledAttributes(mAttrs, R.styleable.ThemedImageButton, 0, 0)
            try {
                val icon: Int = if (ThemeUtil.getInstance(context).isDark) {
                    a.getResourceId(R.styleable.ThemedImageButton_tb_icon_light, 0)
                } else {
                    a.getResourceId(R.styleable.ThemedImageButton_tb_icon, 0)
                }
                setImageResource(icon)
                val message = a.getString(R.styleable.ThemedImageButton_tb_message)
                if (message != null) {
                    setOnLongClickListener { showMessage(message) }
                }
            } catch (e: Exception) {
                LogUtil.d(TAG, "There was an error loading attributes.")
            } finally {
                a.recycle()
            }
        }
    }

    private fun showMessage(message: String?): Boolean {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        return true
    }

    companion object {

        private const val TAG = "ThemedImageButton"
    }
}
