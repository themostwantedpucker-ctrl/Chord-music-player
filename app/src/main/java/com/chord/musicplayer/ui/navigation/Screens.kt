package com.chord.musicplayer.ui.navigation

sealed class Screens(val route: String) {
    // Main Tabs
    object Home : Screens("home")
    object Search : Screens("search")
    object Library : Screens("library")
    object Settings : Screens("settings")
    
    // Detail Screens
    object Album : Screens("album") {
        fun createRoute(albumId: Long) = "$route/$albumId"
    }
    
    object Artist : Screens("artist") {
        fun createRoute(artistId: Long) = "$route/$artistId"
    }
    
    object Playlist : Screens("playlist") {
        fun createRoute(playlistId: Long) = "$route/$playlistId"
    }
    
    // Modal Screens
    object NowPlaying : Screens("now_playing") 
}
