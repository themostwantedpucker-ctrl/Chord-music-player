package com.chord.musicplayer.ui.screens.album

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AlbumScreen(
    albumId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToArtist: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Album #$albumId", style = MaterialTheme.typography.headlineSmall)
    }
}
