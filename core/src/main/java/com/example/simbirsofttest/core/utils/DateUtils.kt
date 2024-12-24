package com.example.simbirsofttest.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toDate(): Date = Date.from(atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

fun Date.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()