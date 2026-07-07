package com.github.naz013.datecalc

import org.threeten.bp.LocalDateTime

fun LocalDateTime.minusMillis(millis: Long): LocalDateTime = minusSeconds(millis / 1000L)

fun LocalDateTime.plusMillis(millis: Long): LocalDateTime = plusSeconds(millis / 1000L)
