package com.chord.musicplayer.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.chord.musicplayer.data.database.dao.FavoriteDao
import com.chord.musicplayer.data.database.dao.PlaylistDao
import com.chord.musicplayer.data.database.entities.FavoriteSong
import com.chord.musicplayer.data.database.entities.Playlist
import com.chord.musicplayer.data.database.entities.PlaylistSong

@Database(
    entities = [Playlist::class, PlaylistSong::class, FavoriteSong::class],
    version = 1,
    exportSchema = false
)
abstract class ChordDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: ChordDatabase? = null

        fun getDatabase(context: Context): ChordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChordDatabase::class.java,
                    "chord_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
