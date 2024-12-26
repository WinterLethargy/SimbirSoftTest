package com.example.simbirsofttest.feature.deal.extensions

import android.os.Bundle
import com.example.simbirsofttest.data.models.Deal
import java.time.LocalDate

private const val YEAR = "YEAR"
private const val MONTH = "MONTH"
private const val DAY = "DAY"

fun LocalDate.toBundle() = Bundle().apply {
    putInt(YEAR, year)
    putInt(MONTH, monthValue)
    putInt(DAY, dayOfMonth)
}

fun Bundle.readLocalDate() = LocalDate.of(
    getInt(YEAR),
    getInt(MONTH),
    getInt(DAY),
)

private const val ID = "ID"
private const val NAME = "NAME"
private const val DESCRIPTION = "DESCRIPTION"
private const val DATE = "DATE"
private const val START_HOUR = "START_HOUR"
private const val DURATION = "DURATION"

fun Deal.toBundle() = Bundle().apply{
    id?.let{ putLong(ID, it) }
    putString(NAME, name)
    putString(DESCRIPTION, description)
    putBundle(DATE, date.toBundle())
    putInt(START_HOUR, startHour)
    putInt(DURATION, duration)
}

fun Bundle.readDeal() = Deal(
    getLong(ID).let{ if (it == 0L) null else it },
    getString(NAME)!!,
    getString(DESCRIPTION)!!,
    getBundle(DATE)!!.readLocalDate(),
    getInt(START_HOUR),
    getInt(DURATION),
)