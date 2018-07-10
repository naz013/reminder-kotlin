package com.elementary.tasks.notes.preview

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.elementary.tasks.databinding.FragmentImageBinding
import com.elementary.tasks.notes.create.NoteImage

import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher

internal class PhotoPagerAdapter(context: Context, private val mPhotosUrl: List<NoteImage>) : PagerAdapter() {

    private val mLayoutInflater: LayoutInflater


    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mPhotosUrl.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = FragmentImageBinding.inflate(mLayoutInflater, container, false)
        val imageView = binding.ivPhoto
        loadPhoto(imageView, position)
        val mAttacher = PhotoViewAttacher(imageView)
        mAttacher.scaleType = ImageView.ScaleType.CENTER_INSIDE
        container.addView(binding.root)
        return binding.root
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