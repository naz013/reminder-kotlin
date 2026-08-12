package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.github.naz013.domain.note.NoteWithImages

@Keep
internal data class NoteWithImagesEntity(
  @Embedded
  val note: NoteEntity? = null,
  @Relation(parentColumn = "key", entityColumn = "noteId")
  val images: List<ImageFileEntity> = ArrayList()
) {

  constructor(noteWithImages: NoteWithImages) : this(
    noteWithImages.note?.let { NoteEntity(it) },
    noteWithImages.images.map { ImageFileEntity(it) }
  )

  fun toDomain(): NoteWithImages {
    return NoteWithImages(
      note = note?.toDomain(),
      images = images.map { it.toDomain() }
    )
  }
}
