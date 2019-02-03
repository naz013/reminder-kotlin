package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.MelodyViewBinding
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
class MelodyView : LinearLayout {

    private lateinit var binding: MelodyViewBinding
    var onFileUpdateListener: ((path: String) -> Unit)? = null
    var onFileSelectListener: (() -> Unit)? = null
    var file: String = ""
        set(value) {
            field = value
            if (value != "") {
                val file = File(value)
                if (file.exists()) {
                    binding.text.text = file.name
                    binding.removeButton.visibility = View.VISIBLE
                    onFileUpdateListener?.invoke(value)
                } else {
                    noFile()
                }
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

    private fun noFile() {
        binding.removeButton.visibility = View.GONE
        binding.text.text = context.getString(R.string.not_selected)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_melody, this)
        orientation = LinearLayout.VERTICAL
        binding = MelodyViewBinding(this)

        binding.hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.melody), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.melody))
        binding.removeButton.setOnClickListener {
            file = ""
        }
        binding.text.setOnClickListener {
            Timber.d("init: $file")
            if (file == "") {
                onFileSelectListener?.invoke()
            }
        }
        file = ""
    }
}
