package com.elementary.tasks.core.cloud

import android.os.Build
import androidx.annotation.Keep
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.daysAfter
import com.elementary.tasks.core.utils.launchDefault
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import timber.log.Timber

class TokenDataFile {

    private val devices: MutableList<DeviceToken> = mutableListOf()

    fun parse(json: String?) {
        val tokens = Gson().fromJson(json, Tokens::class.java)
        this.devices.clear()
        this.devices.addAll(tokens.tokens)
    }

    fun toJson(): String? {
        removeOldTokens()
        return Gson().toJson(Tokens(this.devices))
    }

    fun notifyDevices() {
        removeOldTokens()
        val withoutMe = this.devices.filter { it.model != myDevice() }.map { it.token }

        val sender = NotificationSender(myDevice())
        val data = NotificationData("sync", sender)
        val notification = Notification(withoutMe.toList(), data)
        val bytes = Gson().toJson(notification).toByteArray()
        sendPost(bytes)
    }

    private fun removeOldTokens() {
        val filtered = devices.filter { TimeUtil.getDateTimeFromGmt(it.updatedAt).daysAfter() > 30 }
        this.devices.clear()
        this.devices.addAll(filtered)
    }

    private fun myDevice(): String {
        return Build.MANUFACTURER + " " + Build.MODEL
    }

    private fun sendPost(data: ByteArray) = launchDefault{
        val client = OkHttpClient.Builder().build()
        val body = RequestBody.create(MediaType.get("JSON"), data)
        val httpRequest = Request.Builder()
                .post(body)
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAPY5Nv3w:APA91bEFpU39hl6PYE5jIwQ4kgZJY_SnOqC9j30bYbD4lyfx02SK3DdjhzzrZVw58Y_CNtnC272JcHuH-45g4GieVfaexll6CZXxEy2PNi4G8WI7hENV7hFW_YdnWHSgwJijEL2MS4uB")
                .build()
        try {
            val response = client.newCall(httpRequest).execute()
            if (response.code() == 200 || response.code() == 206) {
                Timber.d("sendPost: %s", "OK")
            } else {
                Timber.d("sendPost: %s", response.message())
            }
        } catch (e: Exception) {
            Timber.d("sendPost: %s", e.message)
        }
    }

    @Keep
    data class Notification(
            @SerializedName("registration_ids")
            var registrationIds: List<String> = listOf(),
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
    }
}