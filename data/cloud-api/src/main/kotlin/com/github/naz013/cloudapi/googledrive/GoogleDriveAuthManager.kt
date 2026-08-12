package com.github.naz013.cloudapi.googledrive

interface GoogleDriveAuthManager {
  fun isAuthorized(): Boolean
  fun hasGooglePlayServices(): Boolean
  fun getUserName(): String
  fun removeUserName()
  fun saveUserName(name: String)
  fun getScopes(): List<String>
}
