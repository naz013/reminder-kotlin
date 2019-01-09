package com.elementary.tasks.notes.editor

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.fragment_crop_image.*
import java.io.ByteArrayOutputStream

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
class CropFragment : BitmapFragment() {

    override val image: ByteArray?
        get() {
            val cropped = cropImageView.croppedImage
            val outputStream = ByteArrayOutputStream()
            var img: ByteArray? = null
            if (cropped != null) {
                cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                img = outputStream.toByteArray()
                editInterface?.saveCurrent(img)
            }
            return img
        }

    override val originalImage: ByteArray?
        get() = editInterface?.getOriginal()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_crop_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initControls()
    }

    override fun onResume() {
        super.onResume()
        loadImage()
    }

    override fun onPause() {
        super.onPause()
        cropImageView.clearImage()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun getTitle(): String = ""

    private fun loadImage() {
        val image = editInterface?.getCurrent()
        if (image != null) {
            Glide.with(context!!)
                    .asBitmap()
                    .load(image)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            cropImageView.setImageBitmap(resource)
                        }
                    })
        }
    }

    private fun initControls() {
        rotateLeftButton.setOnClickListener { cropImageView.rotateImage(-90) }
        rotateRightButton.setOnClickListener { cropImageView.rotateImage(90) }
    }

    companion object {
        fun newInstance(): CropFragment {
            return CropFragment()
        }
    }
}
