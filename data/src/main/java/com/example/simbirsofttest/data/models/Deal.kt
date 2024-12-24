package com.example.simbirsofttest.data.models

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Deal(
    val id : Long?,
    val name: String,
    val description: String,
    val date: LocalDate,
    val startHour: Int,
    val duration: Int,
){
    companion object{
        fun create(
            name: String,
            description: String,
            date: LocalDate,
            hourStart: Int,
            duration: Int,
        ) = Deal(
            null,
            name,
            description,
            date,
            hourStart,
            duration,
        )
    }
}

val Deal.dateTimeStart get(): LocalDateTime = LocalDateTime.of(date, LocalTime.of(startHour,0))
val Deal.dateTimeEnd get(): LocalDateTime = dateTimeStart + Duration.ofHours(duration.toLong())
