package com.elementary.tasks.core.cloud.storages

import android.os.Build
import androidx.annotation.Keep
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.daysAfter
import com.elementary.tasks.core.utils.hoursAfter
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.koin.core.KoinComponent
import timber.log.Timber


class TokenDataFile : KoinComponent {

    private val devices: MutableList<DeviceToken> = mutableListOf()
    private var lastUpdateTime = 0L
    var isLoading = false
    var isLoaded = false
        private set

    fun isOld(): Boolean = (System.currentTimeMillis() - lastUpdateTime).hoursAfter() > 1

    fun isEmpty(): Boolean = devices.isEmpty()

    fun parse(json: String?) {
        Timber.d("parse: $json")
        try {
            val tokens = Gson().fromJson(json, Tokens::class.java)
            if (tokens != null) {
                val oldTokens = this.devices.toList()
                val newTokens = mergeTokens(tokens.tokens, oldTokens)
                if (oldTokens != newTokens) {
                    this.devices.clear()
                    this.devices.addAll(newTokens)
                }
                lastUpdateTime = System.currentTimeMillis()
            }
        } catch (e: Exception) {
        }
        isLoaded = true
        isLoading = false
    }

    private fun mergeTokens(newTokens: List<DeviceToken>, oldTokens: List<DeviceToken>): List<DeviceToken> {
        Timber.d("mergeTokens: $oldTokens, NEW $newTokens")
        val list = mutableListOf<DeviceToken>()
        for (token in newTokens) {
            list.add(selectToken(token, oldTokens))
        }
        for (token in oldTokens) {
            if (!containsToken(token, list)) {
                list.add(token)
            }
        }
        return list
    }

    private fun containsToken(token: DeviceToken, list: List<DeviceToken>): Boolean {
        for (t in list) {
            if (t.model == token.model) {
                return true
            }
        }
        return false
    }

    private fun selectToken(token: DeviceToken, list: List<DeviceToken>): DeviceToken {
        for (t in list) {
            if (t.model == token.model && TimeUtil.isAfterDate(t.updatedAt, token.updatedAt)) {
                return t
            }
        }
        return token
    }

    fun toJson(): String? {
        removeOldTokens()
        val json = Gson().toJson(Tokens(this.devices.toList()))
        Timber.d("toJson: $json")
        return json
    }

    fun notifyDevices() {
        removeOldTokens()
        val withoutMe = this.devices.filter { it.model != myDevice() }.map { it.token }
        if (withoutMe.isEmpty()) {
            Timber.d("notifyDevices: NO DEVICES")
            return
        }
        val notification = Notification(withoutMe.toList(), "sync", myDevice(), TimeUtil.gmtDateTime)
        Timber.d("notifyDevices: $notification")
        val database = FirebaseDatabase.getInstance()
        database.reference.child("notifications")
                .setValue(notification)
                .addOnSuccessListener { Timber.d("notifyDevices: FD WRITE SUCCESS") }
                .addOnFailureListener { Timber.d("notifyDevices: FD WRITE ERROR: ${it.message}") }
    }

    fun addDevice(token: String): Boolean {
        removeOldTokens()
        var currentDevice = findCurrent()
        if (currentDevice == null) {
            currentDevice = DeviceToken(myDevice(), TimeUtil.gmtDateTime, token)
        } else {
            if (TimeUtil.getDateTimeFromGmt(currentDevice.updatedAt).hoursAfter() < 12) {
                return false
            }
        }
        currentDevice.token = token
        val withoutMe = this.devices.filter { it.model != myDevice() }
        this.devices.clear()
        this.devices.addAll(withoutMe)
        this.devices.add(currentDevice)
        return true
    }

    private fun findCurrent(): DeviceToken? {
        if (devices.isEmpty()) return null
        for (d in devices) {
            if (d.model == myDevice()) {
                return d
            }
        }
        return null
    }

    private fun removeOldTokens() {
        val filtered = devices.filter { TimeUtil.getDateTimeFromGmt(it.updatedAt).daysAfter() < 30 }
        this.devices.clear()
        this.devices.addAll(filtered)
    }

    private fun myDevice(): String {
        return Build.MANUFACTURER + " " + Build.MODEL
    }

    @Keep
    data class Notification(
            @SerializedName("tokens")
            var tokens: List<String> = listOf(),
            @SerializedName("type")
            var type: String = "",
            @SerializedName("sender")
            var sender: String = "",
            @SerializedName("createdAt")
            var createdAt: String = ""
    )

    @Keep
    data class DeviceToken(
            @SerializedName("model")
            var model: String = "",
            @SerializedName("updatedAt")
            var updatedAt: String = "",
            @SerializedName("token")
            var token: String = ""
    )

    @Keep
    data class Tokens(
            @SerializedName("tokens")
            var tokens: List<DeviceToken> = listOf()
    )

    companion object {
        const val FILE_NAME = "tokens.json"
    }
}