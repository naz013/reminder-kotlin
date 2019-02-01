package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityImagePreviewBinding
import java.util.*
import javax.inject.Inject

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
class ImagePreviewActivity : ThemedActivity<ActivityImagePreviewBinding>() {

    @Inject
    lateinit var imagesSingleton: ImagesSingleton

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun layoutRes(): Int = R.layout.activity_image_preview

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
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
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
