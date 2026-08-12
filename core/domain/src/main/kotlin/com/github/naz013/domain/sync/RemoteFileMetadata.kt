package com.github.naz013.domain.sync

data class RemoteFileMetadata(
  val id: String,
  val name: String,
  val lastModified: Long,
  val size: Int,
  val source: String,
  val localUuId: String?,
  val fileExtension: String,
  val version: Long,
  val rev: String,
)
