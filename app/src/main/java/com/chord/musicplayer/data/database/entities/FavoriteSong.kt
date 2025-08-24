package com.chord.musicplayer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey
    val songId: String, // MediaStore ID
    val songUri: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val artworkUri: String?,
    val addedAt: Long = System.currentTimeMillis()
)
