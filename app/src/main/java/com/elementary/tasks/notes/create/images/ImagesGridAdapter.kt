package com.elementary.tasks.notes.create.images

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import coil.load
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.ListItemNoteImageBinding

class ImagesGridAdapter : ListAdapter<UiNoteImage, ImagesGridAdapter.PhotoViewHolder>(
  UiNoteImageDiffCallback()
) {

  var isEditable: Boolean = false
  var actionsListener: ActionsListener<UiNoteImage>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
    return PhotoViewHolder(parent)
  }

  override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class PhotoViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemNoteImageBinding>(
    ListItemNoteImageBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(noteImage: UiNoteImage) {
      binding.stateLoading.visibleGone(noteImage.state == UiNoteImageState.LOADING)
      binding.stateReady.visibleGone(noteImage.state != UiNoteImageState.LOADING)
      if (noteImage.state != UiNoteImageState.LOADING) {
        loadImage(binding.photoView, noteImage)
      }
    }

    init {
      binding.photoView.setOnClickListener { view -> performClick(view, bindingAdapterPosition) }
      if (isEditable) {
        binding.removeButton.visibility = View.VISIBLE
        binding.removeButton.setOnClickListener {
          actionsListener?.onAction(
            it,
            bindingAdapterPosition,
            getItem(bindingAdapterPosition),
            ListActions.REMOVE
          )
        }
      } else {
        binding.removeButton.visibility = View.GONE
      }
    }

    private fun performClick(view: View, position: Int) {
      actionsListener?.onAction(view, position, null, ListActions.OPEN)
    }

    private fun loadImage(imageView: ImageView, image: UiNoteImage) {
      imageView.load(image.filePath)
    }
  }
}
