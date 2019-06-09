package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityImagePreviewBinding
import org.koin.android.ext.android.inject
import java.util.*

class ImagePreviewActivity : BindingActivity<ActivityImagePreviewBinding>(R.layout.activity_image_preview) {

    private val imagesSingleton: ImagesSingleton by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()

        showImages()
    }

    private fun showImages() {
        val images = imagesSingleton.getCurrent()
        if (images.isNotEmpty()) {
            initViewPager(images)
        }
    }

    private fun setPhotoPosition() {
        val position = intent.getIntExtra(Constants.INTENT_POSITION, -1)
        if (position != -1)
            binding.photoPager.currentItem = position
    }

    private fun initViewPager(images: List<ImageFile>) {
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
        binding.toolbar.title = String.format(Locale.getDefault(), getString(R.string.x_out_of_x),
                position + 1, imagesSingleton.getCurrent().size)
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
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        imagesSingleton.clear()
        super.onDestroy()
    }
}
