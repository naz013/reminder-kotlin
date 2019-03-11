package com.elementary.tasks

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.google.gson.annotations.SerializedName
import java.util.*

class QrShareProvider(val themeUtil: ThemeUtil) {

//    val database = FirebaseDatabase.getInstance()
    private var data: ShareData? = null

    fun openScanner(activity: Activity, code: Int) {
        val intent = Intent()
        activity.startActivityForResult(intent, code)
    }

    fun verifyData(password: String, onReady: (type: String?, data: String?) -> Unit) {
        val shareData = data
        if (shareData == null) {
            onReady.invoke(null, null)
            return
        }
        val encPassword = Hashing.sha256(password)
        if (encPassword == shareData.password) {
            onReady.invoke(shareData.type, shareData.data)
        } else {
            onReady.invoke(null, null)
        }
    }

    fun readData(key: String, onReady: (Boolean) -> Unit) {
//        database.reference.child(CHILD_SHARE)
//                .child(key)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(error: DatabaseError) {
//                        Timber.d("onCancelled: ${error.message}")
//                        onReady.invoke(false)
//                    }
//
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        Timber.d("onDataChange: $dataSnapshot")
//                        val shareData = dataSnapshot.getValue(ShareData::class.java)
//                        if (shareData != null) {
//                            data = shareData
//                            onReady.invoke(true)
//                        } else {
//                            onReady.invoke(false)
//                        }
//                    }
//                })
    }

    fun shareData(type: String, data: String, password: String, onReady: (String?) -> Unit) {
        val encPassword = Hashing.sha256(password)
        val file = ShareData(type, data, encPassword)
        val key = UUID.randomUUID().toString()

//        database.reference.child(CHILD_SHARE)
//                .child(key)
//                .setValue(file)
//                .addOnSuccessListener { onReady.invoke(key) }
//                .addOnFailureListener { onReady.invoke(null) }
    }

    fun showQrImage(imageView: ImageView, qrData: String) {
        launchDefault {
//            val bitmap = QRCode.from(qrData)
//                    .withHint(EncodeHintType.MARGIN, 0)
//                    .withColor(themeUtil.getSecondaryColor(), 0x00ffffff)
//                    .bitmap()
//            withUIContext { imageView.setImageBitmap(bitmap) }
        }
    }

    fun hasQrSupport(): Boolean = true

    data class ShareData(
            @SerializedName("type")
            val type: String,
            @SerializedName("data")
            val data: String,
            @SerializedName("password")
            val password: String
    )

    companion object {
        const val TYPE_REMINDER = "reminder"
        const val CHILD_SHARE = "shared"
        const val INTENT_DATA = "intent_data"
    }
}