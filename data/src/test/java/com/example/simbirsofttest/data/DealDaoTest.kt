package com.example.simbirsofttest.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.simbirsofttest.core.utils.toDate
import com.example.simbirsofttest.data.daos.DealDao
import com.example.simbirsofttest.data.utils.getTestDeal
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DealDaoTest {
    private lateinit var userDao: DealDao
    private lateinit var db: SSDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                SSDatabase::class.java
            ).allowMainThreadQueries()
            .build()
        userDao = db.dealDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetDeal() = runBlocking {
        var deal = getTestDeal()

        val id = userDao.insert(deal)
        val retrieved = userDao.getDeal(id)
        deal = deal.copy(id = id)

        assertNotNull(retrieved)
        assertEquals(deal, retrieved)
    }

    @Test
    fun updateDeal() = runBlocking {
        val updatedName = "Updated Deal"
        val deal = getTestDeal()

        val id = userDao.insert(deal)
        val updatedDeal = deal.copy(id = id, name = updatedName)
        userDao.update(updatedDeal)
        val retrieved = userDao.getDeal(id)

        assertEquals(updatedName, retrieved?.name)
    }

    @Test
    fun `deal started at 0 hour does not belong to the previous day`() = runBlocking {
        val currentDay = LocalDate.of(2000,12,21)
        val nextDay = LocalDate.of(2000,12,22)
        val dayStartTime = LocalTime.of(0,0)
        val currentDayStartDayTime = LocalDateTime.of(currentDay, dayStartTime)
        val nextDayStartDayTime = LocalDateTime.of(nextDay, dayStartTime)
        val dealDateEnd = nextDayStartDayTime.plusHours(4)
        val deal = getTestDeal().copy(
            dateStart = nextDayStartDayTime.toDate(),
            dateEnd = dealDateEnd.toDate(),
        )

        userDao.insert(deal)
        val currentDayDeal = userDao.getByDay(currentDayStartDayTime.toDate().time).first()
        val nextDayDeal = userDao.getByDay(nextDayStartDayTime.toDate().time).first()

        assertEquals(currentDayDeal.size, 0)
        assertEquals(nextDayDeal.size, 1)
    }
}