package com.github.naz013.cloudapi.googledrive

import com.github.naz013.cloudapi.CloudFileApi

interface GoogleDriveApi : CloudFileApi {
  fun initialize(): Boolean
  fun disconnect()
}
