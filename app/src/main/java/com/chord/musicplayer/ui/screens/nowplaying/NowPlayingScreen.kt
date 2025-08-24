package com.chord.musicplayer.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chord.musicplayer.playback.PlayerController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    playerController: PlayerController = hiltViewModel()
) {
    val title by playerController.currentTitle.collectAsState()
    val artist by playerController.currentSubtitle.collectAsState()
    val artworkUri by playerController.artworkUri.collectAsState()
    val isPlaying by playerController.isPlaying.collectAsState()
    val duration by playerController.durationMs.collectAsState()
    val position by playerController.positionMs.collectAsState()
    val shuffle by playerController.shuffleEnabled.collectAsState()
    val repeatMode by playerController.repeatMode.collectAsState()

    var userPos by remember(position) { mutableStateOf(position.toFloat()) }
    var dragging by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title / Artist
            Text(
                text = if (title.isNotBlank()) title else "Unknown Title",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (artist.isNotBlank()) artist else "Unknown Artist",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            // Seek bar
            val durF = duration.coerceAtLeast(1L).toFloat()
            Slider(
                value = (if (dragging) userPos else position.toFloat()).coerceIn(0f, durF),
                onValueChange = {
                    userPos = it
                    dragging = true
                },
                onValueChangeFinished = {
                    dragging = false
                    playerController.seekTo(userPos.toLong())
                },
                valueRange = 0f..durF,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(if (dragging) userPos.toLong() else position))
                Text(formatTime(duration))
            }

            Spacer(Modifier.height(8.dp))

            // Controls with Shuffle / Repeat
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Shuffle
                IconButton(onClick = { playerController.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffle) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }

                // Previous
                IconButton(onClick = { playerController.previous() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }

                // Play/Pause
                FilledIconButton(onClick = { playerController.playPause() }) {
                    if (isPlaying) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }

                // Next
                IconButton(onClick = { playerController.next() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }

                // Repeat
                IconButton(onClick = { playerController.cycleRepeatMode() }) {
                    val isActive = repeatMode != Player.REPEAT_MODE_OFF
                    val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    Icon(
                        icon,
                        contentDescription = "Repeat",
                        tint = if (isActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    }
}

@Composable
private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
