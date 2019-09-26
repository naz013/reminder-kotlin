package com.elementary.tasks.core.utils

import android.content.Context
import android.content.pm.PackageManager
import com.elementary.tasks.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber

class RemotePrefs(context: Context) {

    private val mFirebaseRemoteConfig: FirebaseRemoteConfig? = try {
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
                .build()
        this.mFirebaseRemoteConfig?.setConfigSettings(configSettings)
        this.mFirebaseRemoteConfig?.setDefaults(R.xml.remote_config_defaults)
        fetchConfig()
    }

    private fun fetchConfig() {
        mFirebaseRemoteConfig?.fetch(3600)?.addOnCompleteListener { task ->
            Timber.d("fetchConfig: ${task.isSuccessful}")
            if (task.isSuccessful) {
                mFirebaseRemoteConfig?.activateFetched()
            }
            displayVersionMessage()
            if (!Module.isPro) displaySaleMessage()
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
        val versionCode = mFirebaseRemoteConfig?.getLong(VERSION_CODE) ?: 0
        try {
            val pInfo = pm.getPackageInfo(packageName, 0)
            val verCode = pInfo.versionCode
            if (versionCode > verCode) {
                val version = mFirebaseRemoteConfig?.getString(VERSION_NAME) ?: ""
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
        val isSale = mFirebaseRemoteConfig?.getBoolean(SALE_STARTED) ?: false
        if (isSale) {
            val expiry = mFirebaseRemoteConfig?.getString(SALE_EXPIRY_DATE) ?: ""
            val discount = mFirebaseRemoteConfig?.getString(SALE_VALUE) ?: ""
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
    }
}
