package com.elementary.tasks.notes.preview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.notes.create.NoteImage
import kotlinx.android.synthetic.main.fragment_image.view.*
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher

class PhotoPagerAdapter(private val mPhotosUrl: List<NoteImage>) : PagerAdapter() {

    override fun getCount(): Int {
        return mPhotosUrl.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = LayoutInflater.from(container.context).inflate(R.layout.fragment_image, container, false)
        val imageView = binding.iv_photo
        loadPhoto(imageView, position)
        val mAttacher = PhotoViewAttacher(imageView)
        mAttacher.scaleType = ImageView.ScaleType.CENTER_INSIDE
        container.addView(binding)
        return binding
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as PhotoView)
    }

    private fun loadPhoto(imageView: ImageView, position: Int) {
        val image = mPhotosUrl[position]
        Glide.with(imageView.context.applicationContext)
                .load(image.image)
                .into(imageView)
    }
}