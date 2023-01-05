package com.elementary.tasks.core.data.ui.note

data class UiNoteImage(
  val id: Int,
  val data: ByteArray?,
  val state: UiNoteImageState = UiNoteImageState.READY
) {

  override fun toString(): String {
    return "UiNoteImage(id=$id, data=${data != null}, state=$state)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as UiNoteImage

    if (id != other.id) return false
    if (data != null) {
      if (other.data == null) return false
      if (!data.contentEquals(other.data)) return false
    } else if (other.data != null) return false
    if (state != other.state) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id
    result = 31 * result + (data?.contentHashCode() ?: 0)
    result = 31 * result + state.hashCode()
    return result
  }
}
