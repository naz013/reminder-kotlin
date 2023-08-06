package com.elementary.tasks.notes.preview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.databinding.FragmentImageBinding
import com.github.chrisbanes.photoview.PhotoView

class PhotoPagerAdapter(private val images: List<UiNoteImage>) : PagerAdapter() {

  override fun getCount(): Int {
    return images.size
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view == `object`
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val binding = FragmentImageBinding.inflate(
      LayoutInflater.from(container.context),
      container,
      false
    )
    loadPhoto(binding.ivPhoto, position)
    container.addView(binding.root)
    return binding.root
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as PhotoView)
  }

  private fun loadPhoto(imageView: ImageView, position: Int) {
    imageView.load(images[position].filePath)
  }
}
