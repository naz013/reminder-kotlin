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
import com.elementary.tasks.notes.editor.layers.LayersRecyclerAdapter
import com.elementary.tasks.notes.editor.layers.OnStartDragListener
import com.elementary.tasks.notes.editor.layers.SimpleItemTouchHelperCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_draw_image.*
import kotlinx.android.synthetic.main.view_draw_tools.view.*
import kotlinx.android.synthetic.main.view_image_prefs.view.*
import kotlinx.android.synthetic.main.view_layers_prefs.view.*
import kotlinx.android.synthetic.main.view_standard_prefs.view.*
import kotlinx.android.synthetic.main.view_text_prefs.view.*
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
class DrawFragment : BitmapFragment(), PhotoSelectionUtil.UriCallback {

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
            draw_view.removeObserver(mAdapter)

            val binding = layoutInflater.inflate(R.layout.view_layers_prefs, null)
            binding.layersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            mAdapter.onStartDragListener =  object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                    mItemTouchHelper?.startDrag(viewHolder)
                }
            }
            mAdapter.mCallback = object : LayersRecyclerAdapter.AdapterCallback {
                override fun onChanged() {
                    draw_view.invalidate()
                }

                override fun onItemSelect(position: Int) {
                    mAdapter.setIndex(position)
                    draw_view.setHistoryPointer(position + 1)
                }

                override fun onItemRemoved(position: Int) {
                    draw_view.setHistoryPointer(draw_view.getHistoryPointer() - 1)
                }

                override fun onItemAdded() {
                    binding.layersList.scrollToPosition(mAdapter.itemCount - 1)
                }
            }

            mAdapter.setData(draw_view.elements)
            mAdapter.setIndex(draw_view.getHistoryPointer() - 1)
            val callback = SimpleItemTouchHelperCallback(mAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper?.attachToRecyclerView(binding.layersList)
            binding.layersList.adapter = mAdapter
            draw_view.addObserver(mAdapter)
            return binding
        }

    override fun getTitle(): String = ""

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
            binding.imageButton.setOnClickListener { photoSelectionUtil.selectImage() }
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
            return binding
        }

    private val toolsPanel: View
        get() {
            val binding = layoutInflater.inflate(R.layout.view_draw_tools, null)
            binding.penButton.setOnClickListener { toolClick(it) }
            binding.lineButton.setOnClickListener { toolClick(it) }
            binding.cubicBezierButton.setOnClickListener { toolClick(it) }
            binding.textButton.setOnClickListener { toolClick(it) }
            binding.rectangleButton.setOnClickListener { toolClick(it) }
            binding.ellipseButton.setOnClickListener { toolClick(it) }
            binding.circleButton.setOnClickListener { toolClick(it) }
            binding.imageToolButton.setOnClickListener { toolClick(it) }
            binding.fillToolButton.setOnClickListener { toolClick(it) }
            return binding
        }

    override val image: ByteArray?
        get() {
            val image = draw_view.getBitmapAsByteArray(Bitmap.CompressFormat.PNG, 100)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_draw_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        draw_view.setCallback(mDrawCallback)
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
        draw_view.paintStrokeColor = color
        draw_view.paintFillColor = color
        setCurrentColor(color)
    }

    private fun initControls() {
        undoButton.setOnClickListener { undo() }
        redoButton.setOnClickListener { redo() }
        clearButton.setOnClickListener { draw_view.clear() }
        currentToolButton.setOnClickListener { showToolPanel() }
        currentToolButton.setOnLongClickListener {
            switchPrefsPanel(draw_view.mode)
            return@setOnLongClickListener true
        }
        currentStrokeColorButton.setOnClickListener { showColorPanel() }
        checkRedo()
        checkUndo()
        layersButton.setOnClickListener { switchPrefsPanel(DrawView.Mode.LAYERS) }
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
        switchPrefsPanel(draw_view.mode)
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
        builder.setSingleChoiceItems(adapter, draw_view.getFontFamily()) { dialog, which ->
            draw_view.setFontFamily(which)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setCurrentColor(@ColorInt color: Int) {
        currentStrokeColorButton.setColorFilter(color)
    }

    private fun setCurrentTool(button: AppCompatImageView) {
        currentToolButton.setImageDrawable(button.drawable)
        currentToolButton.contentDescription = button.contentDescription
    }

    private fun setDrawMode(mode: DrawView.Mode, drawer: DrawView.Drawer?) {
        draw_view.mode = mode
        if (drawer != null) draw_view.drawer = drawer
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
        draw_view.addText(text)
    }

    private fun loadImage() {
        val image = editInterface?.getCurrent()
        if (image != null) {
            draw_view.addBitmap(image)
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
        draw_view.addBitmap(bitmap)
        switchPrefsPanel(DrawView.Mode.IMAGE)
    }

    companion object {
        fun newInstance(): DrawFragment {
            return DrawFragment()
        }
    }
}
