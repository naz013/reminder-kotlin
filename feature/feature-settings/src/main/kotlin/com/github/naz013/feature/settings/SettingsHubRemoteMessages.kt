package com.github.naz013.feature.settings

interface SettingsHubRemoteMessages {
  fun addUpdateObserver(observer: UpdateObserver)
  fun removeUpdateObserver(observer: UpdateObserver)
  fun addSaleObserver(observer: SaleObserver)
  fun removeSaleObserver(observer: SaleObserver)
  fun addMessageObserver(observer: MessageObserver)
  fun removeMessageObserver(observer: MessageObserver)

  interface UpdateObserver {
    fun onUpdateChanged(
      hasUpdate: Boolean,
      version: String,
    )
  }

  interface SaleObserver {
    fun onSaleChanged(
      showDiscount: Boolean,
      discount: String,
      until: String,
    )
  }

  interface MessageObserver {
    fun onMessageChanged(
      showMessage: Boolean,
      message: String,
    )
  }
}
