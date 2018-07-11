package com.elementary.tasks.navigation.settings.images

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast

import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.MainImage
import com.elementary.tasks.core.network.Api
import com.elementary.tasks.core.network.RetrofitBuilder
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.UriUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.main_image.MainImagesViewModel
import com.elementary.tasks.core.views.roboto.RoboRadioButton
import com.elementary.tasks.databinding.ActivityMainImageBinding

import java.io.File
import java.util.ArrayList
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
class MainImageActivity : ThemedActivity(), CompoundButton.OnCheckedChangeListener {

    private var binding: ActivityMainImageBinding? = null
    private var imagesList: RecyclerView? = null
    private var mAdapter: ImagesRecyclerAdapter? = null

    private var mPhotoList: List<MainImage>? = ArrayList()
    private var mPointer: Int = 0

    private var position = -1
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mSelectedItem: MainImage? = null
    private var viewModel: MainImagesViewModel? = null

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as GridLayoutManager?
            val visiblePosition = layoutManager!!.findLastVisibleItemPosition()
            val count = mAdapter!!.itemCount
            if (visiblePosition >= count - 10 && mPointer < mPhotoList!!.size - 1 && mPointer < count + START_SIZE / 2 - 1) {
                var endPoint = mPointer + START_SIZE / 2
                val last = endPoint >= mPhotoList!!.size
                if (last) endPoint = mPhotoList!!.size - 1
                val nextChunk = mPhotoList!!.subList(mPointer, endPoint)
                mPointer += START_SIZE / 2
                if (last) mPointer = mPhotoList!!.size - 1
                mAdapter!!.addItems(nextChunk)
            }
        }
    }
    private var mCall: Call<List<MainImage>>? = null
    private val mPhotoCallback = object : Callback<List<MainImage>> {
        override fun onResponse(call: Call<List<MainImage>>, response: Response<List<MainImage>>) {
            LogUtil.d(TAG, "onResponse: " + response.code() + ", " + response.message())
            if (response.code() == Api.OK) {
                if (mPhotoList!!.isEmpty() && response.body() != null) {
                    mPhotoList = ArrayList(response.body()!!)
                    saveToDb(response.body())
                    loadDataToList()
                } else {
                    saveToDb(response.body())
                }
            }
        }

        override fun onFailure(call: Call<List<MainImage>>, t: Throwable) {
            LogUtil.d(TAG, "onFailure: " + t.localizedMessage)
        }
    }

    private val mListener = object : SelectListener {
        override fun onImageSelected(b: Boolean) {
            if (b) {
                binding!!.selectGroup.clearCheck()
            } else {
                (findViewById<View>(R.id.defaultCheck) as RoboRadioButton).isChecked = true
            }
        }

        override fun deselectOverItem(position: Int) {

        }

        override fun onItemLongClicked(position: Int, view: View) {
            showImage(position)
        }
    }
    private val mAnimationCallback = { code ->
        if (code == 0) {
            binding!!.fullImageView.setImageBitmap(null)
            mSelectedItem = null
        }
    }

    private fun saveToDb(body: List<MainImage>?) {
        viewModel!!.saveImages(body)
    }

    private fun loadDataToList() {
        mPointer = START_SIZE - 1
        mAdapter = ImagesRecyclerAdapter(this, mPhotoList!!.subList(0, mPointer), mListener)
        mAdapter!!.setPrevSelected(position)
        imagesList!!.adapter = mAdapter
        binding!!.emptyLayout.emptyItem.visibility = View.GONE
        imagesList!!.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_image)
        initActionBar()
        initRadios()
        binding!!.emptyLayout.emptyItem.visibility = View.VISIBLE
        binding!!.emptyLayout.emptyText.setText(R.string.no_images)
        initRecyclerView()
        initImageContainer()

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainImagesViewModel::class.java)
        viewModel!!.images.observe(this, { mainImages ->
            if (mainImages != null) {
                mPhotoList = mainImages
            }
            initData()
        })
    }

    private fun initData() {
        if (!mPhotoList!!.isEmpty()) {
            loadDataToList()
        }
        mCall = RetrofitBuilder.unsplashApi.allImages
        mCall!!.enqueue(mPhotoCallback)
    }

    private fun initRadios() {
        binding!!.defaultCheck.setOnCheckedChangeListener(this)
        binding!!.noneCheck.setOnCheckedChangeListener(this)
        position = prefs!!.imageId
        val path = prefs!!.imagePath
        if (path!!.matches(NONE_PHOTO.toRegex())) {
            binding!!.noneCheck.isChecked = true
        } else if (position == -1 || path.matches(DEFAULT_PHOTO.toRegex())) {
            binding!!.defaultCheck.isChecked = true
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.toolbar.title = getString(R.string.main_image)
    }

    private fun initImageContainer() {
        binding!!.imageContainer.visibility = View.GONE
        binding!!.fullContainer.visibility = View.GONE
        binding!!.fullContainer.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                hideImage()
            }
            true
        }
        binding!!.fullImageView.setOnTouchListener { view, motionEvent -> true }
        binding!!.downloadButton.setOnClickListener { view -> showDownloadDialog() }
        binding!!.setToMonthButton.setOnClickListener { view -> showMonthDialog() }
        binding!!.setReminderButton.setOnClickListener { v -> setToReminder() }
        if (!prefs!!.isCalendarImagesEnabled) {
            binding!!.setToMonthButton.visibility = View.GONE
        }
    }

    private fun setToReminder() {
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(this, REQUEST_REMINDER, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)
            return
        }
        if (mSelectedItem != null && MemoryUtil.isSdPresent) {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val imageFile = File(folder, mSelectedItem!!.filename!!)
            val destination = UriUtil.getUri(this, imageFile)
            prefs!!.reminderImage = destination.toString()
            DownloadAsync(this, mSelectedItem!!.filename, imageFile.toString(), 1080, 1920, mSelectedItem!!.id).execute()
        } else {
            Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show()
            return
        }
        hideImage()
    }

    private fun showMonthDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setItems(R.array.month_list) { dialogInterface, i -> setImageForMonth(i) }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDownloadDialog() {
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, REQUEST_DOWNLOAD, Permissions.WRITE_EXTERNAL)
            return
        }
        if (mSelectedItem == null) return
        val builder = Dialogues.getDialog(this)
        val maxSize = mSelectedItem!!.height.toString() + "x" + mSelectedItem!!.width
        builder.setItems(arrayOf<CharSequence>(maxSize, "1080x1920", "768x1280", "480x800")) { dialogInterface, i ->
            var width = mSelectedItem!!.width
            var height = mSelectedItem!!.height
            when (i) {
                1 -> {
                    width = 1080
                    height = 1920
                }
                2 -> {
                    width = 768
                    height = 1280
                }
                3 -> {
                    width = 480
                    height = 800
                }
            }
            downloadImage(width, height)
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showImage(position: Int) {
        mSelectedItem = mPhotoList!![position]
        if (mSelectedItem != null) {
            binding!!.photoInfoView.text = getString(R.string.number) + mSelectedItem!!.id + " " + mSelectedItem!!.author
            Glide.with(binding!!.fullImageView)
                    .load(RetrofitBuilder.getImageLink(mSelectedItem!!.id))
                    .into(binding!!.fullImageView)
            ViewUtils.showReveal(binding!!.fullContainer)
            ViewUtils.show(this, binding!!.imageContainer, mAnimationCallback)
        }
    }

    private fun hideImage() {
        ViewUtils.hide(this, binding!!.imageContainer, mAnimationCallback)
        ViewUtils.hideReveal(binding!!.fullContainer)
    }

    private fun downloadImage(width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, 112, Permissions.WRITE_EXTERNAL)
            return
        }
        if (mSelectedItem != null && MemoryUtil.isSdPresent) {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val imageFile = File(folder, mSelectedItem!!.filename!!)
            DownloadAsync(this, mSelectedItem!!.filename, imageFile.toString(), width, height, mSelectedItem!!.id).execute()
        } else {
            Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show()
            return
        }
        hideImage()
    }

    private fun setImageForMonth(month: Int) {
        if (mSelectedItem == null) return
        val monthImage = prefs!!.calendarImages
        monthImage.setPhoto(month, mSelectedItem!!.id)
        prefs!!.calendarImages = monthImage
        hideImage()
    }

    private fun initRecyclerView() {
        imagesList = binding!!.imagesList
        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                when (position % 6) {
                    5 -> return 3
                    3 -> return 2
                    else -> return 1
                }
            }
        }
        imagesList!!.layoutManager = gridLayoutManager
        imagesList!!.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
        imagesList!!.setHasFixedSize(true)
        imagesList!!.itemAnimator = DefaultItemAnimator()
        imagesList!!.addOnScrollListener(mOnScrollListener)
        imagesList!!.visibility = View.GONE
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

    override fun onPause() {
        super.onPause()
        if (mCall != null && !mCall!!.isExecuted) {
            mCall!!.cancel()
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        when (compoundButton.id) {
            R.id.defaultCheck -> if (b) setImageUrl(DEFAULT_PHOTO, -1)
            R.id.noneCheck -> if (b) setImageUrl(NONE_PHOTO, -1)
        }
    }

    private fun setImageUrl(imageUrl: String, id: Int) {
        prefs!!.imagePath = imageUrl
        prefs!!.imageId = id
        if (mAdapter != null) mAdapter!!.deselectLast()
    }

    override fun onBackPressed() {
        if (binding!!.fullContainer.visibility == View.VISIBLE) {
            hideImage()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            REQUEST_DOWNLOAD -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage(mWidth, mHeight)
            }
            REQUEST_REMINDER -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setToReminder()
            }
        }
    }

    companion object {

        val DEFAULT_PHOTO = "https://unsplash.it/1280/768?image=33"
        private val NONE_PHOTO = ""
        private val TAG = "MainImageActivity"
        private val START_SIZE = 50
        private val REQUEST_DOWNLOAD = 112
        private val REQUEST_REMINDER = 113
    }
}
