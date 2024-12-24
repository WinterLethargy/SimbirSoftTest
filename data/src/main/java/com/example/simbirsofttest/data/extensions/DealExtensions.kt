package com.example.simbirsofttest.data.extensions

import com.example.simbirsofttest.core.utils.empty
import com.example.simbirsofttest.core.utils.toDate
import com.example.simbirsofttest.core.utils.toLocalDateTime
import com.example.simbirsofttest.data.entities.DealDBEntity
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.data.models.dateTimeEnd
import com.example.simbirsofttest.data.models.dateTimeStart
import java.time.Duration

internal fun Deal.toDealDBEntity() = DealDBEntity(
    id = id,
    name = name,
    description = description,
    dateStart = dateTimeStart.toDate(),
    dateEnd = dateTimeEnd.toDate(),
    )

internal fun DealDBEntity.toDeal(): Deal{
    val localDateTimeStart = dateStart.toLocalDateTime()
    val localDateTimeEnd = dateEnd.toLocalDateTime()
    val duration = Duration.between(localDateTimeStart, localDateTimeEnd)
        .toHours()
        .toInt()
    return Deal(
        id = id,
        name = name,
        description = description ?: String.empty,
        date = localDateTimeStart.toLocalDate(),
        startHour = localDateTimeStart.hour,
        duration = duration,
    )
}

