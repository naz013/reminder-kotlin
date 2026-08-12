package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private const val GROUP_SIZE = 3

/**
 * Groups digits with a space every [GROUP_SIZE] characters (e.g. "1234567890" -> "123 456 789 0")
 * for readability, leaving a leading "+" (international prefix) ungrouped. Purely cosmetic - the
 * underlying field value is untouched, so no locale-aware phone number formatting library is
 * needed.
 */
object PhoneNumberVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val raw = text.text
    val hasPlusPrefix = raw.startsWith("+")
    val digits = if (hasPlusPrefix) raw.substring(1) else raw
    val prefixLength = if (hasPlusPrefix) 1 else 0

    val formatted =
      buildString {
        if (hasPlusPrefix) append('+')
        digits.forEachIndexed { index, char ->
          if (index != 0 && index % GROUP_SIZE == 0) append(' ')
          append(char)
        }
      }

    val offsetMapping =
      object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
          if (offset <= prefixLength) return offset
          val digitOffset = (offset - prefixLength).coerceIn(0, digits.length)
          return (prefixLength + digitOffset + digitOffset / GROUP_SIZE).coerceIn(0, formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
          if (offset <= prefixLength) return offset
          val transformedDigitOffset = (offset - prefixLength).coerceIn(0, formatted.length - prefixLength)
          val spaces = transformedDigitOffset / (GROUP_SIZE + 1)
          return (prefixLength + transformedDigitOffset - spaces).coerceIn(0, raw.length)
        }
      }

    return TransformedText(AnnotatedString(formatted), offsetMapping)
  }
}
