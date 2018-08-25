package com.elementary.tasks.notes.editor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
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
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.drawing.DrawView
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.IconRadioButton
import com.elementary.tasks.core.views.ThemedImageButton
import com.elementary.tasks.core.views.roboto.RoboEditText
import com.elementary.tasks.notes.create.NoteImage
import com.elementary.tasks.notes.editor.layers.LayersRecyclerAdapter
import com.elementary.tasks.notes.editor.layers.OnStartDragListener
import com.elementary.tasks.notes.editor.layers.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.fragment_draw_image.*
import kotlinx.android.synthetic.main.view_image_prefs.view.*
import kotlinx.android.synthetic.main.view_layers_prefs.view.*
import kotlinx.android.synthetic.main.view_standard_prefs.view.*
import kotlinx.android.synthetic.main.view_text_prefs.view.*
import java.io.File
import java.io.FileNotFoundException
import java.util.*

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

    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mAdapter: LayersRecyclerAdapter? = null

    private var mImageUri: Uri? = null

    @ColorRes
    private var strokeColor: Int = 0
    @ColorRes
    private var fillColor: Int = 0
    private var isFillPicker: Boolean = false
    private var mPrefsControl: ThemedImageButton? = null

    private val mDrawCallback = object : DrawView.DrawCallback {
        override fun onDrawEnd() {
            checkRedo()
            checkUndo()
        }
    }

    private val isPrefsPanelExpanded: Boolean
        get() = prefsView.visibility == View.VISIBLE

    private val layersPanel: View
        get() {
            if (mAdapter != null) {
                draw_view.removeObserver(mAdapter!!)
            }
            val binding = layoutInflater.inflate(R.layout.view_layers_prefs, null)
            binding.layersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val mAdapter = LayersRecyclerAdapter(context!!, draw_view.elements,
                    object : OnStartDragListener {
                        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                            mItemTouchHelper?.startDrag(viewHolder)
                        }
                    }, object : LayersRecyclerAdapter.AdapterCallback {
                        override fun onChanged() {
                            draw_view.invalidate()
                        }

                        override fun onItemSelect(position: Int) {
                            if (mAdapter != null) mAdapter!!.setIndex(position)
                            draw_view.setHistoryPointer(position + 1)
                        }

                        override fun onItemRemoved(position: Int) {
                            draw_view.setHistoryPointer(draw_view.getHistoryPointer() - 1)
                        }

                        override fun onItemAdded() {
                            binding.layersList.scrollToPosition(mAdapter!!.itemCount - 1)
                        }
                    })
            mAdapter.setIndex(draw_view.getHistoryPointer() - 1)
            val callback = SimpleItemTouchHelperCallback(mAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper?.attachToRecyclerView(binding.layersList)
            binding.layersList.adapter = mAdapter
            mPrefsControl = binding.prefsControl
            binding.prefsControl.setOnClickListener { togglePrefsPanel() }
            draw_view.addObserver(mAdapter)
            return binding
        }


    private val imagePanel: View
        get() {
            val binding = layoutInflater.inflate(R.layout.view_image_prefs, null)
            binding.opacitySeekImage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    draw_view.setOpacity(i, DrawView.Mode.IMAGE)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            binding.opacitySeekImage.progress = draw_view.getOpacity()
            binding.scaleSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    draw_view.setScale(progress, DrawView.Mode.IMAGE)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            binding.scaleSeek.progress = draw_view.scale
            mPrefsControl = binding.prefsControlImage
            binding.prefsControlImage.setOnClickListener { togglePrefsPanel() }
            binding.imageButton.setOnClickListener { showImagePickerDialog() }
            return binding
        }

    private val textPanel: View
        get() {
            val binding = layoutInflater.inflate(R.layout.view_text_prefs, null)
            binding.opacitySeekText.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    draw_view.setOpacity(i, DrawView.Mode.TEXT)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            binding.opacitySeekText.progress = draw_view.getOpacity()
            binding.widthSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    draw_view.setFontSize(i.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            binding.widthSeek.progress = draw_view.getFontSize().toInt()
            binding.addButton.setOnClickListener { showTextPickerDialog() }
            mPrefsControl = binding.prefsControlText
            binding.prefsControlText.setOnClickListener { togglePrefsPanel() }
            binding.fontButton.setOnClickListener { showStyleDialog() }
            return binding
        }

    private val penPanel: View
        get() {
            val binding = layoutInflater.inflate(R.layout.view_standard_prefs, null)
            binding.opacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    draw_view.setOpacity(i, DrawView.Mode.DRAW)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            binding.opacitySeek.progress = draw_view.getOpacity()
            binding.widthSeekStandard.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    draw_view.setPaintStrokeWidth(i.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            binding.widthSeekStandard.progress = draw_view.getPaintStrokeWidth().toInt()
            mPrefsControl = binding.prefsControlStandard
            binding.prefsControlStandard.setOnClickListener { togglePrefsPanel() }
            return binding
        }

    override val image: NoteImage?
        get() {
            ImageSingleton.getInstance().setImage(draw_view.getBitmapAsByteArray(Bitmap.CompressFormat.PNG, 100))
            return ImageSingleton.getInstance().item
        }

    override val originalImage: NoteImage?
        get() = ImageSingleton.getInstance().item

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_draw_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUiTheme()
        draw_view.setCallback(mDrawCallback)
        initDrawControl()
        initControls()
        initColorControls()
        loadImage()
        Handler().postDelayed({ this.hideToolPanel() }, 1000)
        Handler().postDelayed({ this.hideColorPanel() }, 1000)
    }

    private fun initColorControls() {
        setColor(R.color.pureBlack)
        setCurrentColor(colorBlackButton)
        isFillPicker = true
        setColor(R.color.whitePrimary)
        setCurrentColor(colorWhiteButton)
        colorGroup.setOnCheckedChangeListener { _, i ->
            hideColorPanel()
            when (i) {
                R.id.colorAmberButton -> {
                    setColor(R.color.amberPrimary)
                    setCurrentColor(colorAmberButton)
                }
                R.id.colorBlackButton -> {
                    setColor(R.color.pureBlack)
                    setCurrentColor(colorBlackButton)
                }
                R.id.colorBlueButton -> {
                    setColor(R.color.bluePrimary)
                    setCurrentColor(colorBlueButton)
                }
                R.id.colorBlueLightButton -> {
                    setColor(R.color.blueLightPrimary)
                    setCurrentColor(colorBlueLightButton)
                }
                R.id.colorCyanButton -> {
                    setColor(R.color.cyanPrimary)
                    setCurrentColor(colorCyanButton)
                }
                R.id.colorDeepOrangeButton -> {
                    setColor(R.color.orangeDeepPrimary)
                    setCurrentColor(colorDeepOrangeButton)
                }
                R.id.colorDeepPurpleButton -> {
                    setColor(R.color.purpleDeepPrimary)
                    setCurrentColor(colorDeepPurpleButton)
                }
                R.id.colorGreenButton -> {
                    setColor(R.color.greenPrimary)
                    setCurrentColor(colorGreenButton)
                }
                R.id.colorGreenLightButton -> {
                    setColor(R.color.greenLightPrimary)
                    setCurrentColor(colorGreenLightButton)
                }
                R.id.colorGreyButton -> {
                    setColor(R.color.material_divider)
                    setCurrentColor(colorGreyButton)
                }
                R.id.colorIndigoButton -> {
                    setColor(R.color.indigoPrimary)
                    setCurrentColor(colorIndigoButton)
                }
                R.id.colorLimeButton -> {
                    setColor(R.color.limePrimary)
                    setCurrentColor(colorLimeButton)
                }
                R.id.colorOrangeButton -> {
                    setColor(R.color.orangePrimary)
                    setCurrentColor(colorOrangeButton)
                }
                R.id.colorPinkButton -> {
                    setColor(R.color.pinkPrimary)
                    setCurrentColor(colorPinkButton)
                }
                R.id.colorPurpleButton -> {
                    setColor(R.color.purplePrimary)
                    setCurrentColor(colorPurpleButton)
                }
                R.id.colorRedButton -> {
                    setColor(R.color.redPrimary)
                    setCurrentColor(colorRedButton)
                }
                R.id.colorTealButton -> {
                    setColor(R.color.tealPrimary)
                    setCurrentColor(colorTealButton)
                }
                R.id.colorWhiteButton -> {
                    setColor(R.color.whitePrimary)
                    setCurrentColor(colorWhiteButton)
                }
                R.id.colorYellowButton -> {
                    setColor(R.color.yellowPrimary)
                    setCurrentColor(colorYellowButton)
                }
            }
        }
    }

    private fun setColor(@ColorRes color: Int) {
        if (color == 0) return
        if (isFillPicker) {
            fillColor = color
            draw_view.paintFillColor = ContextCompat.getColor(context!!, color)
        } else {
            strokeColor = color
            draw_view.paintStrokeColor = ContextCompat.getColor(context!!, color)
        }
    }

    private fun setUiTheme() {
        val bgColor = themeUtil.backgroundStyle
        background.setBackgroundColor(bgColor)
        draw_tools.setBackgroundColor(bgColor)
        colorView.setBackgroundColor(bgColor)
        controlBox.setBackgroundColor(bgColor)
        prefsView.setBackgroundColor(bgColor)
    }

    private fun initControls() {
        undoButton.setOnClickListener { undo() }
        redoButton.setOnClickListener { redo() }
        clearButton.setOnClickListener { draw_view.clear() }
        fillButton.setOnClickListener { setBackground() }
        currentToolButton.setOnClickListener { toggleToolPanel() }
        currentFillColorButton.setOnClickListener {
            isFillPicker = true
            toggleColorPanel()
        }
        currentStrokeColorButton.setOnClickListener {
            isFillPicker = false
            toggleColorPanel()
        }
        showPrefsButton.setOnClickListener { switchPrefsPanel(draw_view.mode) }
        layersButton.setOnClickListener { showLayersPanel() }
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
        if (colorView.visibility == View.VISIBLE) {
            hideColorPanel()
        } else {
            showColorPanel()
        }
    }

    private fun showColorPanel() {
        if (isFillPicker) {
            colorGroup.check(getId(fillColor))
        } else {
            colorGroup.check(getId(strokeColor))
        }
        ViewUtils.slideInDown(context!!, colorView)
    }

    private fun hideColorPanel() {
        ViewUtils.slideOutUp(context!!, colorView)
    }

    private fun toggleToolPanel() {
        if (draw_tools.visibility == View.VISIBLE) {
            hideToolPanel()
        } else {
            showToolPanel()
        }
    }

    private fun showToolPanel() {
        ViewUtils.slideInDown(context!!, draw_tools)
    }

    private fun hideToolPanel() {
        ViewUtils.slideOutUp(context!!, draw_tools)
    }

    private fun setBackground() {
        draw_view.baseColor = draw_view.paintFillColor
    }

    private fun redo() {
        if (draw_view.canRedo()) {
            draw_view.redo()
        }
        checkRedo()
    }

    private fun checkRedo() {
        redoButton.isEnabled = draw_view.canRedo()
    }

    private fun undo() {
        if (draw_view.canUndo()) {
            draw_view.undo()
        }
        checkUndo()
    }

    private fun checkUndo() {
        undoButton.isEnabled = draw_view.canUndo()
    }

    private fun initDrawControl() {
        tools_group.setOnCheckedChangeListener { _, i ->
            hideToolPanel()
            when (i) {
                R.id.penButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.PEN)
                    setCurrentTool(penButton)
                }
                R.id.lineButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.LINE)
                    setCurrentTool(lineButton)
                }
                R.id.cubicBezierButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.QUADRATIC_BEZIER)
                    setCurrentTool(cubicBezierButton)
                }
                R.id.textButton -> {
                    setDrawMode(DrawView.Mode.TEXT, null)
                    setCurrentTool(textButton)
                }
                R.id.rectangleButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.RECTANGLE)
                    setCurrentTool(rectangleButton)
                }
                R.id.ellipseButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.ELLIPSE)
                    setCurrentTool(ellipseButton)
                }
                R.id.circleButton -> {
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.CIRCLE)
                    setCurrentTool(circleButton)
                }
                R.id.imageButton -> {
                    setDrawMode(DrawView.Mode.IMAGE, null)
                    setCurrentTool(imageButton)
                }
            }
        }
        currentFillColorButton.setIcon(colorWhiteButton.getIcon())
        currentStrokeColorButton.setIcon(colorBlackButton.getIcon())
        switchPrefsPanel(draw_view.mode)
    }

    private fun switchPrefsPanel(mode: DrawView.Mode) {
        if (isPrefsPanelExpanded) {
            hidePrefsPanel()
        }
        prefsView.removeAllViewsInLayout()
        when (mode) {
            DrawView.Mode.DRAW -> prefsView.addView(penPanel)
            DrawView.Mode.TEXT -> prefsView.addView(textPanel)
            DrawView.Mode.IMAGE -> prefsView.addView(imagePanel)
            DrawView.Mode.LAYERS -> prefsView.addView(layersPanel)
        }
        showPrefsPanel()
    }

    private fun showImagePickerDialog() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(activity!!, REQUEST_SD_CARD, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL, Permissions.MANAGE_DOCUMENTS)
            return
        }
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.image))
        builder.setItems(arrayOf<CharSequence>(getString(R.string.gallery), getString(R.string.take_a_shot))) { _, which ->
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
        val builder = dialogues.getDialog(context!!)
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
                var cView = convertView
                if (cView == null) {
                    cView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null)
                }
                val textView = cView!!.findViewById<TextView>(android.R.id.text1)
                textView.typeface = getTypeface(position)
                textView.text = contacts[position]
                return cView
            }

            private fun getTypeface(position: Int): Typeface {
                return AssetsUtil.getTypeface(context, position)
            }
        }
        builder.setSingleChoiceItems(adapter, draw_view.getFontFamily()) { dialog, which ->
            draw_view.setFontFamily(which)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showPrefsPanel() {
        ViewUtils.slideInUp(context!!, prefsView)
        if (mPrefsControl == null) {
            return
        }
        if (themeUtil.isDark) {
            mPrefsControl?.setImageResource(R.drawable.ic_expand_more_white_24dp)
        } else {
            mPrefsControl?.setImageResource(R.drawable.ic_expand_more_black_24dp)
        }
    }

    private fun hidePrefsPanel() {
        ViewUtils.slideOutDown(context!!, prefsView)
        if (mPrefsControl == null) {
            return
        }
        if (themeUtil.isDark) {
            mPrefsControl?.setImageResource(R.drawable.ic_expand_less_white_24dp)
        } else {
            mPrefsControl?.setImageResource(R.drawable.ic_expand_less_black_24dp)
        }
    }

    private fun setCurrentColor(button: IconRadioButton) {
        if (isFillPicker) {
            currentFillColorButton.setIcon(button.getIcon())
        } else {
            currentStrokeColorButton.setIcon(button.getIcon())
        }
    }

    private fun setCurrentTool(button: IconRadioButton) {
        currentToolButton.setIcon(button.getIcon())
        currentToolButton.text = button.text
    }

    private fun setDrawMode(mode: DrawView.Mode, drawer: DrawView.Drawer?) {
        draw_view.mode = mode
        if (drawer != null) draw_view.drawer = drawer
        switchPrefsPanel(mode)
    }

    private fun showTextPickerDialog() {
        val builder = dialogues.getDialog(context!!)
        val editText = RoboEditText(context!!)
        editText.setHint(R.string.text)
        builder.setView(editText)
        builder.setPositiveButton(R.string.add_text) { dialogInterface, _ ->
            val text = editText.text.toString().trim { it <= ' ' }
            dialogInterface.dismiss()
            setText(text)
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun setText(text: String) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(context, R.string.text_is_empty, Toast.LENGTH_SHORT).show()
            return
        }
        draw_view.addText(text)
    }

    private fun loadImage() {
        val image = ImageSingleton.getInstance().item?.image
        if (image != null) {
            draw_view.addBitmap(image)
        }
        imageButton.isChecked = true
    }

    private fun getId(@ColorRes color: Int): Int {
        var id = 0
        when (color) {
            R.color.amberPrimary -> id = R.id.colorAmberButton
            R.color.pureBlack -> id = R.id.colorBlackButton
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

    private fun getRealPathFromURI(contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity!!.managedQuery(contentUri, proj, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
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
            draw_view.addBitmap(bitmapImage)
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
        private const val REQUEST_SD_CARD = 1112

        fun newInstance(): DrawFragment {
            return DrawFragment()
        }
    }
}
