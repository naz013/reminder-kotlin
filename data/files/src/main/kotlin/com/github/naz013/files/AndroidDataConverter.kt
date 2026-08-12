package com.github.naz013.files

import android.net.Uri

interface AndroidDataConverter {
  suspend fun toData(uri: Uri): Any?
}
