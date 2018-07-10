package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.databinding.ActivityImagePreviewBinding

import java.util.ArrayList
import java.util.Locale
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager

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
class ImagePreviewActivity : ThemedActivity() {

    private var binding: ActivityImagePreviewBinding? = null
    private var viewModel: NoteViewModel? = null
    private var mNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview)
        initActionBar()

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, NoteViewModel.Factory(application, intent.getStringExtra(Constants.INTENT_ID))).get(NoteViewModel::class.java)
        viewModel!!.note.observe(this, { note ->
            if (note != null) {
                initViewPager(note)
            }
        })
    }

    private fun setPhotoPosition() {
        val position = intent.getIntExtra(Constants.INTENT_POSITION, -1)
        if (position != -1)
            binding!!.photoPager.currentItem = position
    }

    private fun getAdapter(note: Note?): PhotoPagerAdapter {
        return if (note == null) {
            PhotoPagerAdapter(this, ArrayList<NoteImage>())
        } else {
            PhotoPagerAdapter(this, note.images)
        }
    }

    private fun initViewPager(note: Note?) {
        this.mNote = note
        binding!!.photoPager.adapter = getAdapter(note)
        binding!!.photoPager.pageMargin = 5
        binding!!.photoPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                setToolbarTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        if (note != null) {
            setToolbarTitle(binding!!.photoPager.currentItem)
        }
        setPhotoPosition()
    }

    private fun setToolbarTitle(position: Int) {
        binding!!.toolbar.title = String.format(Locale.getDefault(), getString(R.string.x_out_of_x), position + 1, mNote!!.images.size)
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.toolbar.title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mNote != null) viewModel!!.deleteNote(mNote!!)
    }
}
