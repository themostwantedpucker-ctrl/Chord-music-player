package com.chord.musicplayer.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chord.musicplayer.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.chord.musicplayer.playback.PlayerController
import com.chord.musicplayer.ui.screens.home.HomeScreen
import com.chord.musicplayer.ui.screens.library.LibraryScreen
import com.chord.musicplayer.ui.screens.nowplaying.NowPlayingScreen
import com.chord.musicplayer.ui.screens.search.SearchScreen
import com.chord.musicplayer.ui.screens.settings.SettingsScreen
import com.chord.musicplayer.ui.theme.ChordTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerController: PlayerController = hiltViewModel()
    val isPlaying by playerController.isPlaying.collectAsState()
    val currentTitle by playerController.currentTitle.collectAsState()
    val currentSubtitle by playerController.currentSubtitle.collectAsState()
    val currentArtworkUri by playerController.artworkUri.collectAsState()
    val durationMs by playerController.durationMs.collectAsState()
    val positionMs by playerController.positionMs.collectAsState()
    val progress = remember(durationMs, positionMs) { if (durationMs > 0) positionMs.toFloat() / durationMs else 0f }
    
    // Only show bottom bar for main screens
    val showBottomBar = listOf(
        Screens.Home.route,
        Screens.Search.route,
        Screens.Library.route,
        Screens.Settings.route
    ).any { it == currentRoute }
    
    // Bottom navigation items
    val items = listOf(
        BottomNavItem(
            route = Screens.Home.route,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            contentDescription = "Home"
        ),
        BottomNavItem(
            route = Screens.Search.route,
            label = "Search",
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            contentDescription = "Search"
        ),
        BottomNavItem(
            route = Screens.Library.route,
            label = "Library",
            selectedIcon = Icons.Filled.LibraryMusic,
            unselectedIcon = Icons.Outlined.LibraryMusic,
            contentDescription = "Library"
        )
    )
    
    ChordTheme {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 8.dp
                    ) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.contentDescription,
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                // Main Navigation Graph
                NavHost(
                    navController = navController,
                    startDestination = Screens.Home.route
                ) {
                    // Home Screen
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
                            },
                            isPlaying = isPlaying,
                            currentTitle = currentTitle,
                            currentSubtitle = currentSubtitle,
                            currentArtworkUri = currentArtworkUri,
                            progress = progress,
                            onPlayPauseClick = { playerController.ensureDemoAndToggle() },
                            onNextClick = { playerController.next() },
                            onPreviousClick = { playerController.previous() }
                        )
                    }
                    
                    // Search Screen
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
                    
                    // Library Screen
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
                    
                    // Settings Screen
                    composable(Screens.Settings.route) {
                        SettingsScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    // Now Playing Screen
                    composable(Screens.NowPlaying.route) {
                        NowPlayingScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                    
                    // Playlist Detail Screen
                    composable("${Screens.Playlist.route}/{playlistId}") { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: return@composable
                        com.chord.musicplayer.ui.screens.playlist.PlaylistDetailScreen(
                            playlistId = playlistId,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
)
