package com.github.naz013.cloudapi.dropbox

interface DropboxAuthManager {
  fun isAuthorized(): Boolean
  fun getOAuth2Token(): String
  fun removeOAuth2Token()
  fun startAuth()
  fun onAuthFinished()
}
