package com.elementary.tasks.notes.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ActivityImageEditBinding
import com.elementary.tasks.notes.preview.ImagesSingleton
import org.koin.android.ext.android.inject
import timber.log.Timber

class ImageEditActivity : ThemedActivity<ActivityImageEditBinding>(), EditInterface {

    private var fragment: BitmapFragment<*>? = null
    private var imageFile: ImageFile? = null
    private var currentImage: ByteArray? = null
    private var state = 0

    private val imagesSingleton: ImagesSingleton by inject()

    override fun layoutRes(): Int = R.layout.activity_image_edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()
        binding.typeIcon.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.INVISIBLE
        binding.typeIcon.setOnClickListener { toggleScreen() }
        binding.backButton.setOnClickListener { closeScreen() }
        binding.saveButton.setOnClickListener { saveImage() }
    }

    private fun toggleScreen() {
        if (state == 0) {
            selectTab(1)
        } else {
            selectTab(0)
        }
    }

    override fun onStart() {
        super.onStart()

        val image = imagesSingleton.getEditable()
        this.imageFile = image
        this.currentImage = image?.image

        if (image != null) {
            showImage(image)
        }
    }

    private fun closeOk() {
        setResult(RESULT_OK)
        finish()
    }

    private fun showImage(note: ImageFile) {
        Timber.d("showImage: ")
        currentImage = note.image
        imageFile = note
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
        val builder = dialogues.getMaterialDialog(this)
        builder.setMessage(R.string.which_image_you_want_to_use)
        builder.setPositiveButton(R.string.edited) { dialogInterface, _ ->
            dialogInterface.dismiss()
            imageFile?.image = fragment?.image
            switchTab(position)
        }
        builder.setNegativeButton(R.string.original) { dialogInterface, _ ->
            dialogInterface.dismiss()
            imageFile?.image = fragment?.originalImage
            switchTab(position)
        }
        builder.create().show()
    }

    private fun askCrop(position: Int) {
        val builder = dialogues.getMaterialDialog(this)
        builder.setMessage(R.string.which_image_you_want_to_use)
        builder.setPositiveButton(R.string.cropped) { dialogInterface, _ ->
            dialogInterface.dismiss()
            imageFile?.image = fragment?.image
            switchTab(position)
        }
        builder.setNegativeButton(R.string.original) { dialogInterface, _ ->
            dialogInterface.dismiss()
            imageFile?.image = fragment?.originalImage
            switchTab(position)
        }
        builder.create().show()
    }

    private fun switchTab(position: Int) {
        Timber.d("switchTab: $position")
        state = position
        if (position == 1) {
            binding.typeIcon.setImageResource(R.drawable.ic_twotone_crop_24px)
            openDrawFragment()
        } else {
            binding.typeIcon.setImageResource(R.drawable.ic_random_line)
            openCropFragment()
        }
    }

    private fun openDrawFragment() {
        replaceFragment(DrawFragment.newInstance())
    }

    private fun openCropFragment() {
        replaceFragment(CropFragment.newInstance())
    }

    private fun replaceFragment(fragment: BitmapFragment<*>) {
        this.fragment = fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment, null)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun initActionBar() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onBackPressed() {
        if (fragment?.onBackPressed() == true) {
            return
        }
        closeScreen()
    }

    private fun closeScreen() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Module.isMarshmallow) {
            fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun saveImage() {
        val image = fragment?.image
        val imageFile = this.imageFile
        if (image != null && imageFile != null) {
            imageFile.image = image
            imagesSingleton.setEditable(imageFile)
            closeOk()
        }
    }

    override fun getCurrent(): ByteArray? {
        return currentImage
    }

    override fun saveCurrent(byteArray: ByteArray?) {
        this.currentImage = byteArray
    }

    override fun getOriginal(): ByteArray? {
        return imageFile?.image
    }
}
