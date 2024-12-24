package com.example.simbirsofttest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.simbirsofttest.data.converters.Converters
import com.example.simbirsofttest.data.daos.DealDao
import com.example.simbirsofttest.data.entities.DealDBEntity

@Database(entities = [DealDBEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
internal abstract class SSDatabase: RoomDatabase() {
    abstract fun dealDao(): DealDao
}