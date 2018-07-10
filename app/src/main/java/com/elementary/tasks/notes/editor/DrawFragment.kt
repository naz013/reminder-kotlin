package com.elementary.tasks.notes.editor

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.drawing.DrawView
import com.elementary.tasks.core.utils.AssetsUtil
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.views.IconRadioButton
import com.elementary.tasks.core.views.ThemedImageButton
import com.elementary.tasks.core.views.roboto.RoboEditText
import com.elementary.tasks.databinding.FragmentDrawImageBinding
import com.elementary.tasks.databinding.ViewImagePrefsBinding
import com.elementary.tasks.databinding.ViewLayersPrefsBinding
import com.elementary.tasks.databinding.ViewStandardPrefsBinding
import com.elementary.tasks.databinding.ViewTextPrefsBinding
import com.elementary.tasks.notes.create.NoteImage
import com.elementary.tasks.notes.editor.layers.LayersRecyclerAdapter
import com.elementary.tasks.notes.editor.layers.SimpleItemTouchHelperCallback

import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayList

import androidx.annotation.ColorRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager

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
class DrawFragment : BitmapFragment() {

    private var binding: FragmentDrawImageBinding? = null
    private var mView: DrawView? = null
    private var mControlButton: ThemedImageButton? = null

    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mAdapter: LayersRecyclerAdapter? = null

    private var mImageUri: Uri? = null

    @ColorRes
    private var strokeColor: Int = 0
    @ColorRes
    private var fillColor: Int = 0
    private var isFillPicker: Boolean = false

    private val mDrawCallback = {
        checkRedo()
        checkUndo()
    }

    private val isPrefsPanelExpanded: Boolean
        get() = binding!!.prefsView.visibility == View.VISIBLE

    private val layersPanel: ViewLayersPrefsBinding
        get() {
            if (mAdapter != null) {
                mView!!.removeObserver(mAdapter)
            }
            val binding = ViewLayersPrefsBinding.inflate(LayoutInflater.from(context))
            binding.layersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            mAdapter = LayersRecyclerAdapter(context, mView!!.elements,
                    { viewHolder -> mItemTouchHelper!!.startDrag(viewHolder) },
                    object : LayersRecyclerAdapter.AdapterCallback {
                        override fun onChanged() {
                            mView!!.invalidate()
                        }

                        override fun onItemSelect(position: Int) {
                            mAdapter!!.index = position
                            mView!!.historyPointer = position + 1
                        }

                        override fun onItemRemoved(position: Int) {
                            mView!!.historyPointer = mView!!.historyPointer - 1
                        }

                        override fun onItemAdded() {
                            binding.layersList.scrollToPosition(mAdapter!!.itemCount - 1)
                        }
                    })
            mAdapter!!.index = mView!!.historyPointer - 1
            val callback = SimpleItemTouchHelperCallback(mAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper!!.attachToRecyclerView(binding.layersList)
            binding.layersList.adapter = mAdapter
            mControlButton = binding.prefsControl
            binding.prefsControl.setOnClickListener { v -> togglePrefsPanel() }
            mView!!.addObserver(mAdapter)
            return binding
        }

    private val imagePanel: ViewImagePrefsBinding
        get() {
            val binding = ViewImagePrefsBinding.inflate(LayoutInflater.from(context))
            binding.opacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mView!!.setOpacity(i, DrawView.Mode.IMAGE)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            binding.opacitySeek.progress = mView!!.opacity
            binding.scaleSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mView!!.setScale(progress, DrawView.Mode.IMAGE)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            binding.scaleSeek.progress = mView!!.scale
            mControlButton = binding.prefsControl
            binding.prefsControl.setOnClickListener { v -> togglePrefsPanel() }
            binding.imageButton.setOnClickListener { v -> showImagePickerDialog() }
            return binding
        }

    private val textPanel: ViewTextPrefsBinding
        get() {
            val binding = ViewTextPrefsBinding.inflate(LayoutInflater.from(context))
            binding.opacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mView!!.setOpacity(i, DrawView.Mode.TEXT)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            binding.opacitySeek.progress = mView!!.opacity
            binding.widthSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mView!!.fontSize = i.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            binding.widthSeek.progress = mView!!.fontSize.toInt()
            binding.addButton.setOnClickListener { view -> showTextPickerDialog() }
            mControlButton = binding.prefsControl
            binding.prefsControl.setOnClickListener { v -> togglePrefsPanel() }
            binding.fontButton.setOnClickListener { v -> showStyleDialog() }
            return binding
        }

    private val penPanel: ViewStandardPrefsBinding
        get() {
            val binding = ViewStandardPrefsBinding.inflate(LayoutInflater.from(context))
            binding.opacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mView!!.setOpacity(i, DrawView.Mode.DRAW)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            binding.opacitySeek.progress = mView!!.opacity
            binding.widthSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mView!!.paintStrokeWidth = i.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            binding.widthSeek.progress = mView!!.paintStrokeWidth.toInt()
            mControlButton = binding.prefsControl
            binding.prefsControl.setOnClickListener { v -> togglePrefsPanel() }
            return binding
        }

    override val image: NoteImage?
        get() {
            ImageSingleton.getInstance().setImage(mView!!.getBitmapAsByteArray(Bitmap.CompressFormat.PNG, 100))
            return ImageSingleton.getInstance().item
        }

    override val originalImage: NoteImage?
        get() = ImageSingleton.getInstance().item

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDrawImageBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUiTheme()
        mView = binding!!.drawView
        mView!!.setCallback(mDrawCallback)
        initDrawControl()
        initControls()
        initColorControls()
        loadImage()
        Handler().postDelayed({ this.hideToolPanel() }, 1000)
        Handler().postDelayed({ this.hideColorPanel() }, 1000)
    }

