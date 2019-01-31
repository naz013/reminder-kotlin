package com.elementary.tasks.core.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.AttachmentViewBinding
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
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

    private lateinit var binding: AttachmentViewBinding
    var onFileUpdateListener: ((path: String) -> Unit)? = null
    var onFileSelectListener: (() -> Unit)? = null
    private var content: String = ""
        private set(value) {
            field = value
            if (value != "") {
                binding.text.text = Uri.parse(value).lastPathSegment
                binding.removeButton.show()
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
        binding.removeButton.hide()
        binding.text.text = context.getString(R.string.not_selected)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_attachment, this)
        orientation = LinearLayout.VERTICAL
        binding = AttachmentViewBinding(this)

        binding.removeButton.setOnClickListener {
            content = ""
        }
        binding.text.setOnClickListener {
            addClick()
        }
        binding.hintIcon.setOnClickListener {
            addClick()
        }
        binding.hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.attachment), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.attachment))
        content = ""
    }

    private fun addClick() {
        Timber.d("init: $content")
        if (content == "") {
            onFileSelectListener?.invoke()
        }
    }
}
