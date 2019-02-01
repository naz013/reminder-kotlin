package com.elementary.tasks.notes.editor

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentCropImageBinding
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
class CropFragment : BitmapFragment<FragmentCropImageBinding>() {

    override val image: ByteArray?
        get() {
            val cropped = binding.cropImageView.croppedImage
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

    override fun layoutRes(): Int = R.layout.fragment_crop_image

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
        binding.cropImageView.clearImage()
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
                    .addListener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            binding.cropImageView.setImageBitmap(resource)
                            return false
                        }
                    })
                    .submit()
        }
    }

    private fun initControls() {
        binding.rotateLeftButton.setOnClickListener { binding.cropImageView.rotateImage(-90) }
        binding.rotateRightButton.setOnClickListener { binding.cropImageView.rotateImage(90) }
    }

    companion object {
        fun newInstance(): CropFragment {
            return CropFragment()
        }
    }
}
