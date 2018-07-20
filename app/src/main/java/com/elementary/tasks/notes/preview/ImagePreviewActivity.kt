package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import kotlinx.android.synthetic.main.activity_image_preview.*
import java.util.*

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

    private lateinit var viewModel: NoteViewModel
    private var mNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        initActionBar()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this,
                NoteViewModel.Factory(application, intent.getStringExtra(Constants.INTENT_ID)))
                .get(NoteViewModel::class.java)
        viewModel.note.observe(this, Observer{ note ->
            if (note != null) {
                initViewPager(note)
            }
        })
    }

    private fun setPhotoPosition() {
        val position = intent.getIntExtra(Constants.INTENT_POSITION, -1)
        if (position != -1)
            photo_pager.currentItem = position
    }

    private fun getAdapter(note: Note?): PhotoPagerAdapter {
        return if (note == null) {
            PhotoPagerAdapter(listOf())
        } else {
            PhotoPagerAdapter(note.images)
        }
    }

    private fun initViewPager(note: Note?) {
        this.mNote = note
        photo_pager.adapter = getAdapter(note)
        photo_pager.pageMargin = 5
        photo_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                setToolbarTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        if (note != null) {
            setToolbarTitle(photo_pager.currentItem)
        }
        setPhotoPosition()
    }

    private fun setToolbarTitle(position: Int) {
        toolbar.title = String.format(Locale.getDefault(), getString(R.string.x_out_of_x), position + 1, mNote!!.images.size)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = ""
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
        super.onDestroy()
        if (mNote != null) viewModel.deleteNote(mNote!!)
    }
}
