package com.elementary.tasks.notes.preview.carousel

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import coil.load
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.databinding.ListItemNoteImageCarouselBinding
import com.elementary.tasks.notes.create.images.UiNoteImageDiffCallback

class ImagesCarouselAdapter : ListAdapter<UiNoteImage, ImagesCarouselAdapter.PhotoViewHolder>(
  UiNoteImageDiffCallback()
) {

  var actionsListener: ActionsListener<UiNoteImage>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
    return PhotoViewHolder(parent)
  }

  override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class PhotoViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemNoteImageCarouselBinding>(
    ListItemNoteImageCarouselBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(noteImage: UiNoteImage) {
      loadImage(binding.photoView, noteImage)
    }

    init {
      binding.photoView.setOnClickListener { view -> performClick(view, bindingAdapterPosition) }
    }

    private fun performClick(view: View, position: Int) {
      actionsListener?.onAction(view, position, null, ListActions.OPEN)
    }

    private fun loadImage(imageView: ImageView, image: UiNoteImage) {
      imageView.load(image.filePath)
    }
  }
}
