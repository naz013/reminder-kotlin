package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.RemotePrefs
import com.github.naz013.feature.settings.SettingsHubRemoteMessages

class SettingsHubRemoteMessagesImpl(
  private val remotePrefs: RemotePrefs,
) : SettingsHubRemoteMessages {
  private val updateAdapters = mutableMapOf<SettingsHubRemoteMessages.UpdateObserver, RemotePrefs.UpdateObserver>()
  private val saleAdapters = mutableMapOf<SettingsHubRemoteMessages.SaleObserver, RemotePrefs.SaleObserver>()
  private val messageAdapters = mutableMapOf<SettingsHubRemoteMessages.MessageObserver, RemotePrefs.MessageObserver>()

  override fun addUpdateObserver(observer: SettingsHubRemoteMessages.UpdateObserver) {
    val adapter =
      object : RemotePrefs.UpdateObserver {
        override fun onUpdateChanged(hasUpdate: Boolean, version: String) {
          observer.onUpdateChanged(hasUpdate, version)
        }
      }
    updateAdapters[observer] = adapter
    remotePrefs.addUpdateObserver(adapter)
  }

  override fun removeUpdateObserver(observer: SettingsHubRemoteMessages.UpdateObserver) {
    val adapter = updateAdapters.remove(observer) ?: return
    remotePrefs.removeUpdateObserver(adapter)
  }

  override fun addSaleObserver(observer: SettingsHubRemoteMessages.SaleObserver) {
    val adapter =
      object : RemotePrefs.SaleObserver {
        override fun onSaleChanged(showDiscount: Boolean, discount: String, until: String) {
          observer.onSaleChanged(showDiscount, discount, until)
        }
      }
    saleAdapters[observer] = adapter
    remotePrefs.addSaleObserver(adapter)
  }

  override fun removeSaleObserver(observer: SettingsHubRemoteMessages.SaleObserver) {
    val adapter = saleAdapters.remove(observer) ?: return
    remotePrefs.removeSaleObserver(adapter)
  }

  override fun addMessageObserver(observer: SettingsHubRemoteMessages.MessageObserver) {
    val adapter =
      object : RemotePrefs.MessageObserver {
        override fun onMessageChanged(showMessage: Boolean, message: String) {
          observer.onMessageChanged(showMessage, message)
        }
      }
    messageAdapters[observer] = adapter
    remotePrefs.addMessageObserver(adapter)
  }

  override fun removeMessageObserver(observer: SettingsHubRemoteMessages.MessageObserver) {
    val adapter = messageAdapters.remove(observer) ?: return
    remotePrefs.removeMessageObserver(adapter)
  }
}
