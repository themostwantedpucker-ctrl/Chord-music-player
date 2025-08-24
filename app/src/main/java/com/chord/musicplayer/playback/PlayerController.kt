package com.chord.musicplayer.playback

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerController @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private var controller: MediaController? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _currentSubtitle = MutableStateFlow("")
    val currentSubtitle: StateFlow<String> = _currentSubtitle.asStateFlow()

    private val _artworkUri = MutableStateFlow<String?>(null)
    val artworkUri: StateFlow<String?> = _artworkUri.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    init {
        connect()
    }

    private fun connect() {
        viewModelScope.launch {
            val token = SessionToken(context, android.content.ComponentName(context, PlaybackService::class.java))
            controller = MediaController.Builder(context, token)
                .buildAsync()
                .await()

            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentTitle.value = mediaItem?.mediaMetadata?.title?.toString().orEmpty()
                    _currentSubtitle.value = mediaItem?.mediaMetadata?.artist?.toString().orEmpty()
                    _artworkUri.value = mediaItem?.mediaMetadata?.artworkUri?.toString()
                    _durationMs.value = controller?.duration ?: 0L
                }
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleEnabled.value = shuffleModeEnabled
                }
                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }
            })

            // Prime initial state
            controller?.let { c ->
                _isPlaying.value = c.isPlaying
                c.currentMediaItem?.let { item ->
                    _currentTitle.value = item.mediaMetadata.title?.toString().orEmpty()
                    _currentSubtitle.value = item.mediaMetadata.artist?.toString().orEmpty()
                    _artworkUri.value = item.mediaMetadata.artworkUri?.toString()
                }
                _durationMs.value = c.duration
                _positionMs.value = c.currentPosition
                _shuffleEnabled.value = c.shuffleModeEnabled
                _repeatMode.value = c.repeatMode
            }
            _isConnected.value = true

            // Periodically update position
            viewModelScope.launch {
                while (true) {
                    controller?.let { c ->
                        _positionMs.value = c.currentPosition
                        _durationMs.value = c.duration
                    }
                    delay(500)
                }
            }
        }
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun ensureDemoAndToggle() {
        val c = controller ?: return
        if (c.currentMediaItem == null) {
            // Short royalty-free sample
            val demoUri = Uri.parse("https://samplelib.com/lib/preview/mp3/sample-3s.mp3")
            playUri(demoUri, title = "Demo Track", artist = "SampleLib")
            return
        }
        playPause()
    }

    fun next() { controller?.seekToNext() }
    fun previous() { controller?.seekToPrevious() }

    fun playUri(uri: Uri, title: String? = null, artist: String? = null, artworkUri: String? = null) {
        val item = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .apply {
                        if (!artworkUri.isNullOrEmpty()) setArtworkUri(Uri.parse(artworkUri))
                    }
                    .build()
            )
            .build()
        controller?.setMediaItem(item)
        controller?.prepare()
        controller?.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        controller?.let { c ->
            val enabled = !c.shuffleModeEnabled
            c.shuffleModeEnabled = enabled
            _shuffleEnabled.value = enabled
        }
    }

    fun cycleRepeatMode() {
        controller?.let { c ->
            val next = when (c.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            c.repeatMode = next
            _repeatMode.value = next
        }
    }

    fun addToQueue(uri: Uri, title: String? = null, artist: String? = null, artworkUri: String? = null) {
        val item = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .apply { if (!artworkUri.isNullOrEmpty()) setArtworkUri(Uri.parse(artworkUri)) }
                    .build()
            )
            .build()
        controller?.addMediaItem(item)
        controller?.prepare()
    }

    fun playNext(uri: Uri, title: String? = null, artist: String? = null, artworkUri: String? = null) {
        val c = controller ?: return
        val insertIndex = (c.currentMediaItemIndex + 1).coerceAtMost(c.mediaItemCount)
        val item = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .apply { if (!artworkUri.isNullOrEmpty()) setArtworkUri(Uri.parse(artworkUri)) }
                    .build()
            )
            .build()
        c.addMediaItem(insertIndex, item)
        c.prepare()
    }

    override fun onCleared() {
        super.onCleared()
        controller?.release()
        controller = null
    }
}
