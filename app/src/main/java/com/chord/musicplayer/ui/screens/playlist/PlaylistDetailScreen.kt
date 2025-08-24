package com.chord.musicplayer.ui.screens.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chord.musicplayer.data.repo.PlaylistRepository
import com.chord.musicplayer.playback.PlayerController
import com.chord.musicplayer.ui.components.SongItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    playlistRepository: PlaylistRepository = hiltViewModel(),
    playerController: PlayerController = hiltViewModel()
) {
    val playlistSongs by playlistRepository.getPlaylistSongs(playlistId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (playlistSongs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No songs in this playlist",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(playlistSongs) { playlistSong ->
                    PlaylistSongItem(
                        playlistSong = playlistSong,
                        onClick = {
                            playerController.playUri(
                                uri = android.net.Uri.parse(playlistSong.songUri),
                                title = playlistSong.title,
                                artist = playlistSong.artist,
                                artworkUri = playlistSong.artworkUri
                            )
                        },
                        onRemove = {
                            scope.launch {
                                playlistRepository.removeSongFromPlaylist(playlistId, playlistSong.songId)
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PlaylistSongItem(
    playlistSong: com.chord.musicplayer.data.database.entities.PlaylistSong,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song info (reuse similar layout as SongItem but simpler)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clickable { onClick() }
        ) {
            Text(
                text = playlistSong.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = playlistSong.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Remove button
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove from playlist",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
