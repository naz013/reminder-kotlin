package com.github.naz013.repository.impl

import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.repository.dao.RemoteFileMetadataDao
import com.github.naz013.repository.entity.RemoteFileMetadataEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class RemoteFileMetadataRepositoryImpl(
  private val remoteFileMetadataDao: RemoteFileMetadataDao,
  private val tableChangeNotifier: TableChangeNotifier
) : RemoteFileMetadataRepository {
  override suspend fun save(remoteFileMetadata: RemoteFileMetadata) {
    remoteFileMetadataDao.insert(RemoteFileMetadataEntity(remoteFileMetadata))
    tableChangeNotifier.notify(Table.RemoteFileMetadata)
  }

  override suspend fun getById(id: String): RemoteFileMetadata? {
    return remoteFileMetadataDao.getById(id)?.toDomain()
  }

  override suspend fun getByLocalUuId(uuId: String): RemoteFileMetadata? {
    return remoteFileMetadataDao.getByLocalUuId(uuId)?.toDomain()
  }

  override suspend fun getByLocalUuIdAndSource(
    uuId: String,
    source: String
  ): RemoteFileMetadata? {
    return remoteFileMetadataDao.get(uuId, source)?.toDomain()
  }

  override suspend fun getBySource(source: String): List<RemoteFileMetadata> {
    return remoteFileMetadataDao.getBySource(source).map { it.toDomain() }
  }

  override suspend fun getAll(): List<RemoteFileMetadata> {
    return remoteFileMetadataDao.getAll().map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    remoteFileMetadataDao.delete(id)
    tableChangeNotifier.notify(Table.RemoteFileMetadata)
  }

  override suspend fun deleteByLocalUuId(uuId: String) {
    remoteFileMetadataDao.deleteByLocalUuId(uuId)
    tableChangeNotifier.notify(Table.RemoteFileMetadata)
  }

  override suspend fun deleteAll() {
    remoteFileMetadataDao.deleteAll()
    tableChangeNotifier.notify(Table.RemoteFileMetadata)
  }
}
