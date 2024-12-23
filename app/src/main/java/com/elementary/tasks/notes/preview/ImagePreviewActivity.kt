package com.elementary.tasks.notes.preview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.github.naz013.ui.common.activity.BindingActivity
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyTopInsets
import com.elementary.tasks.databinding.ActivityImagePreviewBinding
import org.koin.android.ext.android.inject
import java.util.Locale

class ImagePreviewActivity : BindingActivity<ActivityImagePreviewBinding>() {

  private val imagesSingleton by inject<ImagesSingleton>()

  override fun inflateBinding() = ActivityImagePreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    binding.photoPager.applyBottomInsets()
    initActionBar()
    updateBackground()
    showImages()
  }

  private fun updateBackground() {
    val color = imagesSingleton.getColor()
    if (color == -1) {
      return
    }
    window.statusBarColor = color
    window.navigationBarColor = color
    binding.containerView.setBackgroundColor(color)
  }

  private fun showImages() {
    val images = imagesSingleton.getCurrent()
    if (images.isNotEmpty()) {
      initViewPager(images)
    }
  }

  private fun setPhotoPosition() {
    val position = intent.getIntExtra(IntentKeys.INTENT_POSITION, -1)
    if (position != -1) {
      binding.photoPager.currentItem = position
    }
  }

  private fun initViewPager(images: List<UiNoteImage>) {
    binding.photoPager.adapter = PhotoPagerAdapter(images)
    binding.photoPager.pageMargin = 5
    binding.photoPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) {
        setToolbarTitle(position)
      }

      override fun onPageScrollStateChanged(state: Int) {
      }
    })
    setToolbarTitle(binding.photoPager.currentItem)
    setPhotoPosition()
  }

  private fun setToolbarTitle(position: Int) {
    binding.toolbar.title = String.format(
      Locale.getDefault(),
      getString(R.string.x_out_of_x),
      position + 1,
      imagesSingleton.getCurrent().size
    )
  }

  private fun initActionBar() {
    binding.appBar.applyTopInsets()
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.title = ""
  }

  override fun onDestroy() {
    imagesSingleton.clear()
    super.onDestroy()
  }
}
