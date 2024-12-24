package com.example.simbirsofttest.data.diModules

import android.content.Context
import androidx.room.Room
import com.example.simbirsofttest.data.SSDatabase
import com.example.simbirsofttest.data.daos.DealDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    internal fun provideDatabase(@ApplicationContext context: Context): SSDatabase {
        return Room.databaseBuilder(
            context,
            SSDatabase::class.java,
            "simbirsoft_database"
        ).build()
    }

    @Provides
    internal fun provideDealDao(database: SSDatabase): DealDao {
        return database.dealDao()
    }
}