package com.chord.musicplayer.ui.screens.library

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chord.musicplayer.R
import com.chord.musicplayer.data.model.Song
import com.chord.musicplayer.data.repo.PlaylistRepository
import com.chord.musicplayer.data.repo.FavoriteRepository
import com.chord.musicplayer.playback.PlayerController
import com.chord.musicplayer.ui.components.PlaylistBottomSheet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    playerController: PlayerController = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())
    val permission = viewModel.requiredPermission()
    val permissionState = rememberPermissionState(permission)
    val scope = rememberCoroutineScope()
    
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showPlaylistSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) {
            viewModel.loadAllSongs()
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadAllSongs()
        }
    }

    when {
        !permissionState.status.isGranted -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Storage permission is required to access your music library.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
        isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Scanning your songs...", style = MaterialTheme.typography.bodyMedium)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(songs) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            playerController.playUri(
                                uri = song.uri,
                                title = song.title,
                                artist = song.artist,
                                artworkUri = song.artworkUri
                            )
                        },
                        onMoreClick = {
                            selectedSong = song
                            showPlaylistSheet = true
                        },
                        viewModel = viewModel
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Playlist bottom sheet
    if (showPlaylistSheet && selectedSong != null) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistSheet = false }
        ) {
            PlaylistBottomSheet(
                song = selectedSong!!,
                playlists = playlists,
                onCreatePlaylist = { name ->
                    scope.launch {
                        viewModel.createPlaylistAndAddSong(name, selectedSong!!)
                    }
                },
                onAddToPlaylist = { playlistId ->
                    scope.launch {
                        viewModel.addSongToPlaylist(playlistId, selectedSong!!)
                    }
                },
                onDismiss = { showPlaylistSheet = false }
            )
        }
    }
}
