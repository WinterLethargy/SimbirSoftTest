package com.example.simbirsofttest.data.utils

import com.example.simbirsofttest.core.utils.toDate
import com.example.simbirsofttest.data.entities.DealDBEntity
import java.time.LocalDateTime

fun getTestDeal(): DealDBEntity {
    val dateStart = LocalDateTime.of(2000, 12, 21, 4, 0)
    val dateEnd = dateStart.plusHours(4)
    val deal = DealDBEntity(
        id = null,
        name = "Test Deal",
        description = "Test Description",
        dateStart = dateStart.toDate(),
        dateEnd = dateEnd.toDate(),
    )
    return deal
}