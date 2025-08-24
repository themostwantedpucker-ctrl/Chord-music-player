package com.chord.musicplayer.di

import android.content.Context
import com.chord.musicplayer.data.database.ChordDatabase
import com.chord.musicplayer.data.database.dao.FavoriteDao
import com.chord.musicplayer.data.database.dao.PlaylistDao
import com.chord.musicplayer.data.datastore.PlaybackStateDataStore
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
    fun provideChordDatabase(@ApplicationContext context: Context): ChordDatabase {
        return ChordDatabase.getDatabase(context)
    }

    @Provides
    fun providePlaylistDao(database: ChordDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideFavoriteDao(database: ChordDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun providePlaybackStateDataStore(@ApplicationContext context: Context): PlaybackStateDataStore {
        return PlaybackStateDataStore(context)
    }
}
