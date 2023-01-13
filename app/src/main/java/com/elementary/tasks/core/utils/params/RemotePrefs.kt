package com.elementary.tasks.core.utils.params

import android.content.Context
import android.content.pm.PackageManager
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.Module
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber

class RemotePrefs(
  context: Context,
  private val prefs: Prefs
) {

  private val config: FirebaseRemoteConfig? = try {
    FirebaseRemoteConfig.getInstance()
  } catch (e: Exception) {
    null
  }

  private val mObservers = mutableListOf<SaleObserver>()
  private val mUpdateObservers = mutableListOf<UpdateObserver>()
  private val pm: PackageManager = context.packageManager
  private val packageName: String = context.packageName

  init {
    val configSettings = FirebaseRemoteConfigSettings.Builder()
      .setMinimumFetchIntervalInSeconds(60)
      .build()
    this.config?.setConfigSettingsAsync(configSettings)
    this.config?.setDefaultsAsync(R.xml.remote_config_defaults)
  }

  fun preLoad() {
    fetchConfig()
  }

  private fun fetchConfig() {
    config?.fetchAndActivate()?.addOnCompleteListener { task ->
      Timber.d("fetchConfig: ${task.isSuccessful}, ${task.exception}")
      if (task.isSuccessful) {
        config.fetchAndActivate()
      }
      reaAppConfigs()
      readFeatureFlags()
      displayVersionMessage()
      if (!Module.isPro) displaySaleMessage()
    }?.addOnFailureListener {
      it.printStackTrace()
    }
  }

  private fun reaAppConfigs() {
    val privacyUrl = config?.getString(PRIVACY_POLICY_URL)
    val voiceHelpUrls = config?.getString(VOICE_HELP_URLS)

    Timber.d("RemoteConfig: privacyUrl=$privacyUrl")
    Timber.d("RemoteConfig: voiceHelpJson=$voiceHelpUrls")

    privacyUrl?.also { prefs.privacyUrl = it }
    voiceHelpUrls?.also { prefs.voiceHelpUrls = it }
  }

  private fun readFeatureFlags() {
    FeatureManager.Feature.values().map {
      it to (readBool(it.value) ?: true)
    }.forEach {
      Timber.d("Feature ${it.first} isEnabled=${it.second}")
      prefs.putBoolean(it.first.value, it.second)
    }
  }

  private fun readBool(key: String): Boolean? {
    return config?.getBoolean(key).also {
      Timber.d("Read bool key=$key, val=$it")
    }
  }

  fun addUpdateObserver(observer: UpdateObserver) {
    if (!mUpdateObservers.contains(observer)) {
      mUpdateObservers.add(observer)
    }
    fetchConfig()
  }

  fun removeUpdateObserver(observer: UpdateObserver) {
    if (mUpdateObservers.contains(observer)) {
      mUpdateObservers.remove(observer)
    }
  }

  fun addSaleObserver(observer: SaleObserver) {
    if (!mObservers.contains(observer)) {
      mObservers.add(observer)
    }
    fetchConfig()
  }

  fun removeSaleObserver(observer: SaleObserver) {
    if (mObservers.contains(observer)) {
      mObservers.remove(observer)
    }
  }

  private fun displayVersionMessage() {
    val versionCode = config?.getLong(VERSION_CODE) ?: 0
    try {
      val pInfo = pm.getPackageInfo(packageName, 0)
      val verCode = pInfo.versionCode
      if (versionCode > verCode) {
        val version = config?.getString(VERSION_NAME) ?: ""
        for (observer in mUpdateObservers) {
          observer.onUpdate(version)
        }
        return
      }
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }

    notifyNoUpdate()
  }

  private fun notifyNoUpdate() {
    for (observer in mUpdateObservers) {
      observer.noUpdate()
    }
  }

  private fun displaySaleMessage() {
    val isSale = config?.getBoolean(SALE_STARTED) ?: false
    if (isSale) {
      val expiry = config?.getString(SALE_EXPIRY_DATE) ?: ""
      val discount = config?.getString(SALE_VALUE) ?: ""
      for (observer in mObservers) {
        observer.onSale(discount, expiry)
      }
      return
    }
    notifyNoSale()
  }

  private fun notifyNoSale() {
    for (observer in mObservers) {
      observer.noSale()
    }
  }

  interface UpdateObserver {
    fun onUpdate(version: String)

    fun noUpdate()
  }

  interface SaleObserver {
    fun onSale(discount: String, expiryDate: String)

    fun noSale()
  }

  companion object {

    private const val SALE_STARTED = "sale_started"
    private const val SALE_VALUE = "sale_save_value"
    private const val SALE_EXPIRY_DATE = "sale_until_time_utc"

    private const val VERSION_CODE = "version_code"
    private const val VERSION_NAME = "version_name"

    private const val PRIVACY_POLICY_URL = "privacy_policy_link"
    private const val VOICE_HELP_URLS = "voice_help_urls"
  }
}
