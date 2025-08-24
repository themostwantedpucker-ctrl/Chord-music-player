package com.chord.musicplayer.playback

import android.app.PendingIntent
import android.content.Intent
import com.chord.musicplayer.MainActivity
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.chord.musicplayer.R
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var audioFocusManager: AudioFocusManager
    private var mediaSession: MediaSession? = null
    private var notificationManager: PlayerNotificationManager? = null

    private val notificationId = 1001
    private val imageLoader by lazy { ImageLoader.Builder(this).build() }

    override fun onCreate() {
        super.onCreate()

        // Create an intent that launches the app when tapping the notification
        val sessionActivityPendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer.playWhenReady = false

        // Set up audio focus
        audioFocusManager.setPlayer(exoPlayer)
        
        // Add listener to request audio focus when playback starts
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    audioFocusManager.requestAudioFocus()
                } else {
                    audioFocusManager.abandonAudioFocus()
                }
            }
        })

        NotificationHelper.ensureChannel(this)
        val session = mediaSession ?: return
        notificationManager = PlayerNotificationManager.Builder(this, notificationId, NotificationHelper.CHANNEL_ID)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.currentMediaItem?.mediaMetadata?.title ?: getString(R.string.app_name)
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return session.sessionActivity
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.currentMediaItem?.mediaMetadata?.artist
                }

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): android.graphics.Bitmap? {
                    val artworkUri = player.currentMediaItem?.mediaMetadata?.artworkUri
                    if (artworkUri != null) {
                        // Load asynchronously with listener
                        val request = ImageRequest.Builder(this@PlaybackService)
                            .data(artworkUri)
                            .allowHardware(false)
                            .listener(onSuccess = { _, result ->
                                callback.onBitmap(result.drawable.toBitmap())
                            })
                            .build()
                        imageLoader.enqueue(request)
                    }
                    return null
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: android.app.Notification, ongoing: Boolean) {
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopForeground(true)
                    stopSelf()
                }
            })
            .build().apply {
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUseFastForwardAction(false)
                setUseRewindAction(false)
                setSmallIcon(R.drawable.ic_album)
                setMediaSessionToken(session.sessionCompatToken)
                setPlayer(exoPlayer)
            }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        notificationManager = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
