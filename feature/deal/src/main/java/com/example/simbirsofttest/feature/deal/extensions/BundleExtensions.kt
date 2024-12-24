package com.example.simbirsofttest.feature.deal.extensions

import android.os.Bundle
import com.example.simbirsofttest.feature.deal.constants.DateConstant
import java.time.LocalDate

fun LocalDate.toBundle(): Bundle {
    return Bundle().apply {
        putInt(DateConstant.YEAR, year)
        putInt(DateConstant.MONTH, monthValue)
        putInt(DateConstant.DAY, dayOfMonth)
    }
}

fun Bundle.readLocalDate(): LocalDate {
    val year = getInt(DateConstant.YEAR)
    val month = getInt(DateConstant.MONTH)
    val day = getInt(DateConstant.DAY)
    return LocalDate.of(year, month, day)
}