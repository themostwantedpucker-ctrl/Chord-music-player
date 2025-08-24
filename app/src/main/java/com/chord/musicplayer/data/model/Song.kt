package com.chord.musicplayer.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uri: String,
    val albumArtUri: String?
)