    private fun initColorControls() {
        setColor(R.color.blackPrimary)
        setCurrentColor(binding!!.colorBlackButton)
        isFillPicker = true
        setColor(R.color.whitePrimary)
        setCurrentColor(binding!!.colorWhiteButton)
        binding!!.colorGroup.setOnCheckedChangeListener { radioGroup, i ->
            hideColorPanel()
            when (i) {
                R.id.colorAmberButton -> {
                    setColor(R.color.amberPrimary)
                    setCurrentColor(binding!!.colorAmberButton)
                }
                R.id.colorBlackButton -> {
                    setColor(R.color.blackPrimary)
                    setCurrentColor(binding!!.colorBlackButton)
                }
                R.id.colorBlueButton -> {
                    setColor(R.color.bluePrimary)
                    setCurrentColor(binding!!.colorBlueButton)
                }
                R.id.colorBlueLightButton -> {
                    setColor(R.color.blueLightPrimary)
                    setCurrentColor(binding!!.colorBlueLightButton)
                }
                R.id.colorCyanButton -> {
                    setColor(R.color.cyanPrimary)
                    setCurrentColor(binding!!.colorCyanButton)
                }
                R.id.colorDeepOrangeButton -> {
                    setColor(R.color.orangeDeepPrimary)
                    setCurrentColor(binding!!.colorDeepOrangeButton)
                }
                R.id.colorDeepPurpleButton -> {
                    setColor(R.color.purpleDeepPrimary)
                    setCurrentColor(binding!!.colorDeepPurpleButton)
                }
                R.id.colorGreenButton -> {
                    setColor(R.color.greenPrimary)
                    setCurrentColor(binding!!.colorGreenButton)
                }
                R.id.colorGreenLightButton -> {
                    setColor(R.color.greenLightPrimary)
                    setCurrentColor(binding!!.colorGreenLightButton)
                }
                R.id.colorGreyButton -> {
                    setColor(R.color.material_divider)
                    setCurrentColor(binding!!.colorGreyButton)
                }
                R.id.colorIndigoButton -> {
                    setColor(R.color.indigoPrimary)
                    setCurrentColor(binding!!.colorIndigoButton)
                }
                R.id.colorLimeButton -> {
                    setColor(R.color.limePrimary)
                    setCurrentColor(binding!!.colorLimeButton)
                }
                R.id.colorOrangeButton -> {
                    setColor(R.color.orangePrimary)
                    setCurrentColor(binding!!.colorOrangeButton)
                }
                R.id.colorPinkButton -> {
                    setColor(R.color.pinkPrimary)
                    setCurrentColor(binding!!.colorPinkButton)
                }
                R.id.colorPurpleButton -> {
                    setColor(R.color.purplePrimary)
                    setCurrentColor(binding!!.colorPurpleButton)
                }
                R.id.colorRedButton -> {
                    setColor(R.color.redPrimary)
                    setCurrentColor(binding!!.colorRedButton)
                }
                R.id.colorTealButton -> {
                    setColor(R.color.tealPrimary)
                    setCurrentColor(binding!!.colorTealButton)
                }
                R.id.colorWhiteButton -> {
                    setColor(R.color.whitePrimary)
                    setCurrentColor(binding!!.colorWhiteButton)
                }
                R.id.colorYellowButton -> {
                    setColor(R.color.yellowPrimary)
                    setCurrentColor(binding!!.colorYellowButton)
                }
            }
        }
    }

