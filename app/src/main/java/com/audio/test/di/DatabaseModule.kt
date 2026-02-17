package com.audio.test.di

import android.content.Context
import androidx.room.Room
import com.audio.test.data.local.AppDatabase
import com.audio.test.data.local.SessionDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "audio_practice_db"
        ).build()
    }

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }
}
