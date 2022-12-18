package com.elementary.tasks.core.utils.io

import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.io.Writer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.MalformedInputException

class StreamWriter(
  outputStream: OutputStream
) : Writer() {

  private var outputBuffer: CharBuffer? = CharBuffer.allocate(1024)
  private var out: OutputStream? = outputStream
  private var encoder: CharsetEncoder? = null

  init {
    outputBuffer = null
    encoder = try {
      val encoding = System.getProperty("file.encoding")
      val cs = Charset.forName(encoding)
      cs.newEncoder()
    } catch (e: RuntimeException) {
      null
    }
    if (encoder != null) {
      encoder?.onMalformedInput(CodingErrorAction.REPLACE)
      encoder?.onUnmappableCharacter(CodingErrorAction.REPLACE)
      outputBuffer = CharBuffer.allocate(1024)
    }
  }

  override fun write(str: String?) {
    this.write(str, 0, str?.length ?: 0)
  }

  override fun write(str: String?, off: Int, len: Int) {
    if (str == null) throw IOException("String is null.")
    this.write(str.toCharArray(), off, len)
  }

  override fun write(cbuf: CharArray) {
    this.write(cbuf, 0, cbuf.size)
  }

  override fun write(c: Int) {
    this.write(charArrayOf(c.toChar()), 0, 1)
  }

  override fun write(buf: CharArray?, offset: Int, count: Int) {
    if (out == null) throw IOException("Stream is closed.")
    if (buf == null) throw IOException("Buffer is null.")
    Timber.d("write: $out")
    var o = offset
    var c = count
    val outputBuffer = outputBuffer
    if (outputBuffer != null) {
      if (c >= outputBuffer.remaining()) {
        val r = outputBuffer.remaining()
        outputBuffer.put(buf, o, r)
        writeConvert(outputBuffer.array(), 0, 1024)
        outputBuffer.clear()
        o += r
        c -= r
        if (c >= outputBuffer.remaining()) {
          writeConvert(buf, o, c)
          return
        }
      }
      outputBuffer.put(buf, o, c)
    } else writeConvert(buf, o, c)
  }

  override fun flush() {
    Timber.d("flush: ")
    out?.let { o ->
      outputBuffer?.let { buffer ->
        val buf = CharArray(buffer.position())
        if (buf.isNotEmpty()) {
          buffer.flip()
          buffer.get(buf)
          writeConvert(buf, 0, buf.size)
          buffer.clear()
        }
      }
      o.flush()
    }
  }

  private fun writeConvert(buf: CharArray, offset: Int, count: Int) {
    if (encoder == null) {
      val b = ByteArray(count)
      for (i in 0 until count)
        b[i] = (if (buf[offset + i] <= 0xFF.toChar()) buf[offset + i] else '?').toByte()
      Timber.d("writeConvert: 0 $out")
      out?.write(b)
    } else {
      try {
        val output = encoder!!.encode(CharBuffer.wrap(buf, offset, count))
        encoder?.reset()
        if (output.hasArray()) {
          Timber.d("writeConvert: 1 $out")
          out?.write(output.array())
        } else {
          val outbytes = ByteArray(output.remaining())
          output.get(outbytes)
          Timber.d("writeConvert: 2 $out")
          out?.write(outbytes)
        }
      } catch (e: IllegalStateException) {
        throw IOException("Internal error.")
      } catch (e: MalformedInputException) {
        throw IOException("Invalid character sequence.")
      }
    }
  }

  override fun close() {
    Timber.d("close: ")
    if (out == null)
      return
    flush()
    out?.close()
    out = null
  }
}