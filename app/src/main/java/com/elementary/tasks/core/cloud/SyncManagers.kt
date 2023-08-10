package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.completables.CompletableManager
import com.elementary.tasks.core.cloud.converters.ConverterManager
import com.elementary.tasks.core.cloud.repositories.RepositoryManager
import com.elementary.tasks.core.cloud.storages.StorageManager

class SyncManagers(
  val completableManager: CompletableManager,
  val converterManager: ConverterManager,
  val repositoryManager: RepositoryManager,
  val storageManager: StorageManager
)
