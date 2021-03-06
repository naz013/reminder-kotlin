package com.elementary.tasks.settings.export.backups

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.chart.PieSlice
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.databinding.ListItemBackupInfoBinding
import com.elementary.tasks.settings.export.BackupsFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException

class InfoAdapter(
  private val layout: LinearLayout,
  private val mCallback: ((BackupsFragment.Info?) -> Unit)?
) {

  private val view: ListItemBackupInfoBinding
    get() = ListItemBackupInfoBinding.inflate(LayoutInflater.from(layout.context))

  init {
    layout.removeAllViewsInLayout()
  }

  fun setData(data: List<UserItem>) {
    layout.removeAllViewsInLayout()
    for (userItem in data) {
      val binding = view
      fillInfo(binding, userItem)
      layout.addView(binding.root)
    }
  }

  private fun fillInfo(binding: ListItemBackupInfoBinding, model: UserItem?) {
    if (model != null) {
      binding.moreButton.setOnClickListener { view -> showPopup(model.kind, view) }
      if (model.kind == BackupsFragment.Info.Local) {
        binding.userContainer.visibility = View.GONE
        binding.sourceName.text = binding.root.context.getString(R.string.local)
      } else {
        binding.userContainer.visibility = View.VISIBLE
        if (model.kind == BackupsFragment.Info.Google) {
          binding.sourceName.text = binding.root.context.getString(R.string.google_drive)
        } else if (model.kind == BackupsFragment.Info.Dropbox) {
          binding.sourceName.text = binding.root.context.getString(R.string.dropbox)
        }
      }
      val name = model.name
      if (!TextUtils.isEmpty(name)) {
        binding.cloudUser.text = name
      }
      val photoLink = model.photo
      if (photoLink.isNotEmpty()) {
        loadImage(photoLink, binding.userPhoto)
      }
      showQuota(binding, model)
      binding.cloudCount.text = model.count.toString()
    }
  }

  private fun showPopup(kind: BackupsFragment.Info?, view: View) {
    val popupMenu = PopupMenu(view.context, view)
    popupMenu.inflate(R.menu.popup_menu)
    popupMenu.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.delete_all -> {
          mCallback?.invoke(kind)
          return@setOnMenuItemClickListener true
        }
      }
      false
    }
    popupMenu.show()
  }

  private fun showQuota(binding: ListItemBackupInfoBinding, model: UserItem) {
    val quota = model.quota
    if (quota != 0L) {
      val availQ = quota - model.used
      val free = (availQ * 100.0f / quota).toInt().toFloat()
      val used = (model.used * 100.0f / quota).toInt().toFloat()
      binding.usedSizeGraph.removeSlices()
      var slice = PieSlice()
      val usTitle = String.format(binding.root.context.getString(R.string.used_x), used.toString())
      slice.title = usTitle
      slice.color = ContextCompat.getColor(binding.root.context, R.color.secondaryRed)
      slice.value = used
      binding.usedSizeGraph.addSlice(slice)
      slice = PieSlice()
      val avTitle = String.format(binding.root.context.getString(R.string.available_x), free.toString())
      slice.title = avTitle
      slice.color = ContextCompat.getColor(binding.root.context, R.color.secondaryGreen)
      slice.value = free
      binding.usedSizeGraph.addSlice(slice)
      binding.usedSpace.text = String.format(binding.root.context.getString(R.string.used_x),
        MemoryUtil.humanReadableByte(model.used, false))
      binding.freeSpace.text = String.format(binding.root.context.getString(R.string.available_x),
        MemoryUtil.humanReadableByte(availQ, false))
    }
  }

  private fun loadImage(photoLink: String, userPhoto: ImageView) {
    val dir = MemoryUtil.imagesDir
    val image = File(dir, FILE_NAME)
    Glide.with(userPhoto).load(image).into(userPhoto)
    userPhoto.visibility = View.VISIBLE
    if (!image.exists()) {
      saveImageFile(userPhoto.context, photoLink)
    }
  }

  private fun saveImageFile(context: Context, photoLink: String) {
    Thread {
      try {
        val bitmap = Glide.with(context)
          .asBitmap()
          .load(photoLink)
          .submit().get()
        val dir1 = MemoryUtil.imagesDir
        val image1 = File(dir1, FILE_NAME)
        if (image1.createNewFile()) {
          val stream = FileOutputStream(image1)
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
          stream.close()
        }
      } catch (e: InterruptedException) {
        e.printStackTrace()
      } catch (e: ExecutionException) {
        e.printStackTrace()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }.start()
  }

  companion object {

    private const val FILE_NAME = "Google_photo.jpg"
  }
}
