package com.elementary.tasks.settings.calendar.country

private const val REGIONAL_INDICATOR_OFFSET = 0x1F1E6 - 'A'.code

/**
 * Converts an ISO 3166-1 alpha-2 country code into its flag emoji by composing two Unicode
 * "Regional Indicator Symbol" codepoints - no bundled flag assets needed. Renders as an actual
 * flag on devices/fonts with emoji flag-sequence support, or as two boxed letters otherwise (very
 * old Android versions) - a well-known, acceptable trade-off for this approach.
 */
fun countryCodeToFlagEmoji(code: String): String {
  val upper = code.uppercase()
  if (upper.length != 2 || upper.any { it !in 'A'..'Z' }) return code

  return upper
    .map { char -> String(Character.toChars(char.code + REGIONAL_INDICATOR_OFFSET)) }
    .joinToString(separator = "")
}
