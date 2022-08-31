package com.ndt.musicplayer.data.model

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

data class Song(
    var id: Int,
    var artist: String,
    var title: String,
    var data: String,
    var displayName: String,
    var duration: Int,
    var idAlbum: Long,
    var uri: Uri
) {
    @SuppressLint("Range")
    constructor(cursor: Cursor) : this(
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)).toInt(),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)).toInt(),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toLong(),
        ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toLong()
        )
    )
}