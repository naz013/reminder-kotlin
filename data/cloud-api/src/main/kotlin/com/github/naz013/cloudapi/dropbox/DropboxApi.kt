package com.github.naz013.cloudapi.dropbox

import com.github.naz013.cloudapi.CloudFileApi

interface DropboxApi : CloudFileApi {
  fun initialize(): Boolean
  fun disconnect()
}
