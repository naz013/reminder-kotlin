package com.github.naz013.repository

import com.github.naz013.domain.sync.RemoteFileMetadata

interface RemoteFileMetadataRepository {
  suspend fun save(remoteFileMetadata: RemoteFileMetadata)
  suspend fun getById(id: String): RemoteFileMetadata?
  suspend fun getByLocalUuId(uuId: String): RemoteFileMetadata?
  suspend fun getByLocalUuIdAndSource(uuId: String, source: String): RemoteFileMetadata?
  suspend fun getBySource(source: String): List<RemoteFileMetadata>
  suspend fun getAll(): List<RemoteFileMetadata>
  suspend fun delete(id: String)
  suspend fun deleteByLocalUuId(uuId: String)
  suspend fun deleteAll()
}
