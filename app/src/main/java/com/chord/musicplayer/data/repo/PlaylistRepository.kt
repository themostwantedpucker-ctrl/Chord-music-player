package com.chord.musicplayer.data.repo

import com.chord.musicplayer.data.database.dao.PlaylistDao
import com.chord.musicplayer.data.database.entities.Playlist
import com.chord.musicplayer.data.database.entities.PlaylistSong
import com.chord.musicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSong>> = 
        playlistDao.getPlaylistSongs(playlistId)

    suspend fun createPlaylist(name: String): Long {
        val playlist = Playlist(name = name)
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        val playlistSongs = playlistDao.getPlaylistSongs(playlistId)
        // Get next position (this is a simplified approach)
        val nextPosition = 0 // Could be improved to get actual count
        
        val playlistSong = PlaylistSong(
            playlistId = playlistId,
            songId = song.id,
            songUri = song.uri.toString(),
            title = song.title,
            artist = song.artist,
            duration = song.duration,
            artworkUri = song.artworkUri,
            position = nextPosition
        )
        playlistDao.insertPlaylistSong(playlistSong)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val playlist = Playlist(id = playlistId, name = newName, updatedAt = System.currentTimeMillis())
        playlistDao.updatePlaylist(playlist)
    }
}
