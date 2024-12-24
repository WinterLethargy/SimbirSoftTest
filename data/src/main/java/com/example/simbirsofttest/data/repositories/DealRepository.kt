package com.example.simbirsofttest.data.repositories

import com.example.simbirsofttest.core.di.Dispatcher
import com.example.simbirsofttest.core.di.SSDispatchers
import com.example.simbirsofttest.core.utils.toDate
import com.example.simbirsofttest.data.daos.DealDao
import com.example.simbirsofttest.data.entities.DealDBEntity
import com.example.simbirsofttest.data.extensions.toDeal
import com.example.simbirsofttest.data.extensions.toDealDBEntity
import com.example.simbirsofttest.data.models.Deal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

internal class DealRepository @Inject constructor(
    private val dealDao: DealDao,
    @Dispatcher(SSDispatchers.IO)
    private val dispatcher: CoroutineDispatcher,
    ) : IDealRepository {

    override suspend fun insert(deal: Deal): Long = withContext(dispatcher) {
        dealDao.insert(deal.toDealDBEntity())
    }

    override suspend fun update(deal: Deal) = withContext(dispatcher) {
        dealDao.update(deal.toDealDBEntity())
    }

    override suspend fun getFlow(id: Long): Flow<Deal?> = withContext(dispatcher) {
        dealDao.getFlow(id).map { it?.toDeal() }
    }

    override suspend fun getDeal(id: Long): Deal?  = withContext(dispatcher) {
        dealDao.getDeal(id).let { it?.toDeal() }
    }

    override fun getByDay(date: LocalDate): Flow<List<Deal>> {
        val startOfDay = date.toDate().time
        return dealDao.getByDay(startOfDay)
            .map { it.map(DealDBEntity::toDeal) }
    }
}