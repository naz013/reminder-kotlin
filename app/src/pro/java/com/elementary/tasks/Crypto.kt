package com.elementary.tasks

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object Helper {

    fun getRandomString(): String = SecureRandom().nextLong().toString()

    fun getRandomBytes(size: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }

    fun getRawBytes(text: String): ByteArray {
        return try {
            text.toByteArray(Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            text.toByteArray()
        }
    }

    fun getString(data: ByteArray): String {
        return try {
            String(data, Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            String(data)
        }

    }

    fun base64Decode(text: String): ByteArray = Base64.decode(text, Base64.NO_WRAP)

    fun base64Encode(data: ByteArray): String = Base64.encodeToString(data, Base64.NO_WRAP)
}

object Hashing {
    private const val MD5 = "MD5"
    private const val SHA_1 = "SHA-1"
    private const val SHA_256 = "SHA-256"
    private val DIGITS_LOWER = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    private val DIGITS_UPPER = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    private fun md5(data: ByteArray): String = String(encodeHex(md5Bytes(data)))

    fun md5(text: String): String = String(encodeHex(md5Bytes(Helper.getRawBytes(text))))

    private fun md5Bytes(data: ByteArray): ByteArray = getDigest(MD5).digest(data)

    fun md5Object(form: Serializable): String {
        val baos = ByteArrayOutputStream()
        val os = ObjectOutputStream(baos)
        os.writeObject(form)
        return md5(baos.toByteArray())
    }

    fun sha1(data: ByteArray): String = String(encodeHex(sha1Bytes(data)))

    fun sha1(text: String): String = String(encodeHex(sha1Bytes(Helper.getRawBytes(text))))

    private fun sha1Bytes(data: ByteArray): ByteArray = getDigest(SHA_1).digest(data)

    fun sha256(data: ByteArray): String = String(encodeHex(sha256Bytes(data)))

    fun sha256(text: String): String = String(encodeHex(sha256Bytes(Helper.getRawBytes(text))))

    private fun sha256Bytes(data: ByteArray): ByteArray = getDigest(SHA_256).digest(data)

    private fun getDigest(algorithm: String): MessageDigest {
        try {
            return MessageDigest.getInstance(algorithm)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException(e)
        }

    }

    private fun encodeHex(data: ByteArray, toLowerCase: Boolean = true): CharArray =
            encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)

    private fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
        val l = data.size
        val out = CharArray(l shl 1)
        var i = 0
        var j = 0
        while (i < l) {
            out[j++] = toDigits[(240 and data[i].toInt()).ushr(4)]
            out[j++] = toDigits[15 and data[i].toInt()]
            i++
        }
        return out
    }

}