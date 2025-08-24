package com.chord.musicplayer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedMiniPlayer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onExpandClick: () -> Unit = {},
    title: String? = null,
    subtitle: String? = null,
    artworkUri: String? = null,
    progress: Float = 0f
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    AnimatedVisibility(
        visible = !title.isNullOrBlank() || !subtitle.isNullOrBlank(),
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationY = dragOffset
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragOffset < -100f) {
                                onExpandClick()
                            }
                            dragOffset = 0f
                        }
                    ) { _, dragAmount ->
                        dragOffset = (dragOffset + dragAmount.y).coerceAtMost(0f)
                    }
                }
        ) {
            MiniPlayer(
                modifier = Modifier.fillMaxWidth(),
                isPlaying = isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onPreviousClick = onPreviousClick,
                onExpandClick = onExpandClick,
                title = title,
                subtitle = subtitle,
                artworkUri = artworkUri,
                progress = animatedProgress
            )
        }
    }
}
