package com.example.simbirsofttest.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.simbirsofttest.data.entities.DealDBEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface DealDao {

    @Insert
    suspend fun insert(deal: DealDBEntity): Long

    @Update
    suspend fun update(deal: DealDBEntity)

    @Query("SELECT * FROM deal WHERE id = :id")
    fun getFlow(id: Long): Flow<DealDBEntity?>

    @Query("SELECT * FROM deal WHERE id = :id")
    fun getDeal(id: Long): DealDBEntity?

    @Query("SELECT * FROM deal WHERE date_start BETWEEN :startOfDay AND :startOfDay + 24 * 60 * 60 * 1000 - 1")
    fun getByDay(startOfDay: Long): Flow<List<DealDBEntity>>
}