package com.chord.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chord.musicplayer.ui.screens.album.AlbumScreen
import com.chord.musicplayer.ui.screens.artist.ArtistScreen
import com.chord.musicplayer.ui.screens.home.HomeScreen
import com.chord.musicplayer.ui.screens.library.LibraryScreen
import com.chord.musicplayer.ui.screens.nowplaying.NowPlayingScreen
import com.chord.musicplayer.ui.screens.playlist.PlaylistScreen
import com.chord.musicplayer.ui.screens.search.SearchScreen
import com.chord.musicplayer.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screens.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Main Screens
        composable(Screens.Home.route) {
            HomeScreen(
                onNavigateToAlbum = { albumId ->
                    navController.navigate("${Screens.Album.route}/$albumId")
                },
                onNavigateToArtist = { artistId ->
                    navController.navigate("${Screens.Artist.route}/$artistId")
                },
                onNavigateToNowPlaying = {
                    navController.navigate(Screens.NowPlaying.route)
                }
            )
        }
        
        composable(Screens.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAlbum = { albumId ->
                    navController.navigate("${Screens.Album.route}/$albumId")
                },
                onNavigateToArtist = { artistId ->
                    navController.navigate("${Screens.Artist.route}/$artistId")
                }
            )
        }
        
        composable(Screens.Library.route) {
            LibraryScreen(
                onNavigateToPlaylist = { playlistId ->
                    navController.navigate("${Screens.Playlist.route}/$playlistId")
                },
                onNavigateToAlbum = { albumId ->
                    navController.navigate("${Screens.Album.route}/$albumId")
                },
                onNavigateToArtist = { artistId ->
                    navController.navigate("${Screens.Artist.route}/$artistId")
                }
            )
        }
        
        composable(Screens.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        // Detail Screens
        composable(
            route = "${Screens.Album.route}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
            AlbumScreen(
                albumId = albumId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToArtist = { artistId ->
                    navController.navigate("${Screens.Artist.route}/$artistId")
                }
            )
        }
        
        composable(
            route = "${Screens.Artist.route}/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getLong("artistId") ?: return@composable
            ArtistScreen(
                artistId = artistId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAlbum = { albumId ->
                    navController.navigate("${Screens.Album.route}/$albumId")
                }
            )
        }
        
        composable(
            route = "${Screens.Playlist.route}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            PlaylistScreen(
                playlistId = playlistId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAlbum = { albumId ->
                    navController.navigate("${Screens.Album.route}/$albumId")
                },
                onNavigateToArtist = { artistId ->
                    navController.navigate("${Screens.Artist.route}/$artistId")
                }
            )
        }
        
        // Modal Screens
        composable(Screens.NowPlaying.route) {
            NowPlayingScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
