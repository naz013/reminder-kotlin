package com.elementary.tasks.notes.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_image_edit.*

/**
 * Copyright 2017 Nazar Suhovich
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
class ImageEditActivity : ThemedActivity() {

    private var fragment: BitmapFragment? = null
    private lateinit var viewModel: NoteViewModel
    private var mNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_edit)
        initActionBar()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this,
                NoteViewModel.Factory(application, NotePreviewActivity.PREVIEW_IMAGES))
                .get(NoteViewModel::class.java)
        viewModel.note.observe(this, Observer{ note ->
            if (note != null) {
                showImage(note)
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> closeOk()
                }
            }
        })
    }

    private fun closeOk() {
        ImageSingleton.getInstance().item = null
        setResult(RESULT_OK)
        finish()
    }

    private fun showImage(note: Note) {
        mNote = note
        ImageSingleton.getInstance().item = note.images[0]
        initTabControl()
    }

    private fun initTabControl() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
        tabLayout.getTabAt(0)?.select()
        openCropFragment()
    }

    private fun selectTab(position: Int) {
        if (fragment != null) {
            if (fragment is CropFragment) {
                askCrop(position)
            } else if (fragment is DrawFragment) {
                askDraw(position)
            }
        } else {
            switchTab(position)
        }
    }

    private fun askDraw(position: Int) {
        val builder = dialogues.getDialog(this)
        builder.setMessage(R.string.which_image_you_want_to_use)
        builder.setPositiveButton(R.string.edited) { dialogInterface, _ ->
            dialogInterface.dismiss()
            ImageSingleton.getInstance().item = fragment!!.image
            switchTab(position)
        }
        builder.setNegativeButton(R.string.original) { dialogInterface, _ ->
            dialogInterface.dismiss()
            ImageSingleton.getInstance().item = fragment!!.originalImage
            switchTab(position)
        }
        builder.create().show()
    }

    private fun askCrop(position: Int) {
        val builder = dialogues.getDialog(this)
        builder.setMessage(R.string.which_image_you_want_to_use)
        builder.setPositiveButton(R.string.cropped) { dialogInterface, _ ->
            dialogInterface.dismiss()
            ImageSingleton.getInstance().item = fragment!!.image
            switchTab(position)
        }
        builder.setNegativeButton(R.string.original) { dialogInterface, _ ->
            dialogInterface.dismiss()
            ImageSingleton.getInstance().item = fragment!!.originalImage
            switchTab(position)
        }
        builder.create().show()
    }

    private fun switchTab(position: Int) {
        LogUtil.d(TAG, "switchTab: $position")
        if (position == 1) {
            openDrawFragment()
        } else {
            openCropFragment()
        }
    }

    private fun openDrawFragment() {
        replaceFragment(DrawFragment.newInstance())
    }

    private fun openCropFragment() {
        replaceFragment(CropFragment.newInstance())
    }

    fun replaceFragment(fragment: BitmapFragment) {
        this.fragment = fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment, null)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = getString(R.string.edit)
    }

    override fun onBackPressed() {
        if (fragment!!.onBackPressed()) {
            return
        }
        closeScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_palce_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                closeScreen()
                true
            }
            R.id.action_add -> {
                saveImage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun closeScreen() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (fragment != null) fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Module.isMarshmallow && fragment != null) {
            fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun saveImage() {
        val image = fragment?.image
        val note = mNote
        if (image != null && note != null) {
            note.images = listOf(image)
            viewModel.saveNote(note)
        }
    }

    companion object {
        private const val TAG = "ImageEditActivity"
    }
}
