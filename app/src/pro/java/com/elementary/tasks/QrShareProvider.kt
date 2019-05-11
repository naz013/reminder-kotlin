package com.elementary.tasks

import android.content.Context
import android.util.Base64
import android.util.Base64DataException
import android.util.Base64InputStream
import android.util.Base64OutputStream
import android.widget.ImageView
import androidx.annotation.Keep
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.zxing.EncodeHintType
import net.glxn.qrgen.android.QRCode
import timber.log.Timber
import java.io.*

class QrShareProvider(val themeUtil: ThemeUtil) {

    val database = FirebaseDatabase.getInstance()
    private var data: ShareData? = null
    private var mKey: String? = null

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
        database.reference.child(CHILD_SHARE)
                .child(key)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        onReady.invoke(false)
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val shareData = dataSnapshot.getValue(ShareData::class.java)
                        if (shareData != null) {
                            data = shareData
                            onReady.invoke(true)
                        } else {
                            onReady.invoke(false)
                        }
                    }
                })
    }

    fun shareData(type: String, data: String, password: String, onReady: (String?) -> Unit) {
        val encPassword = Hashing.sha256(password)
        val file = ShareData(type, data, encPassword, TimeUtil.gmtDateTime)
        val key = generateKey()

        database.reference.child(CHILD_SHARE)
                .child(key)
                .setValue(file)
                .addOnSuccessListener {
                    onReady.invoke(key)
                    mKey = key
                    incrementCounter()
                }
                .addOnFailureListener { onReady.invoke(null) }
    }

    private fun incrementCounter() {
        database.reference.child(CHILD_COUNTER)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snap: DataSnapshot) {
                        var count = snap.getValue(Int::class.java) ?: 0
                        count++
                        database.reference.child(CHILD_COUNTER)
                                .setValue(count)
                                .addOnSuccessListener {  }
                                .addOnFailureListener {  }
                    }
                })
    }

    fun showQrImage(imageView: ImageView, qrData: String) {
        val uri = "reminderapp://key?key=$qrData"
        launchDefault {
            val bitmap = QRCode.from(uri)
                    .withHint(EncodeHintType.MARGIN, 0)
                    .withColor(ThemeUtil.getSecondaryColor(imageView.context), 0x00ffffff)
                    .bitmap()
            withUIContext { imageView.setImageBitmap(bitmap) }
        }
    }

    fun removeData() {
        mKey?.let {
            try {
                database.reference.child(CHILD_SHARE)
                        .child(it)
                        .removeValue()
            } catch (e: Exception) {
            }
        }
    }

    private fun generateKey(): String {
        var key = ""
        val values = "0123456789abcdefghijklmnpqrstuvxyz".toUpperCase().toCharArray()
        val random = java.util.Random()
        for (i in 0..5) {
            val index = random.nextInt(values.size)
            key += values[index]
        }
        return key
    }

    @Keep
    data class ShareData(
            val type: String? = "",
            val data: String? = "",
            val password: String? = "",
            val createdAt: String? = ""
    )

    companion object {

        const val TYPE_REMINDER = "reminder"
        const val CHILD_SHARE = "shared"
        const val CHILD_COUNTER = "shared_times"

        fun hasQrSupport(): Boolean = true

        fun openShareScreen(context: Context, data: String, type: String) {
            context.startActivity(ShareActivity.newShareScreen(context, data, type))
        }

        fun openImportScreen(context: Context) {
            context.startActivity(ImportActivity.newImportScreen(context))
        }

        fun generateEncryptedData(any: Any): String? {
            val data = Gson().toJson(any)
            val inputStream = ByteArrayInputStream(data.toByteArray())
            val buffer = ByteArray(8192)
            var bytesRead: Int
            val output = ByteArrayOutputStream()
            val output64 = Base64OutputStream(output, Base64.DEFAULT)
            try {
                do {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead != -1) {
                        output64.write(buffer, 0, bytesRead)
                    }
                } while (bytesRead != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            output64.close()
            val encData = output.toString()
            output.close()
            return encData
        }

        fun readData(data: String): String? {
            val inputStream = ByteArrayInputStream(data.toByteArray())
            val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
            val r = BufferedReader(InputStreamReader(output64))
            val total = StringBuilder()
            var line: String?
            try {
                do {
                    line = r.readLine()
                    if (line != null) {
                        total.append(line)
                    }
                } while (line != null)
            } catch (e: Base64DataException) {
                throw IOException("Bad JSON")
            }
            output64.close()
            inputStream.close()
            val res = total.toString()
            return if (res.startsWith("{") && res.endsWith("}") || res.startsWith("[") && res.endsWith("]")) {
                Timber.d("readFileToJson: $res")
                res
            } else {
                Timber.d("readFileToJson: Bad JSON")
                return null
            }
        }
    }
}