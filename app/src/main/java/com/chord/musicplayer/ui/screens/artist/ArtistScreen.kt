package com.chord.musicplayer.ui.screens.artist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ArtistScreen(
    artistId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Artist #$artistId", style = MaterialTheme.typography.headlineSmall)
    }
}
