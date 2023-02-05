package com.elementary.tasks.core.data.platform

data class ReminderCreatorConfig(private val value: String) {

  private val bytes = Bytes(value)

  constructor(): this(DEFAULT_VALUE)

  fun toHex(): String {
    return bytes.toHexString()
  }

  fun isRepeatLimitPickerEnabled(): Boolean {
    return bytes.isBitSet(0, REPEAT_LIMIT_PICKER_BIT)
  }

  fun setRepeatLimitPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, REPEAT_LIMIT_PICKER_BIT, enabled)
  }

  fun isRepeatPickerEnabled(): Boolean {
    return bytes.isBitSet(0, REPEAT_PICKER_BIT)
  }

  fun setRepeatPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, REPEAT_PICKER_BIT, enabled)
  }

  fun isBeforePickerEnabled(): Boolean {
    return bytes.isBitSet(0, BEFORE_PICKER_BIT)
  }

  fun setBeforePickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, BEFORE_PICKER_BIT, enabled)
  }

  fun isPriorityPickerEnabled(): Boolean {
    return bytes.isBitSet(0, PRIORITY_PICKER_BIT)
  }

  fun setPriorityPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, PRIORITY_PICKER_BIT, enabled)
  }

  fun isMelodyPickerEnabled(): Boolean {
    return bytes.isBitSet(0, MELODY_PICKER_BIT)
  }

  fun setMelodyPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, MELODY_PICKER_BIT, enabled)
  }

  fun isLoudnessPickerEnabled(): Boolean {
    return bytes.isBitSet(0, LOUDNESS_PICKER_BIT)
  }

  fun setLoudnessPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, LOUDNESS_PICKER_BIT, enabled)
  }

  fun isCalendarPickerEnabled(): Boolean {
    return bytes.isBitSet(0, CALENDAR_PICKER_BIT)
  }

  fun setCalendarPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, CALENDAR_PICKER_BIT, enabled)
  }

  fun isGoogleTasksPickerEnabled(): Boolean {
    return bytes.isBitSet(0, TASKS_PICKER_BIT)
  }

  fun setGoogleTasksPickerEnabled(enabled: Boolean) {
    updateBitAndByte(0, TASKS_PICKER_BIT, enabled)
  }

  fun isTuneExtraPickerEnabled(): Boolean {
    return bytes.isBitSet(1, EXTRA_PICKER_BIT)
  }

  fun setTuneExtraPickerEnabled(enabled: Boolean) {
    updateBitAndByte(1, EXTRA_PICKER_BIT, enabled)
  }

  fun isAttachmentPickerEnabled(): Boolean {
    return bytes.isBitSet(1, ATTACHMENT_PICKER_BIT)
  }

  fun setAttachmentPickerEnabled(enabled: Boolean) {
    updateBitAndByte(1, ATTACHMENT_PICKER_BIT, enabled)
  }

  fun isLedPickerEnabled(): Boolean {
    return bytes.isBitSet(1, LED_PICKER_BIT)
  }

  fun setLedPickerEnabled(enabled: Boolean) {
    updateBitAndByte(1, LED_PICKER_BIT, enabled)
  }

  fun isWindowTypePickerEnabled(): Boolean {
    return bytes.isBitSet(1, WINDOW_TYPE_PICKER_BIT)
  }

  fun setWindowTypePickerEnabled(enabled: Boolean) {
    updateBitAndByte(1, WINDOW_TYPE_PICKER_BIT, enabled)
  }

  private fun updateBitAndByte(byte: Int, bit: Int, enabled: Boolean) {
    if (enabled) {
      bytes.setBit(byte, bit)
    } else {
      bytes.unSetBit(byte, bit)
    }
  }

  override fun toString(): String {
    return "ReminderCreatorConfig(bytes=$bytes)"
  }


  companion object {
    const val DEFAULT_VALUE = "FFFF0000"

    // first byte
    private const val BEFORE_PICKER_BIT = 0
    private const val REPEAT_PICKER_BIT = 1
    private const val REPEAT_LIMIT_PICKER_BIT = 2
    private const val PRIORITY_PICKER_BIT = 3
    private const val MELODY_PICKER_BIT = 4
    private const val LOUDNESS_PICKER_BIT = 5
    private const val CALENDAR_PICKER_BIT = 6
    private const val TASKS_PICKER_BIT = 7

    // second byte
    private const val EXTRA_PICKER_BIT = 0
    private const val ATTACHMENT_PICKER_BIT = 1
    private const val LED_PICKER_BIT = 2
    private const val WINDOW_TYPE_PICKER_BIT = 3
  }
}
