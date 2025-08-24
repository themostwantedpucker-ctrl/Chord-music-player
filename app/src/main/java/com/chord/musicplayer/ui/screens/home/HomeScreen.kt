package com.chord.musicplayer.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chord.musicplayer.R
import com.chord.musicplayer.ui.components.*
import com.chord.musicplayer.ui.theme.SpotifyGreen

@Composable
fun HomeScreen(
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    isPlaying: Boolean,
    currentTitle: String?,
    currentSubtitle: String?,
    currentArtworkUri: String?,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit
) {
    // TODO: Replace with ViewModel and state hoisting
    val recentlyPlayed = remember { SampleData.getRecentlyPlayed() }
    val recommendedPlaylists = remember { SampleData.getRecommendedPlaylists() }
    val newReleases = remember { SampleData.getNewReleases() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
        // Welcome Header
        item {
            Text(
                text = "Good afternoon",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        // Recently Played Section
        item {
            SectionHeader(
                title = "Recently played",
                onSeeAllClick = { /* TODO: Navigate to recently played */ }
            )
        }
        
        items(recentlyPlayed.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    RecentlyPlayedItem(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            // TODO: Handle play item
                            onNavigateToNowPlaying()
                        }
                    )
                }
                
                // Add empty item if the last row has only one item
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Recommended Playlists Section
        item {
            SectionHeader(
                title = "Made for you",
                onSeeAllClick = { /* TODO: Navigate to all playlists */ }
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(recommendedPlaylists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { /* TODO: Navigate to playlist */ }
                    )
                }
            }
        }
        
        // New Releases Section
        item {
            SectionHeader(
                title = "New releases",
                onSeeAllClick = { /* TODO: Navigate to all new releases */ }
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(newReleases) { album ->
                    AlbumCard(
                        album = album,
                        onClick = { onNavigateToAlbum(album.id) },
                        onArtistClick = { onNavigateToArtist(album.artistId) }
                    )
                }
            }
        }
        
        // Add some bottom padding for the mini player
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Mini Player
        MiniPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onExpandClick = onNavigateToNowPlaying,
            isPlaying = isPlaying,
            title = currentTitle,
            subtitle = currentSubtitle,
            artworkUri = currentArtworkUri,
            progress = progress
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        TextButton(
            onClick = onSeeAllClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("See all")
        }
    }
}

// Sample data for preview
object SampleData {
    data class MediaItem(
        val id: Long,
        val title: String,
        val subtitle: String,
        val imageUrl: String,
        val artistId: Long = 0
    )
    
    data class Playlist(
        val id: Long,
        val name: String,
        val description: String,
        val imageUrl: String
    )
    
    data class Album(
        val id: Long,
        val title: String,
        val artist: String,
        val artistId: Long,
        val imageUrl: String,
        val year: String
    )
    
    fun getRecentlyPlayed(): List<MediaItem> = List(6) { index ->
        MediaItem(
            id = index.toLong(),
            title = "Song Title ${index + 1}",
            subtitle = "Artist ${index + 1}",
            imageUrl = "https://picsum.photos/200/200?random=$index",
            artistId = index.toLong()
        )
    }
    
    fun getRecommendedPlaylists(): List<Playlist> = List(5) { index ->
        Playlist(
            id = index.toLong(),
            name = "Playlist ${index + 1}",
            description = "${(index + 5) * 2} songs",
            imageUrl = "https://picsum.photos/200/200?playlist=$index"
        )
    }
    
    fun getNewReleases(): List<Album> = List(10) { index ->
        Album(
            id = index.toLong(),
            title = "Album ${index + 1}",
            artist = "Artist ${index + 1}",
            artistId = index.toLong(),
            imageUrl = "https://picsum.photos/200/200?album=$index",
            year = "2023"
        )
    }
}
