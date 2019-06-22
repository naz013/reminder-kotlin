package com.elementary.tasks.core.data.models

import android.os.Parcelable
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.elementary.tasks.core.interfaces.RecyclerInterface
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Keep
@Parcelize
data class Birthday(
        @SerializedName("name")
        var name: String = "",
        @SerializedName("date")
        var date: String = "",
        @SerializedName("number")
        var number: String = "",
        @SerializedName("key")
        var key: String = "",
        @SerializedName("showedYear")
        var showedYear: Int = 0,
        @SerializedName("contactId")
        var contactId: Long = 0L,
        @SerializedName("day")
        var day: Int = 0,
        @SerializedName("month")
        var month: Int = 0,
        @SerializedName("uniqueId")
        var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
        @SerializedName("dayMonth")
        var dayMonth: String = "",
        @SerializedName("uuId")
        @PrimaryKey
        var uuId: String = UUID.randomUUID().toString(),
        @SerializedName("updatedAt")
        var updatedAt: String? = null,
        @Ignore
        var calculatedTime: Long = 0L
) : RecyclerInterface, Parcelable {

    override val viewType: Int
        get() = 2

    @Ignore
    constructor(name: String, date: String, number: String, showedYear: Int, contactId: Long, day: Int, month: Int) : this() {
        this.name = name
        this.date = date
        this.number = number
        val secKey = if (TextUtils.isEmpty(number)) "0" else number.substring(1)
        this.key = "$name|$secKey"
        this.showedYear = showedYear
        this.contactId = contactId
        this.day = day
        this.month = month
        this.dayMonth = "$day|$month"
    }

    fun calculateTime(time: Long) {
        calculatedTime = TimeUtil.getFutureBirthdayDate(time, date)?.millis ?: 0L
    }

    fun getDateTime(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val year = calendar.get(Calendar.YEAR)
        calendar.timeInMillis = time
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        return calendar.timeInMillis
    }
}
