package com.github.naz013.sync

interface SyncSettings {
  fun isDataTypeEnabled(dataType: DataType): Boolean
}
