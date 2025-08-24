package com.chord.musicplayer.data.repo

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.chord.musicplayer.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun loadAllSongs(minDurationMs: Long = 30_000): List<Song> {
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE
        )

        // Enhanced filtering: music files only, minimum duration, exclude voice recordings
        val selection = buildString {
            append("${MediaStore.Audio.Media.IS_MUSIC} != 0")
            append(" AND ${MediaStore.Audio.Media.DURATION} >= ?")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/WhatsApp/%'")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Recordings/%'") 
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Voice Recorder/%'")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Call Recordings/%'")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Telegram/%'")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Notifications/%'")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Ringtones/%'")
            append(" AND ${MediaStore.Audio.Media.DATA} NOT LIKE '%/Alarms/%'")
            append(" AND (${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/mpeg'")
            append(" OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/mp4'")
            append(" OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/x-ms-wma'")
            append(" OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/flac'")
            append(" OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/ogg')")
        }
        
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC"

        val result = mutableListOf<Song>()
        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Unknown Title"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val duration = cursor.getLong(durationCol)
                val albumId = cursor.getLong(albumIdCol)
                val filePath = cursor.getString(dataCol) ?: ""

                // Additional filtering for voice notes and recordings
                if (isVoiceRecording(filePath, title, duration)) {
                    continue
                }

                val contentUri = ContentUris.withAppendedId(collection, id)
                val albumArtUri = if (albumId > 0) {
                    Uri.parse("content://media/external/audio/albumart/$albumId").toString()
                } else null

                result.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        durationMs = duration,
                        uri = contentUri.toString(),
                        albumArtUri = albumArtUri
                    )
                )
            }
        }
        return result
    }

    private fun isVoiceRecording(filePath: String, title: String, durationMs: Long): Boolean {
        val path = filePath.lowercase()
        val titleLower = title.lowercase()
        
        // Common voice recording indicators
        val voiceIndicators = listOf(
            "voice", "recording", "memo", "note", "whatsapp", "telegram",
            "ptt-", "aud-", "rec_", "voice_", "audio_", "wha_"
        )
        
        // Check file path and title for voice recording patterns
        val hasVoiceIndicator = voiceIndicators.any { indicator ->
            path.contains(indicator) || titleLower.contains(indicator)
        }
        
        // Very short files are likely voice notes (less than 30 seconds)
        val isTooShort = durationMs < 30_000
        
        // Files with generic numeric names are often voice recordings
        val hasGenericName = titleLower.matches(Regex("^(aud|rec|ptt|wha)[-_]?\\d+.*"))
        
        return hasVoiceIndicator || (isTooShort && hasGenericName)
    }
}
