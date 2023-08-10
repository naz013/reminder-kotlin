package com.elementary.tasks.core.data.platform

import kotlin.experimental.and
import kotlin.experimental.or

data class Bytes(private val hexString: String) {

  private val bytes: ByteArray = hexString.chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()

  constructor(value: Int) : this("%X".format(value))

  constructor(byteArray: ByteArray) : this(byteArray.toHexString())

  fun toHexString() = bytes.toHexString()

  fun isBitSet(byte: Int, bit: Int): Boolean {
    return bytes[byte] and (1 shl bit).toByte() != 0.toByte()
  }

  fun setBit(byte: Int, bit: Int) {
    bytes[byte] = bytes[byte] or (1 shl bit).toByte()
  }

  fun unSetBit(byte: Int, bit: Int) {
    bytes[byte] = bytes[byte] and (1 shl bit).inv().toByte()
  }

  override fun toString(): String {
    return "Bytes(hex=${toHexString()})"
  }

  companion object {
    val ZERO = Bytes(0)
    val EMPTY = Bytes(ByteArray(0))
  }
}

fun ByteArray.toHexString() = joinToString("") {
  (0xFF and it.toInt()).toString(16).padStart(2, '0')
}
