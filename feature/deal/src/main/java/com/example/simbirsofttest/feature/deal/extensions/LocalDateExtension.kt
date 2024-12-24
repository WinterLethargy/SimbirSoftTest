package com.example.simbirsofttest.feature.deal.extensions

import java.time.LocalDate

fun LocalDate.toEpochMillis() = toEpochDay() * 24 * 60 * 60 * 1000