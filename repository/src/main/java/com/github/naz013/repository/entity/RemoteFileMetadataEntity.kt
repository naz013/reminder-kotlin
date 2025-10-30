package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.sync.RemoteFileMetadata

@Entity(tableName = "RemoteFileMetadata")
internal data class RemoteFileMetadataEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val lastModified: Long,
  val size: Int,
  val source: String,
  val localUuId: String?,
  val fileExtension: String,
  val version: Long,
  val rev: String,
) {

  constructor(remoteFileMetadata: RemoteFileMetadata) : this(
    id = remoteFileMetadata.id,
    name = remoteFileMetadata.name,
    lastModified = remoteFileMetadata.lastModified,
    size = remoteFileMetadata.size,
    source = remoteFileMetadata.source,
    localUuId = remoteFileMetadata.localUuId,
    fileExtension = remoteFileMetadata.fileExtension,
    version = remoteFileMetadata.version,
    rev = remoteFileMetadata.rev,
  )

  fun toDomain(): RemoteFileMetadata = RemoteFileMetadata(
    id = id,
    name = name,
    lastModified = lastModified,
    size = size,
    source = source,
    localUuId = localUuId,
    fileExtension = fileExtension,
    version = version,
    rev = rev,
  )
}
