package com.elementary.tasks.core.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.view_attachment.view.*
import timber.log.Timber

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
    private var content: String = ""
        private set(value) {
            field = value
            if (value != "") {
                text.text = Uri.parse(value).lastPathSegment
                removeButton.visibility = View.VISIBLE
                onFileUpdateListener?.invoke(value)
            } else {
                noFile()
            }
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    fun setUri(uri: Uri) {
        Timber.d("setUri: ${uri.path}")
        content = uri.toString()
    }

    private fun noFile() {
        removeButton.visibility = View.GONE
        text.text = context.getString(R.string.not_selected)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_attachment, this)
        orientation = LinearLayout.VERTICAL

        removeButton.setOnClickListener {
            content = ""
        }
        text.setOnClickListener {
            addClick()
        }
        hintIcon.setOnClickListener {
            addClick()
        }
        hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.attachment), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(hintIcon, context.getString(R.string.attachment))
        content = ""
    }

    private fun addClick() {
        Timber.d("init: $content")
        if (content == "") {
            onFileSelectListener?.invoke()
        }
    }
}
