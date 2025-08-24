package com.chord.musicplayer.ui.screens.library

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chord.musicplayer.data.model.Song
import com.chord.musicplayer.data.repo.MediaStoreRepository
import com.chord.musicplayer.data.repo.PlaylistRepository
import com.chord.musicplayer.data.repo.FavoriteRepository
import com.chord.musicplayer.data.database.entities.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository,
    private val playlistRepository: PlaylistRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val playlists = playlistRepository.getAllPlaylists()

    fun loadAllSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allSongs = mediaStoreRepository.getAllSongs()
                _songs.value = allSongs
            } catch (e: Exception) {
                // Handle error - could emit to an error state
                _songs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun createPlaylistAndAddSong(playlistName: String, song: Song): Long {
        val playlistId = playlistRepository.createPlaylist(playlistName)
        playlistRepository.addSongToPlaylist(playlistId, song)
        return playlistId
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        playlistRepository.addSongToPlaylist(playlistId, song)
    }

    suspend fun toggleFavorite(song: Song) {
        favoriteRepository.toggleFavorite(song)
    }

    fun isFavoriteFlow(songId: String) = favoriteRepository.isFavoriteFlow(songId)

    fun requiredPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}
