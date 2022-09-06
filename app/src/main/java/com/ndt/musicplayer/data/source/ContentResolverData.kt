package com.ndt.musicplayer.data.source

import android.content.Context
import android.provider.MediaStore
import com.ndt.musicplayer.data.model.Song

class ContentResolverData(private val context: Context) {

    fun getData(): ArrayList<Song> {
        val musics: ArrayList<Song> = ArrayList()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, "LOWER(" + MediaStore.Audio.Media.TITLE
                    + ") ASC"
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                musics.add(Song(cursor))
            }
        }
        cursor?.close()

        return musics
    }
}
