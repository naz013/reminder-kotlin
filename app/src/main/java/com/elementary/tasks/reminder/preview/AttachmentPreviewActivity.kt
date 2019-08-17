package com.elementary.tasks.reminder.preview

import android.net.Uri
import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import coil.api.load
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityAttachmentPreviewBinding
import java.io.File

class AttachmentPreviewActivity : BindingActivity<ActivityAttachmentPreviewBinding>(R.layout.activity_attachment_preview) {

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
            binding.ivPhoto.load(file) {
                crossfade(true)
                lifecycle(lifecycle)
            }
        } else {
            val uri = Uri.parse(path)
            binding.toolbar.title = uri.lastPathSegment
            binding.ivPhoto.load(uri) {
                crossfade(true)
                lifecycle(lifecycle)
            }
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
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
