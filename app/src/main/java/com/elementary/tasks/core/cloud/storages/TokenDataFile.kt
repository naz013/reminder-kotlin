package com.elementary.tasks.core.cloud.storages

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.daysAfter
import com.elementary.tasks.core.utils.launchDefault
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber


class TokenDataFile : KoinComponent {

    private val context: Context by inject()
    private val devices: MutableList<DeviceToken> = mutableListOf()
    var isLoaded = false
        private set

    fun parse(json: String?) {
        Timber.d("parse: $json")
        try {
            val tokens = Gson().fromJson(json, Tokens::class.java)
            if (tokens != null) {
                val oldTokens = this.devices.toList()
                this.devices.clear()
                this.devices.addAll(mergeTokens(tokens.tokens, oldTokens))
            }
        } catch (e: Exception) {
        }
        isLoaded = true
    }

    private fun mergeTokens(newTokens: List<DeviceToken>, oldTokens: List<DeviceToken>): List<DeviceToken> {
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

        val sender = NotificationSender(myDevice())
        val data = NotificationData("sync", sender)
        val notification = Notification(withoutMe.toList(), data)
        val json = Gson().toJson(notification)
        Timber.d("notifyDevices: $json")
//        sendPost(json.toByteArray())
    }

    fun addDevice(token: String) {
        removeOldTokens()
        var currentDevice = findCurrent()
        if (currentDevice == null) {
            currentDevice = DeviceToken(myDevice(), TimeUtil.gmtDateTime, token)
        }
        currentDevice.token = token
        val withoutMe = this.devices.filter { it.model != myDevice() }
        this.devices.clear()
        this.devices.addAll(withoutMe)
        this.devices.add(currentDevice)
    }

    private fun findCurrent(): DeviceToken? {
        for (d in devices) {
            if (d.model == myDevice()) {
                return d
            }
        }
        return null
    }

    private fun removeOldTokens() {
        val filtered = devices.filter { TimeUtil.getDateTimeFromGmt(it.updatedAt).daysAfter() > 30 }
        this.devices.clear()
        this.devices.addAll(filtered)
    }

    private fun myDevice(): String {
        return Build.MANUFACTURER + " " + Build.MODEL
    }

    private fun sendPost(data: ByteArray) = launchDefault {
        val client = OkHttpClient.Builder().build()
        try {
            val stream = context.resources.openRawResource(R.raw.admin_token)
            val googleCredential = GoogleCredential
                    .fromStream(stream)
                    .createScoped(listOf(SCOPE))
            googleCredential.refreshToken()
            val token = googleCredential.accessToken

            val body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), data)
            val httpRequest = Request.Builder()
                    .post(body)
                    .url("https://fcm.googleapis.com/v1/projects/${BuildConfig.PROJECT_ID}/messages:send")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

            val response = client.newCall(httpRequest).execute()
            if (response.code() == 200 || response.code() == 206) {
                Timber.d("sendPost: ${response.code()}, ${response.message()}")
            } else {
                Timber.d("sendPost: ${response.code()}, ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.d("sendPost: ${e.message}")
        }
    }

    @Keep
    data class Notification(
            @SerializedName("tokens")
            var tokens: List<String> = listOf(),
            @SerializedName("data")
            var data: NotificationData = NotificationData()
    )

    @Keep
    data class NotificationData(
            @SerializedName("type")
            var type: String = "",
            @SerializedName("sender")
            var sender: NotificationSender = NotificationSender()
    )

    @Keep
    data class NotificationSender(
            @SerializedName("name")
            var name: String = ""
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
        private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
    }
}