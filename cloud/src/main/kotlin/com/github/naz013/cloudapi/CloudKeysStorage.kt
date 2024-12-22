package com.github.naz013.cloudapi

interface CloudKeysStorage {
  fun getGoogleDriveUserName(): String
  fun saveGoogleDriveUserName(name: String)
  fun getDropboxToken(): String
  fun saveDropboxToken(token: String)
}