    private fun setColor(@ColorRes color: Int) {
        if (color == 0) return
        if (isFillPicker) {
            fillColor = color
            mView!!.paintFillColor = resources.getColor(color)
        } else {
            strokeColor = color
            mView!!.paintStrokeColor = resources.getColor(color)
        }
    }

    private fun setUiTheme() {
        val bgColor = ThemeUtil.getInstance(context).backgroundStyle
        binding!!.background.setBackgroundColor(bgColor)
        binding!!.drawTools.setBackgroundColor(bgColor)
        binding!!.colorView.setBackgroundColor(bgColor)
        binding!!.controlBox.setBackgroundColor(bgColor)
        binding!!.prefsView.setBackgroundColor(bgColor)
    }

    private fun initControls() {
        binding!!.undoButton.setOnClickListener { view -> undo() }
        binding!!.redoButton.setOnClickListener { view -> redo() }
        binding!!.clearButton.setOnClickListener { view -> mView!!.clear() }
        binding!!.fillButton.setOnClickListener { view -> setBackground() }
        binding!!.currentToolButton.setOnClickListener { view -> toggleToolPanel() }
        binding!!.currentFillColorButton.setOnClickListener { view ->
            isFillPicker = true
            toggleColorPanel()
        }
        binding!!.currentStrokeColorButton.setOnClickListener { view ->
            isFillPicker = false
            toggleColorPanel()
        }
        binding!!.showPrefsButton.setOnClickListener { v -> switchPrefsPanel(mView!!.mode) }
        binding!!.layersButton.setOnClickListener { v -> showLayersPanel() }
    }

    private fun showLayersPanel() {
        if (isPrefsPanelExpanded) {
            hidePrefsPanel()
        } else {
            switchPrefsPanel(DrawView.Mode.LAYERS)
        }
    }

    private fun togglePrefsPanel() {
        if (isPrefsPanelExpanded) {
            hidePrefsPanel()
        } else {
            showPrefsPanel()
        }
    }

    private fun toggleColorPanel() {
        if (binding!!.colorView.visibility == View.VISIBLE) {
            hideColorPanel()
        } else {
            showColorPanel()
        }
    }

    private fun showColorPanel() {
        if (isFillPicker) {
            binding!!.colorGroup.check(getId(fillColor))
        } else {
            binding!!.colorGroup.check(getId(strokeColor))
        }
        ViewUtils.slideInDown(context, binding!!.colorView)
    }

    private fun hideColorPanel() {
        ViewUtils.slideOutUp(context, binding!!.colorView)
    }

    private fun toggleToolPanel() {
        if (binding!!.drawTools.visibility == View.VISIBLE) {
            hideToolPanel()
        } else {
            showToolPanel()
        }
    }

    private fun showToolPanel() {
        ViewUtils.slideInDown(context, binding!!.drawTools)
    }

    private fun hideToolPanel() {
        ViewUtils.slideOutUp(context, binding!!.drawTools)
    }

    private fun setBackground() {
        mView!!.baseColor = mView!!.paintFillColor
    }

    private fun redo() {
        if (mView!!.canRedo()) {
            mView!!.redo()
        }
        checkRedo()
    }

    private fun checkRedo() {
        if (mView!!.canRedo()) {
            binding!!.redoButton.isEnabled = true
        } else {
            binding!!.redoButton.isEnabled = false
        }
    }

    private fun undo() {
        if (mView!!.canUndo()) {
            mView!!.undo()
        }
        checkUndo()
    }

    private fun checkUndo() {
        if (mView!!.canUndo()) {
            binding!!.undoButton.isEnabled = true
        } else {
            binding!!.undoButton.isEnabled = false
        }
    }

