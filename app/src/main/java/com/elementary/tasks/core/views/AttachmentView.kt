package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.view_attachment.view.*
import timber.log.Timber
import java.io.File

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
class AttachmentView : LinearLayout {

    var onFileUpdateListener: ((path: String) -> Unit)? = null
    var onFileSelectListener: (() -> Unit)? = null
    var file: String = ""
        set(value) {
            field = value
            if (value != "") {
                val file = File(value)
                if (file.exists()) {
                    text.text = file.name
                    removeButton.visibility = View.VISIBLE
                    onFileUpdateListener?.invoke(value)
                } else {
                    noFile()
                }
            } else {
                noFile()
            }
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

    private fun noFile() {
        removeButton.visibility = View.GONE
        text.text = context.getString(R.string.not_selected)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_attachment, this)
        orientation = LinearLayout.VERTICAL

        removeButton.setOnClickListener {
            file = ""
        }
        text.setOnClickListener {
            Timber.d("init: $file")
            if (file == "") {
                onFileSelectListener?.invoke()
            }
        }
        file = ""
    }
}
