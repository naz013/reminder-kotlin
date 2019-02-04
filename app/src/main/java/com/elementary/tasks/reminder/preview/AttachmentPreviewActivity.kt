package com.elementary.tasks.reminder.preview

import android.net.Uri
import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityAttachmentPreviewBinding
import com.squareup.picasso.Picasso
import java.io.File

/**
 * Copyright 2019 Nazar Suhovich
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
class AttachmentPreviewActivity : ThemedActivity<ActivityAttachmentPreviewBinding>() {

    override fun layoutRes(): Int = R.layout.activity_attachment_preview

    override fun onCreate(savedInstanceState: Bundle?) {
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            exitTransition = Explode()
        }
        super.onCreate(savedInstanceState)
        initActionBar()
        showImages()
    }

    private fun showImages() {
        val path = intent.getStringExtra(Constants.INTENT_ITEM) ?: ""
        binding.toolbar.title = path
        val file = File(path)
        if (file.exists()) {
            Picasso.get().load(file).into(binding.ivPhoto)
        } else {
            val uri = Uri.parse(path)
            binding.toolbar.title = uri.lastPathSegment
            Picasso.get()
                    .load(uri)
                    .into(binding.ivPhoto)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
        binding.toolbar.title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