    private fun initDrawControl() {
        binding!!.toolsGroup.setOnCheckedChangeListener { radioGroup, i ->
            hideToolPanel()
            when (i) {
                R.id.penButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.PEN)
                    setCurrentTool(binding!!.penButton)
                }
                R.id.lineButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.LINE)
                    setCurrentTool(binding!!.lineButton)
                }
                R.id.cubicBezierButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.QUADRATIC_BEZIER)
                    setCurrentTool(binding!!.cubicBezierButton)
                }
                R.id.textButton -> {
                    setDrawMode(DrawView.Mode.TEXT, null)
                    setCurrentTool(binding!!.textButton)
                }
                R.id.rectangleButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.RECTANGLE)
                    setCurrentTool(binding!!.rectangleButton)
                }
                R.id.ellipseButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.ELLIPSE)
                    setCurrentTool(binding!!.ellipseButton)
                }
                R.id.circleButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.CIRCLE)
                    setCurrentTool(binding!!.circleButton)
                }
                R.id.imageButton -> {
                    setDrawMode(DrawView.Mode.IMAGE, null)
                    setCurrentTool(binding!!.imageButton)
                }
            }
        }
        binding!!.currentFillColorButton.icon = binding!!.colorWhiteButton.icon
        binding!!.currentStrokeColorButton.icon = binding!!.colorBlackButton.icon
        switchPrefsPanel(mView!!.mode)
    }

    private fun switchPrefsPanel(mode: DrawView.Mode) {
        if (isPrefsPanelExpanded) {
            hidePrefsPanel()
        }
        binding!!.prefsView.removeAllViewsInLayout()
        if (mode == DrawView.Mode.DRAW) {
            binding!!.prefsView.addView(penPanel.root)
        } else if (mode == DrawView.Mode.TEXT) {
            binding!!.prefsView.addView(textPanel.root)
        } else if (mode == DrawView.Mode.IMAGE) {
            binding!!.prefsView.addView(imagePanel.root)
        } else if (mode == DrawView.Mode.LAYERS) {
            binding!!.prefsView.addView(layersPanel.root)
        }
        showPrefsPanel()
    }

    private fun showImagePickerDialog() {
        if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(activity, REQUEST_SD_CARD, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.MANAGE_DOCUMENTS)
            return
        }
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.image))
        builder.setItems(arrayOf<CharSequence>(getString(R.string.gallery), getString(R.string.take_a_shot))
        ) { dialog, which ->
            when (which) {
                0 -> pickFromGallery()
                1 -> pickFromCamera()
            }
        }

        builder.create().show()
    }

    private fun pickFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        mImageUri = context!!.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
        startActivityForResult(intent, Constants.ACTION_REQUEST_CAMERA)
    }

    private fun pickFromGallery() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, Constants.ACTION_REQUEST_GALLERY)
    }

    private fun showStyleDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.font_style))
        val contacts = ArrayList<String>()
        contacts.clear()
        contacts.add("Black")
        contacts.add("Black Italic")
        contacts.add("Bold")
        contacts.add("Bold Italic")
        contacts.add("Italic")
        contacts.add("Light")
        contacts.add("Light Italic")
        contacts.add("Medium")
        contacts.add("Medium Italic")
        contacts.add("Regular")
        contacts.add("Thin")
        contacts.add("Thin Italic")
        val inflater = LayoutInflater.from(context)
        val adapter = object : ArrayAdapter<String>(context!!,
                android.R.layout.simple_list_item_single_choice, contacts) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var convertView = convertView
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null)
                }
                val textView = convertView!!.findViewById<TextView>(android.R.id.text1)
                textView.typeface = getTypeface(position)
                textView.text = contacts[position]
                return convertView
            }

            private fun getTypeface(position: Int): Typeface {
                return AssetsUtil.getTypeface(context, position)
            }
        }
        builder.setSingleChoiceItems(adapter, mView!!.fontFamily) { dialog, which ->
            mView!!.fontFamily = which
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showPrefsPanel() {
        ViewUtils.slideInUp(context, binding!!.prefsView)
        if (mControlButton == null) {
            return
        }
        if (ThemeUtil.getInstance(context).isDark) {
            mControlButton!!.setImageResource(R.drawable.ic_expand_more_white_24dp)
        } else {
            mControlButton!!.setImageResource(R.drawable.ic_expand_more_black_24dp)
        }
    }

    private fun hidePrefsPanel() {
        ViewUtils.slideOutDown(context, binding!!.prefsView)
        if (mControlButton == null) {
            return
        }
        if (ThemeUtil.getInstance(context).isDark) {
            mControlButton!!.setImageResource(R.drawable.ic_expand_less_white_24dp)
        } else {
            mControlButton!!.setImageResource(R.drawable.ic_expand_less_black_24dp)
        }
    }

    private fun setCurrentColor(button: IconRadioButton) {
        if (isFillPicker) {
            binding!!.currentFillColorButton.icon = button.icon
        } else {
            binding!!.currentStrokeColorButton.icon = button.icon
        }
    }

    private fun setCurrentTool(button: IconRadioButton) {
        binding!!.currentToolButton.icon = button.icon
        binding!!.currentToolButton.text = button.text
    }

    private fun setDrawMode(mode: DrawView.Mode, drawer: DrawView.Drawer?) {
        mView!!.mode = mode
        if (drawer != null) mView!!.drawer = drawer
        switchPrefsPanel(mode)
    }

    private fun showTextPickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        val editText = RoboEditText(context)
        editText.setHint(R.string.text)
        builder.setView(editText)
        builder.setPositiveButton(R.string.add_text) { dialogInterface, i ->
            val text = editText.text!!.toString().trim { it <= ' ' }
            dialogInterface.dismiss()
            setText(text)
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun setText(text: String) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(context, R.string.text_is_empty, Toast.LENGTH_SHORT).show()
            return
        }
        mView!!.addText(text)
    }

    private fun loadImage() {
        val image = ImageSingleton.getInstance().item
        if (image != null) {
            mView!!.addBitmap(image.image)
        }
        binding!!.imageButton.isChecked = true
    }

    private fun getId(@ColorRes color: Int): Int {
        var id = 0
        when (color) {
            R.color.amberPrimary -> id = R.id.colorAmberButton
            R.color.blackPrimary -> id = R.id.colorBlackButton
            R.color.bluePrimary -> id = R.id.colorBlueButton
            R.color.blueLightPrimary -> id = R.id.colorBlueLightButton
            R.color.cyanPrimary -> id = R.id.colorCyanButton
            R.color.orangeDeepPrimary -> id = R.id.colorDeepOrangeButton
            R.color.purpleDeepPrimary -> id = R.id.colorDeepPurpleButton
            R.color.greenPrimary -> id = R.id.colorGreenButton
            R.color.greenLightPrimary -> id = R.id.colorGreenLightButton
            R.color.material_divider -> id = R.id.colorGreyButton
            R.color.indigoPrimary -> id = R.id.colorIndigoButton
            R.color.limePrimary -> id = R.id.colorLimeButton
            R.color.orangePrimary -> id = R.id.colorOrangeButton
            R.color.pinkPrimary -> id = R.id.colorPinkButton
            R.color.purplePrimary -> id = R.id.colorPurpleButton
            R.color.redPrimary -> id = R.id.colorRedButton
            R.color.yellowPrimary -> id = R.id.colorYellowButton
            R.color.whitePrimary -> id = R.id.colorWhiteButton
            R.color.tealPrimary -> id = R.id.colorTealButton
        }
        return id
    }

    fun getRealPathFromURI(contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity!!.managedQuery(contentUri, proj, null, null, null)
        val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.ACTION_REQUEST_GALLERY -> addImageFromUri(data!!.data)
                Constants.ACTION_REQUEST_CAMERA -> getImageFromCamera()
            }
        }
    }

    private fun addImageFromUri(uri: Uri?) {
        if (uri == null) return
        var bitmapImage: Bitmap? = null
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(context!!, uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (bitmapImage != null) {
            mView!!.addBitmap(bitmapImage)
            switchPrefsPanel(DrawView.Mode.IMAGE)
        }
    }

    private fun getImageFromCamera() {
        addImageFromUri(mImageUri)
        val pathFromURI = getRealPathFromURI(mImageUri)
        val file = File(pathFromURI)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun onBackPressed(): Boolean {
        if (isPrefsPanelExpanded) {
            togglePrefsPanel()
            return true
        }
        return false
    }

    companion object {

        private val TAG = "DrawFragment"
        private val REQUEST_SD_CARD = 1112

        fun newInstance(): DrawFragment {
            return DrawFragment()
        }
    }
}
