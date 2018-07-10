package com.elementary.tasks.core.file_explorer

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.views.roboto.RoboEditText
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.ActivityFileExplorerBinding

import java.io.File
import java.io.FilenameFilter
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
class FileExplorerActivity : ThemedActivity() {

    private val str = ArrayList<String>()
    private var firstLvl: Boolean? = true
    private var isDark = false
    private var mFilter: Boolean = false

    private var mDataList: ArrayList<FileDataItem>? = null
    private var path = File(Environment.getExternalStorageDirectory().toString() + "")
    private var mFileName: String? = null
    private var mFilePath: String? = null

    private var filType: String? = null

    private var mAdapter: FileRecyclerAdapter? = null
    private var mSound: Sound? = null

    private var binding: ActivityFileExplorerBinding? = null
    private var mFilesList: RecyclerView? = null
    private var mPlayerLayout: LinearLayout? = null
    private var mMelodyTitle: RoboTextView? = null
    private var mSearchView: RoboEditText? = null

    private val recyclerClick = RecyclerClickListener { this.selectFile(it) }

    private val mFilterCallback = FilterCallback { mFilesList!!.scrollToPosition(0) }

    private val directoryIcon: Int
        get() = if (isDark) R.drawable.ic_folder_white_24dp else R.drawable.ic_folder_black_24dp

    private val undoIcon: Int
        get() = if (isDark) R.drawable.ic_undo_white_24dp else R.drawable.ic_undo_black_24dp

