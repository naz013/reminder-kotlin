package com.elementary.tasks.notes.editor

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.drawing.DrawView
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.*
import com.elementary.tasks.notes.editor.layers.LayersRecyclerAdapter
import com.elementary.tasks.notes.editor.layers.OnStartDragListener
import com.elementary.tasks.notes.editor.layers.SimpleItemTouchHelperCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.FileNotFoundException

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
class DrawFragment : BitmapFragment<FragmentDrawImageBinding>(), PhotoSelectionUtil.UriCallback {

    private var mItemTouchHelper: ItemTouchHelper? = null
    private val mAdapter: LayersRecyclerAdapter = LayersRecyclerAdapter()

    private lateinit var photoSelectionUtil: PhotoSelectionUtil
    private var mDialog: BottomSheetDialog? = null

    @ColorRes
    private var strokeColor: Int = 0
    private var colors = intArrayOf()

    private val mDrawCallback = object : DrawView.DrawCallback {
        override fun onDrawEnd() {
            checkRedo()
            checkUndo()
        }
    }

    private val layersPanel: View
        get() {
            binding.drawView.removeObserver(mAdapter)

            val b = ViewLayersPrefsBinding.inflate(layoutInflater)
            b.layersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            mAdapter.onStartDragListener =  object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                    mItemTouchHelper?.startDrag(viewHolder)
                }
            }
            mAdapter.mCallback = object : LayersRecyclerAdapter.AdapterCallback {
                override fun onChanged() {
                    binding.drawView.invalidate()
                }

                override fun onItemSelect(position: Int) {
                    mAdapter.setIndex(position)
                    binding.drawView.setHistoryPointer(position + 1)
                }

                override fun onItemRemoved(position: Int) {
                    binding.drawView.setHistoryPointer(binding.drawView.getHistoryPointer() - 1)
                }

                override fun onItemAdded() {
                    b.layersList.scrollToPosition(mAdapter.itemCount - 1)
                }
            }

            mAdapter.setData(binding.drawView.elements)
            mAdapter.setIndex(binding.drawView.getHistoryPointer() - 1)
            val callback = SimpleItemTouchHelperCallback(mAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper?.attachToRecyclerView(b.layersList)
            b.layersList.adapter = mAdapter
            binding.drawView.addObserver(mAdapter)
            return b.root
        }

    override fun getTitle(): String = ""

    private val imagePanel: View
        get() {
            val b = ViewImagePrefsBinding.inflate(layoutInflater)
            b.opacitySeekImage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.drawView.setOpacity(i, DrawView.Mode.IMAGE)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            b.opacitySeekImage.progress = binding.drawView.getOpacity()
            b.scaleSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    binding.drawView.setScale(progress, DrawView.Mode.IMAGE)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            b.scaleSeek.progress = binding.drawView.scale
            b.imageButton.setOnClickListener { photoSelectionUtil.selectImage() }
            return b.root
        }

    private val textPanel: View
        get() {
            val b = ViewTextPrefsBinding.inflate(layoutInflater)
            b.opacitySeekText.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.drawView.setOpacity(i, DrawView.Mode.TEXT)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            b.opacitySeekText.progress = binding.drawView.getOpacity()
            b.widthSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.drawView.setFontSize(i.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            b.widthSeek.progress = binding.drawView.getFontSize().toInt()
            b.addButton.setOnClickListener { showTextPickerDialog() }
            b.fontButton.setOnClickListener { showStyleDialog() }
            return b.root
        }

    private val penPanel: View
        get() {
            val b = ViewStandardPrefsBinding.inflate(layoutInflater)
            b.opacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.drawView.setOpacity(i, DrawView.Mode.DRAW)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            b.opacitySeek.progress = binding.drawView.getOpacity()
            b.widthSeekStandard.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.drawView.setPaintStrokeWidth(i.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            b.widthSeekStandard.progress = binding.drawView.getPaintStrokeWidth().toInt()
            return b.root
        }

    private val toolsPanel: View
        get() {
            val b = ViewDrawToolsBinding.inflate(layoutInflater)
            b.penButton.setOnClickListener { toolClick(it) }
            b.lineButton.setOnClickListener { toolClick(it) }
            b.cubicBezierButton.setOnClickListener { toolClick(it) }
            b.textButton.setOnClickListener { toolClick(it) }
            b.rectangleButton.setOnClickListener { toolClick(it) }
            b.ellipseButton.setOnClickListener { toolClick(it) }
            b.circleButton.setOnClickListener { toolClick(it) }
            b.imageToolButton.setOnClickListener { toolClick(it) }
            b.fillToolButton.setOnClickListener { toolClick(it) }
            return binding.root
        }

    override val image: ByteArray?
        get() {
            val image = binding.drawView.getBitmapAsByteArray(Bitmap.CompressFormat.PNG, 100)
            editInterface?.saveCurrent(image)
            return image
        }

    override val originalImage: ByteArray?
        get() = editInterface?.getOriginal()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colors = drawingColorsForSlider()
        photoSelectionUtil = PhotoSelectionUtil(activity!!, dialogues, false, this)
    }

    override fun layoutRes(): Int = R.layout.fragment_draw_image

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.drawView.setCallback(mDrawCallback)
        initDrawControl()
        initControls()
        initColorControls()
        loadImage()
    }

    private fun showPanel(view: View) {
        val dialog = BottomSheetDialog(activity!!)
        dialog.setContentView(view)
        dialog.show()
        mDialog = dialog
    }

    private fun initColorControls() {
        setColor(drawingColorsForSlider()[0])
    }

    private fun setColor(@ColorInt color: Int) {
        if (color == 0) return
        binding.drawView.paintStrokeColor = color
        binding.drawView.paintFillColor = color
        setCurrentColor(color)
    }

    private fun initControls() {
        binding.undoButton.setOnClickListener { undo() }
        binding.redoButton.setOnClickListener { redo() }
        binding.clearButton.setOnClickListener { binding.drawView.clear() }
        binding.currentToolButton.setOnClickListener { showToolPanel() }
        binding.currentToolButton.setOnLongClickListener {
            switchPrefsPanel(binding.drawView.mode)
            return@setOnLongClickListener true
        }
        binding.currentStrokeColorButton.setOnClickListener { showColorPanel() }
        checkRedo()
        checkUndo()
        binding.layersButton.setOnClickListener { switchPrefsPanel(DrawView.Mode.LAYERS) }
    }

    private fun showColorPanel() {
        dialogues.showColorBottomDialog(activity!!, strokeColor, drawingColorsForSlider()) {
            strokeColor = it
            setColor(drawingColorsForSlider()[it])
        }
    }

    private fun showToolPanel() {
        showPanel(toolsPanel)
    }

    private fun hideToolPanel() {
        mDialog?.dismiss()
    }

    private fun redo() {
        if (binding.drawView.canRedo()) {
            binding.drawView.redo()
        }
        checkRedo()
    }

    private fun checkRedo() {
        binding.redoButton.isEnabled = binding.drawView.canRedo()
    }

    private fun undo() {
        if (binding.drawView.canUndo()) {
            binding.drawView.undo()
        }
        checkUndo()
    }

    private fun checkUndo() {
        binding.undoButton.isEnabled = binding.drawView.canUndo()
    }

    private fun initDrawControl() {
        switchPrefsPanel(binding.drawView.mode)
    }

    private fun toolClick(view: View) {
        hideToolPanel()
        when (view.id) {
            R.id.penButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.PEN)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.fillToolButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.FILL)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.lineButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.LINE)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.cubicBezierButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.QUADRATIC_BEZIER)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.textButton -> {
                setDrawMode(DrawView.Mode.TEXT, null)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.rectangleButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.RECTANGLE)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.ellipseButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.ELLIPSE)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.circleButton -> {
                setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.CIRCLE)
                setCurrentTool(view as AppCompatImageView)
            }
            R.id.imageToolButton -> {
                setDrawMode(DrawView.Mode.IMAGE, null)
                setCurrentTool(view as AppCompatImageView)
            }
        }
    }

    private fun switchPrefsPanel(mode: DrawView.Mode) {
        mDialog?.dismiss()
        when (mode) {
            DrawView.Mode.DRAW -> showPanel(penPanel)
            DrawView.Mode.TEXT -> showPanel(textPanel)
            DrawView.Mode.IMAGE -> showPanel(imagePanel)
            DrawView.Mode.LAYERS -> showPanel(layersPanel)
            else -> {
            }
        }
    }

    private fun showStyleDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.font_style))
        val contacts = AssetsUtil.getFontNames()
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

            private fun getTypeface(position: Int): Typeface? {
                return AssetsUtil.getTypeface(context, position)
            }
        }
        builder.setSingleChoiceItems(adapter, binding.drawView.getFontFamily()) { dialog, which ->
            binding.drawView.setFontFamily(which)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setCurrentColor(@ColorInt color: Int) {
        binding.currentStrokeColorButton.setColorFilter(color)
    }

    private fun setCurrentTool(button: AppCompatImageView) {
        binding.currentToolButton.setImageDrawable(button.drawable)
        binding.currentToolButton.contentDescription = button.contentDescription
    }

    private fun setDrawMode(mode: DrawView.Mode, drawer: DrawView.Drawer?) {
        binding.drawView.mode = mode
        if (drawer != null) binding.drawView.drawer = drawer
        switchPrefsPanel(mode)
    }

    private fun showTextPickerDialog() {
        val builder = dialogues.getDialog(context!!)
        val editText = AppCompatEditText(context!!)
        editText.setHint(R.string.text)
        builder.setView(editText)
        builder.setPositiveButton(R.string.add_text) { dialogInterface, _ ->
            val text = editText.text.toString().trim()
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
        binding.drawView.addText(text)
    }

    private fun loadImage() {
        val image = editInterface?.getCurrent()
        if (image != null) {
            binding.drawView.addBitmap(image)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionUtil.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        photoSelectionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addImageFromUri(uri: Uri?) {
        if (uri == null) return
        launchDefault {
            var bitmapImage: Bitmap? = null
            try {
                bitmapImage = BitmapUtils.decodeUriToBitmap(context!!, uri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (bitmapImage != null) {
                withUIContext {
                    onBitmapReady(bitmapImage)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    @ColorInt
    fun drawingColorsForSlider(): IntArray {
        return intArrayOf(
                ContextCompat.getColor(context!!, R.color.redAccent),
                ContextCompat.getColor(context!!, R.color.pinkAccent),
                ContextCompat.getColor(context!!, R.color.purpleAccent),
                ContextCompat.getColor(context!!, R.color.purpleDeepAccent),
                ContextCompat.getColor(context!!, R.color.indigoAccent),
                ContextCompat.getColor(context!!, R.color.blueAccent),
                ContextCompat.getColor(context!!, R.color.blueLightAccent),
                ContextCompat.getColor(context!!, R.color.cyanAccent),
                ContextCompat.getColor(context!!, R.color.tealAccent),
                ContextCompat.getColor(context!!, R.color.greenAccent),
                ContextCompat.getColor(context!!, R.color.greenLightAccent),
                ContextCompat.getColor(context!!, R.color.limeAccent),
                ContextCompat.getColor(context!!, R.color.yellowAccent),
                ContextCompat.getColor(context!!, R.color.amberAccent),
                ContextCompat.getColor(context!!, R.color.orangeAccent),
                ContextCompat.getColor(context!!, R.color.orangeDeepAccent),
                ContextCompat.getColor(context!!, R.color.pureWhite),
                ContextCompat.getColor(context!!, R.color.pureBlack)
        )
    }

    override fun onImageSelected(uri: Uri?, clipData: ClipData?) {
        addImageFromUri(uri)
    }

    override fun onBitmapReady(bitmap: Bitmap) {
        binding.drawView.addBitmap(bitmap)
        switchPrefsPanel(DrawView.Mode.IMAGE)
    }

    companion object {
        fun newInstance(): DrawFragment {
            return DrawFragment()
        }
    }
}
