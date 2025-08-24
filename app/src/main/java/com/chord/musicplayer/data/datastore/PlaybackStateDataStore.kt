package com.chord.musicplayer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_state")

@Singleton
class PlaybackStateDataStore @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val CURRENT_MEDIA_URI = stringPreferencesKey("current_media_uri")
        val CURRENT_TITLE = stringPreferencesKey("current_title")
        val CURRENT_ARTIST = stringPreferencesKey("current_artist")
        val CURRENT_ARTWORK_URI = stringPreferencesKey("current_artwork_uri")
        val CURRENT_POSITION = longPreferencesKey("current_position")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val QUEUE_URIS = stringPreferencesKey("queue_uris") // JSON array
        val QUEUE_TITLES = stringPreferencesKey("queue_titles") // JSON array
        val QUEUE_ARTISTS = stringPreferencesKey("queue_artists") // JSON array
        val QUEUE_ARTWORK_URIS = stringPreferencesKey("queue_artwork_uris") // JSON array
        val CURRENT_INDEX = intPreferencesKey("current_index")
    }

    val currentMediaUri: Flow<String?> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_MEDIA_URI] }

    val currentTitle: Flow<String?> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_TITLE] }

    val currentArtist: Flow<String?> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_ARTIST] }

    val currentArtworkUri: Flow<String?> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_ARTWORK_URI] }

    val currentPosition: Flow<Long> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_POSITION] ?: 0L }

    val shuffleEnabled: Flow<Boolean> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.SHUFFLE_ENABLED] ?: false }

    val repeatMode: Flow<Int> = context.playbackDataStore.data
        .map { preferences -> preferences[PreferencesKeys.REPEAT_MODE] ?: 0 }

    suspend fun saveCurrentTrack(
        uri: String?,
        title: String?,
        artist: String?,
        artworkUri: String?,
        position: Long
    ) {
        context.playbackDataStore.edit { preferences ->
            if (uri != null) preferences[PreferencesKeys.CURRENT_MEDIA_URI] = uri
            if (title != null) preferences[PreferencesKeys.CURRENT_TITLE] = title
            if (artist != null) preferences[PreferencesKeys.CURRENT_ARTIST] = artist
            if (artworkUri != null) preferences[PreferencesKeys.CURRENT_ARTWORK_URI] = artworkUri
            preferences[PreferencesKeys.CURRENT_POSITION] = position
        }
    }

    suspend fun savePlaybackSettings(shuffleEnabled: Boolean, repeatMode: Int) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUFFLE_ENABLED] = shuffleEnabled
            preferences[PreferencesKeys.REPEAT_MODE] = repeatMode
        }
    }

    suspend fun clearPlaybackState() {
        context.playbackDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