    private val mListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.fab -> saveChoice()
            R.id.playButton -> play()
            R.id.stopButton -> stop()
            R.id.pauseButton -> pause()
            R.id.clearButton -> mSearchView!!.setText("")
        }
    }

    private fun selectFile(position: Int) {
        val item = mAdapter!!.getItem(position)
        mFileName = item.fileName
        mFilePath = item.filePath
        val sel = File(path.toString() + "/" + mFileName)
        if (sel.isDirectory) {
            firstLvl = false
            str.add(mFileName)
            mDataList = null
            path = File(sel.toString() + "")
            loadFileList()
            loadList()
        } else if (mFileName!!.equals(getString(R.string.up), ignoreCase = true) && !sel.exists()) {
            moveUp()
        } else {
            if (filType!!.matches("any".toRegex())) {
                sendFile()
            } else if (filType == TYPE_PHOTO) {
                if (isImage(mFileName)) {
                    showFullImage()
                } else {
                    Toast.makeText(this, R.string.not_a_image_file, Toast.LENGTH_SHORT).show()
                }
            } else {
                if (isMelody(mFileName)) {
                    play()
                } else {
                    Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFullImage() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(mFileName)
        val imageView = ImageView(this)
        val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, MeasureUtils.dp2px(this, 256))
        imageView.layoutParams = layoutParams
        Glide.with(this)
                .load(File(mFilePath!!))
                .into(imageView)
        builder.setView(imageView)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            dialog.dismiss()
            sendFile()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun moveUp() {
        val s = str.removeAt(str.size - 1)
        path = File(path.toString().substring(0,
                path.toString().lastIndexOf(s)))
        mDataList = null
        if (str.isEmpty()) {
            firstLvl = true
        }
        loadFileList()
        loadList()
    }

    private fun sendFile() {
        val intent = Intent()
        intent.putExtra(Constants.FILE_PICKED, mFilePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSound = Sound(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_explorer)
        filType = intent.getStringExtra(Constants.FILE_TYPE)
        if (filType == null) filType = TYPE_MUSIC
        isDark = themeUtil!!.isDark
        initActionBar()
        initRecyclerView()
        initPlayer()
        initSearch()
        initButtons()
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
            loadFileList()
            loadList()
        } else {
            Permissions.requestPermission(this, SD_CARD, Permissions.READ_EXTERNAL)
        }
    }

    private fun initPlayer() {
        mPlayerLayout = binding!!.playerLayout
        mPlayerLayout!!.visibility = View.GONE
        mMelodyTitle = binding!!.currentMelody
    }

    private fun initRecyclerView() {
        mFilesList = binding!!.mDataList
        mFilesList!!.setHasFixedSize(true)
        mFilesList!!.layoutManager = LinearLayoutManager(this)
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun initSearch() {
        mSearchView = binding!!.searchField
        mSearchView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mFilter && mAdapter != null) mAdapter!!.filter(s.toString(), mDataList)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun initButtons() {
        val clearButton = binding!!.clearButton
        binding!!.fab.setOnClickListener(mListener)
        binding!!.pauseButton.setOnClickListener(mListener)
        binding!!.stopButton.setOnClickListener(mListener)
        binding!!.playButton.setOnClickListener(mListener)
        clearButton.setOnClickListener(mListener)
    }

    private fun loadList() {
        if (mDataList == null) {
            Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show()
            finish()
        }
        mFilesList!!.adapter = mAdapter
    }

    private fun play() {
        if (!mSound!!.isPlaying) {
            if (mPlayerLayout!!.visibility == View.GONE) {
                ViewUtils.expand(mPlayerLayout!!)
            }
            if (mSound!!.isPaused && mSound!!.isSameFile(mFilePath)) {
                mSound!!.resume()
            } else {
                mSound!!.play(mFilePath)
                mMelodyTitle!!.text = mFileName
            }
        } else {
            if (mSound!!.isSameFile(mFilePath)) {
                return
            }
            mSound!!.play(mFilePath)
            mMelodyTitle!!.text = mFileName
        }
    }

    private fun pause() {
        if (mSound!!.isPlaying) {
            mSound!!.pause()
        }
    }

    private fun stop() {
        if (mSound!!.isPlaying) {
            mSound!!.stop(true)
        }
        ViewUtils.collapse(mPlayerLayout!!)
    }

    private fun loadFileList() {
        try {
            path.mkdirs()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        mFilter = false
        mSearchView!!.setText("")
        mFilter = true
        if (path.exists()) {
            createFilteredFileList()
        }
        mAdapter = FileRecyclerAdapter(this, mDataList, recyclerClick, mFilterCallback)
    }

    private fun createFilteredFileList() {
        val filter = { dir, filename ->
            val sel = File(dir, filename)
            (sel.isFile() || sel.isDirectory()) && !sel.isHidden()
        }

        var list: List<String>
        try {
            list = Arrays.asList(*path.list(filter))
        } catch (e: NullPointerException) {
            list = ArrayList()
        }

        Collections.sort(list)
        mDataList = ArrayList(list.size)
        for (i in list.indices) {
            val fileName = list[i]
            val sel = File(path, fileName)
            mDataList!!.add(i, FileDataItem(fileName, 0, sel.toString()))

            if (sel.isDirectory) {
                mDataList!![i].icon = directoryIcon
            }
        }

        if ((!firstLvl)!!) {
            addUpItem()
        }
    }

    private fun addUpItem() {
        val temp = ArrayList<FileDataItem>(mDataList!!.size + 1)
        temp.add(0, FileDataItem(getString(R.string.up), undoIcon, ""))
        temp.addAll(mDataList)
        mDataList = temp
    }

    private fun isMelody(file: String?): Boolean {
        return file != null && (file.endsWith(".mp3") || file.endsWith(".ogg")
                || file.endsWith(".m4a") || file.endsWith(".flac"))
    }

    private fun isImage(file: String?): Boolean {
        return file != null && (file.endsWith(".jpg") || file.endsWith(".jpeg")
                || file.endsWith(".png") || file.endsWith(".tiff"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                exit()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if ((!firstLvl)!!) {
            moveUp()
        } else {
            exit()
        }
    }

    private fun exit() {
        if (isMelody(mFileName)) {
            stop()
        }
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun saveChoice() {
        if (isMelody(mFileName)) {
            stop()
            sendFile()
        } else {
            Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            SD_CARD -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFileList()
                loadList()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    companion object {

        private val SD_CARD = 444
        val TYPE_MUSIC = "music"
        val TYPE_PHOTO = "photo"
    }
}
