package com.chord.musicplayer.data.repo

import com.chord.musicplayer.data.database.dao.FavoriteDao
import com.chord.musicplayer.data.database.entities.FavoriteSong
import com.chord.musicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    fun getAllFavorites(): Flow<List<FavoriteSong>> = favoriteDao.getAllFavorites()

    suspend fun isFavorite(songId: String): Boolean = favoriteDao.isFavorite(songId)

    fun isFavoriteFlow(songId: String): Flow<Boolean> = favoriteDao.isFavoriteFlow(songId)

    suspend fun toggleFavorite(song: Song) {
        if (favoriteDao.isFavorite(song.id)) {
            favoriteDao.removeFavorite(song.id)
        } else {
            val favoriteSong = FavoriteSong(
                songId = song.id,
                songUri = song.uri.toString(),
                title = song.title,
                artist = song.artist,
                duration = song.duration,
                artworkUri = song.artworkUri
            )
            favoriteDao.insertFavorite(favoriteSong)
        }
    }

    suspend fun addToFavorites(song: Song) {
        val favoriteSong = FavoriteSong(
            songId = song.id,
            songUri = song.uri.toString(),
            title = song.title,
            artist = song.artist,
            duration = song.duration,
            artworkUri = song.artworkUri
        )
        favoriteDao.insertFavorite(favoriteSong)
    }

    suspend fun removeFromFavorites(songId: String) {
        favoriteDao.removeFavorite(songId)
    }
}
