package com.elementary.tasks.core.utils.params

import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.remote.InternalMessageV1
import com.elementary.tasks.core.utils.params.remote.SaleMessageV2
import com.elementary.tasks.core.utils.params.remote.UpdateMessageV2
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import timber.log.Timber

class RemotePrefs(
  private val prefs: Prefs,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val dateTimeManager: DateTimeManager,
  private val language: Language
) {

  private val config: FirebaseRemoteConfig? = try {
    FirebaseRemoteConfig.getInstance()
  } catch (e: Exception) {
    null
  }

  private val mSaleObservers = mutableListOf<SaleObserver>()
  private val mUpdateObservers = mutableListOf<UpdateObserver>()
  private val mMessageObservers = mutableListOf<MessageObserver>()

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
      readAppConfigs()
      readFeatureFlags()
      readUpdateMessage()
      readInternalMessage()
      if (!Module.isPro) {
        readSaleMessage()
      }
    }?.addOnFailureListener {
      it.printStackTrace()
    }
  }

  private fun readUpdateMessage() {
    val json = config?.getString(UPDATE_MESSAGE)

    val updateMessage =
      runCatching { Gson().fromJson(json, UpdateMessageV2::class.java) }.getOrNull()

    Timber.d("readUpdateMessage: json=$json")
    Timber.d("readUpdateMessage: message=$updateMessage")

    if (updateMessage != null) {
      val currentVersionCode = packageManagerWrapper.getVersionCode()
      if (updateMessage.versionCode > currentVersionCode) {
        notifyUpdateObservers(true, updateMessage.versionName)
      } else {
        notifyUpdateObservers(false, "")
      }
    } else {
      notifyUpdateObservers(false, "")
    }
  }

  private fun notifyUpdateObservers(hasUpdate: Boolean, version: String) {
    for (observer in mUpdateObservers) {
      observer.onUpdateChanged(hasUpdate, version)
    }
  }

  private fun readSaleMessage() {
    val json = config?.getString(PRO_SALE_MESSAGE)

    val saleMessageV2 = runCatching { Gson().fromJson(json, SaleMessageV2::class.java) }.getOrNull()

    Timber.d("readSaleMessage: json=$json")
    Timber.d("readSaleMessage: message=$saleMessageV2")

    if (saleMessageV2 != null) {
      prefs.saleMessage = json ?: ""
      checkSaleMessage(saleMessageV2)
    } else {
      val oldJson = prefs.saleMessage.takeIf { it.isNotEmpty() } ?: return
      runCatching {
        Gson().fromJson(oldJson, SaleMessageV2::class.java)?.also { checkSaleMessage(it) }
      }
    }
  }

  private fun checkSaleMessage(saleMessageV2: SaleMessageV2) {
    val now = dateTimeManager.getCurrentDateTime()
    val startDateTime = dateTimeManager.fromRfc3339ToLocal(saleMessageV2.startAt)
    val endDateTime = dateTimeManager.fromRfc3339ToLocal(saleMessageV2.endAt)

    Timber.d("checkSaleMessage: now=$now")
    Timber.d("checkSaleMessage: startDateTime=$startDateTime")
    Timber.d("checkSaleMessage: endDateTime=$endDateTime")

    if (startDateTime != null && endDateTime != null) {
      if (now.isAfter(startDateTime) && now.isBefore(endDateTime)) {
        val userDateTime = dateTimeManager.getFullDateTime(endDateTime)
        notifySaleObservers(true, saleMessageV2.salePercentage, userDateTime)
      } else {
        notifySaleObservers(false, "", "")
      }
    } else {
      notifySaleObservers(false, "", "")
    }
  }

  private fun notifySaleObservers(hasSale: Boolean, discount: String, endDate: String) {
    for (observer in mSaleObservers) {
      observer.onSaleChanged(hasSale, discount, endDate)
    }
  }

  private fun readInternalMessage() {
    val json = config?.getString(INTERNAL_MESSAGE)

    val internalMessageV1 =
      runCatching { Gson().fromJson(json, InternalMessageV1::class.java) }.getOrNull()

    Timber.d("readInternalMessage: json=$json")
    Timber.d("readInternalMessage: message=$internalMessageV1")

    if (internalMessageV1 != null) {
      prefs.internalMessage = json ?: ""
      checkInternalMessage(internalMessageV1)
    } else {
      val oldJson = prefs.internalMessage.takeIf { it.isNotEmpty() } ?: return
      runCatching {
        Gson().fromJson(oldJson, InternalMessageV1::class.java)?.also { checkInternalMessage(it) }
      }
    }
  }

  private fun checkInternalMessage(internalMessageV1: InternalMessageV1) {
    val now = dateTimeManager.getCurrentDateTime()
    val startDateTime = dateTimeManager.fromRfc3339ToLocal(internalMessageV1.startAt)
    val endDateTime = dateTimeManager.fromRfc3339ToLocal(internalMessageV1.endAt)

    Timber.d("checkInternalMessage: now=$now")
    Timber.d("checkInternalMessage: startDateTime=$startDateTime")
    Timber.d("checkInternalMessage: endDateTime=$endDateTime")

    if (startDateTime != null && endDateTime != null) {
      if (now.isAfter(startDateTime) && now.isBefore(endDateTime)) {
        notifyMessageObservers(true, getMessageText(internalMessageV1))
      } else {
        notifyMessageObservers(false, "")
      }
    } else {
      notifyMessageObservers(false, "")
    }
  }

  private fun getMessageText(internalMessageV1: InternalMessageV1): String {
    return if (internalMessageV1.localized.isEmpty()) {
      internalMessageV1.message
    } else {
      val locale = language.getCurrentLocale().lowercase()
      internalMessageV1.localized
        .firstOrNull { it.lang.lowercase() == locale }
        ?.text
        ?: internalMessageV1.message
    }
  }

  private fun notifyMessageObservers(showMessage: Boolean, message: String) {
    for (observer in mMessageObservers) {
      observer.onMessageChanged(showMessage, message)
    }
  }

  private fun readAppConfigs() {
    val privacyUrl = config?.getString(PRIVACY_POLICY_URL)
    val termsUrl = config?.getString(TERMS_URL)
    val voiceHelpUrls = config?.getString(VOICE_HELP_URLS)

    Timber.d("RemoteConfig: privacyUrl=$privacyUrl")
    Timber.d("RemoteConfig: termsUrl=$termsUrl")
    Timber.d("RemoteConfig: voiceHelpJson=$voiceHelpUrls")

    privacyUrl?.also { prefs.privacyUrl = it }
    termsUrl?.also { prefs.termsUrl = it }
    voiceHelpUrls?.also { prefs.voiceHelpUrls = it }
  }

  private fun readFeatureFlags() {
    FeatureManager.Feature.entries.map {
      it to (readBool(it.value) ?: it.defaultValue)
    }.forEach {
      Traces.d("Feature ${it.first} isEnabled=${it.second}")
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
    if (!mSaleObservers.contains(observer)) {
      mSaleObservers.add(observer)
    }
    fetchConfig()
  }

  fun removeSaleObserver(observer: SaleObserver) {
    if (mSaleObservers.contains(observer)) {
      mSaleObservers.remove(observer)
    }
  }

  fun addMessageObserver(observer: MessageObserver) {
    if (!mMessageObservers.contains(observer)) {
      mMessageObservers.add(observer)
    }
    fetchConfig()
  }

  fun removeMessageObserver(observer: MessageObserver) {
    if (mMessageObservers.contains(observer)) {
      mMessageObservers.remove(observer)
    }
  }

  interface UpdateObserver {
    fun onUpdateChanged(hasUpdate: Boolean, version: String)
  }

  interface SaleObserver {
    fun onSaleChanged(showDiscount: Boolean, discount: String, until: String)
  }

  interface MessageObserver {
    fun onMessageChanged(showMessage: Boolean, message: String)
  }

  companion object {

    private const val PRIVACY_POLICY_URL = "privacy_policy_link"
    private const val TERMS_URL = "terms_link"
    private const val VOICE_HELP_URLS = "voice_help_urls"

    private const val UPDATE_MESSAGE = "update_message_v2"
    private const val PRO_SALE_MESSAGE = "pro_sale_message_v2"
    private const val INTERNAL_MESSAGE = "internal_message_v1"
  }
}
