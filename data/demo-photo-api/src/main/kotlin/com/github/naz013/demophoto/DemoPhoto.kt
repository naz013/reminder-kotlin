package com.github.naz013.demophoto

/** A downloaded showcase photo plus the attribution its source requires. */
class DemoPhoto(
  val bytes: ByteArray,
  val photographerName: String,
  val sourcePageUrl: String,
)
