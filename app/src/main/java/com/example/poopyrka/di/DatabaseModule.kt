package com.example.poopyrka.di

import android.content.Context
import com.example.poopyrka.data.AppDao
import com.example.poopyrka.data.AppDatabase
import com.example.poopyrka.data.EarningsCalculator
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideDao(database: AppDatabase): AppDao {
        return database.dao()
    }

    @Provides
    @Singleton
    fun provideEarningsCalculator(): EarningsCalculator {
        return EarningsCalculator
    }
}
