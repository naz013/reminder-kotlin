package com.github.naz013.cloudapi.stream

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CopyByteArrayStream : ByteArrayOutputStream() {
  fun toInputStream(): InputStream {
    return ByteArrayInputStream(this.buf, 0, this.count)
  }
}
