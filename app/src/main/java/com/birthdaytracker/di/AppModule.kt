package com.birthdaytracker.di

import android.content.Context
import com.birthdaytracker.data.BirthdayDao
import com.birthdaytracker.data.BirthdayDatabase
import com.birthdaytracker.repository.BirthdayRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBirthdayDatabase(@ApplicationContext context: Context): BirthdayDatabase {
        return BirthdayDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideBirthdayDao(database: BirthdayDatabase): BirthdayDao {
        return database.birthdayDao()
    }

    @Provides
    @Singleton
    fun provideBirthdayRepository(birthdayDao: BirthdayDao): BirthdayRepository {
        return BirthdayRepository(birthdayDao)
    }
}