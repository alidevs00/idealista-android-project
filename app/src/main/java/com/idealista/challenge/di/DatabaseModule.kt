package com.idealista.challenge.di

import android.content.Context
import androidx.room.Room
import com.idealista.challenge.data.local.AppDatabase
import com.idealista.challenge.data.local.dao.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "idealista_challenge.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            // No migrations are written yet (schema has never changed). Falling
            // back to destroying and recreating the DB on a future version bump
            // is the right default for this app - the only data at stake is
            // locally-favorited property codes, not anything irreplaceable.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()
}
