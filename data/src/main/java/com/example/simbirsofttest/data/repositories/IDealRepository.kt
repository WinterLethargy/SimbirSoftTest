package com.example.simbirsofttest.data.repositories

import com.example.simbirsofttest.data.models.Deal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IDealRepository {
    suspend fun insert(deal: Deal) : Long
    suspend fun update(deal: Deal)
    suspend fun getFlow(id: Long): Flow<Deal?>
    suspend fun getDeal(id: Long): Deal?
    fun getByDay(date: LocalDate): Flow<List<Deal>>
}