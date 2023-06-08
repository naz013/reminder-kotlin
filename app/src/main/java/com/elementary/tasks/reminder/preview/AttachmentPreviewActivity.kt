package com.elementary.tasks.reminder.preview

import android.net.Uri
import android.os.Bundle
import android.transition.Explode
import android.view.Window
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.databinding.ActivityAttachmentPreviewBinding
import com.squareup.picasso.Picasso
import java.io.File

class AttachmentPreviewActivity : BindingActivity<ActivityAttachmentPreviewBinding>() {

  override fun inflateBinding() = ActivityAttachmentPreviewBinding.inflate(layoutInflater)

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
    binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
    binding.toolbar.title = ""
  }

  override fun handleBackPress(): Boolean {
    supportFinishAfterTransition()
    return true
  }
}
