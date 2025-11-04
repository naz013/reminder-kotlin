package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.domain.sync.RemoteFileMetadata

internal class CreateRemoteFileMetadataUseCase(
  private val getLocalUuIdUseCase: GetLocalUuIdUseCase
) {
  operator fun invoke(
    source: String,
    cloudFile: CloudFile,
    any: Any
  ): RemoteFileMetadata {
    return RemoteFileMetadata(
      id = cloudFile.id,
      name = cloudFile.name,
      lastModified = cloudFile.lastModified,
      size = cloudFile.size,
      source = source,
      localUuId = getLocalUuIdUseCase(any),
      fileExtension = cloudFile.fileExtension,
      version = cloudFile.version,
      rev = cloudFile.rev
    )
  }
}
