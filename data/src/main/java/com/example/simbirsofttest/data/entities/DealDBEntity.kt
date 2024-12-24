package com.example.simbirsofttest.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "deal")
data class DealDBEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long?,
    val name: String,
    val description: String?,
    @ColumnInfo(name = "date_start", index = true)
    val dateStart: Date,
    @ColumnInfo(name = "date_finish")
    val dateEnd: Date,
)