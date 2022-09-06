package com.ndt.musicplayer.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.ndt.musicplayer.R
import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.ui.MusicActivity
import com.ndt.musicplayer.utils.Constant


class MyMusicService : Service() {
    private var serviceStatus: Int = 0
    private var position: Int = 0
    private val ibinder = PlayMusicBinder()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var music: Song
    private var songList: MutableList<Song>? = null
    private lateinit var musicArrayList: ArrayList<Song>
    private lateinit var callBack: CallBack

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constant.ACTION_PLAY -> {
                if (mediaPlayer.isPlaying) pauseSong() else playSong()
                callBack.onUpTime(music, mediaPlayer.currentPosition)
            }
            Constant.ACTION_NEXT -> nextSong()
            Constant.ACTION_PREV -> previousSong()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return ibinder
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        super.onDestroy()
    }

    fun isPlaying(): Int = serviceStatus

    fun onProgress(i: Int) {
        mediaPlayer.seekTo(i * music.duration / 100)
    }

    fun setSongList(songList: MutableList<Song>) {
        this.songList = songList
    }

    fun chooseMusic(context: Context, musics: ArrayList<Song>, position: Int) {
        this.musicArrayList = musics
        this.position = position
        mediaPlayer.run {
            reset()
            setDataSource(context, Uri.parse(musics[position].data))
            prepare()
        }
        music = musics[position]

        if (serviceStatus == 0) mediaPlayer.start()
        updateMusicTime(music, 0, this.position)
    }

    fun nextSong() {
        position = if (position < musicArrayList.size - 1) position + 1 else 0
        mediaPlayer.run {
            reset()
            setDataSource(applicationContext, Uri.parse(musicArrayList[position].data))
            prepare()
        }

        music = musicArrayList[position]
        if (serviceStatus == 0) mediaPlayer.start()
        updateMusicTime(music, 0, position)
    }

    fun previousSong() {
        position = if (position <= 0) musicArrayList.size - 1 else position - 1
        mediaPlayer.run {
            reset()
            setDataSource(applicationContext, Uri.parse(musicArrayList[position].data))
            prepare()
        }
        music = musicArrayList[position]
        if (serviceStatus == 0) mediaPlayer.start()
        updateMusicTime(music, 0, position)

    }

    fun playSong() {
        mediaPlayer.start()
        serviceStatus = 0
        updateMusicTime(music, mediaPlayer.currentPosition, position)
        pushNotification(music)
    }

    fun pauseSong() {
        mediaPlayer.pause()
        serviceStatus = 1
        pushNotification(music)
    }

    fun updateMusicTime(music: Song, progress: Int, position: Int) {
        Handler().postDelayed({
            if (progress == 0) pushNotification(music)
            if (progress == 0 && serviceStatus == 1) callBack.onUpTime(
                music,
                mediaPlayer.currentPosition
            )
            if (serviceStatus == 0 && !mediaPlayer.isPlaying) nextSong()
            if (serviceStatus == 0 && position == this.position) {
                callBack.onUpTime(music, mediaPlayer.currentPosition)
                updateMusicTime(music, mediaPlayer.currentPosition, position)
            }
        }, Constant.TIME_DELAY_UPDATE_MUSIC)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun pushNotification(music: Song) {
        val intentFlag = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        val intentNotification = MusicActivity.getIntent(this).apply { intentFlag }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intentNotification,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(Constant.CHANNEL_ID, Constant.CHANNEL_NAME, importance)
            notificationChannel.description = Constant.CHANNEL_DESCRIPTION
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        startForeground(Constant.ID, getNotification(music, pendingIntent, intentFlag))
    }

    private fun getNotification(
        music: Song,
        pendingIntent: PendingIntent,
        flags: Int
    ): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.nofication_music)
        notificationLayout.run {
            setTextViewText(R.id.text_title_play, music.title)
            setTextViewText(R.id.text_artist_play, music.artist)
            setImageViewResource(R.id.image_avatar_play, R.drawable.icon_music_player)
            if (mediaPlayer.isPlaying) {
                setImageViewResource(R.id.image_play, R.drawable.ic_pause)
            } else {
                setImageViewResource(R.id.image_play, R.drawable.ic_play)
            }
        }

        val playIntentNotification = Intent(this, MyMusicService::class.java).apply { flags }
        playIntentNotification.action = Constant.ACTION_PLAY
        val playPendingIntent = PendingIntent.getService(this, 0, playIntentNotification, 0)

        val nextIntentNotification = Intent(this, MyMusicService::class.java).apply { flags }
        nextIntentNotification.action = Constant.ACTION_NEXT
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntentNotification, 0)

        val preIntentNotification = Intent(this, MyMusicService::class.java).apply { flags }
        preIntentNotification.action = Constant.ACTION_PREV
        val prePendingIntent = PendingIntent.getService(this, 0, preIntentNotification, 0)

        notificationLayout.run {
            setOnClickPendingIntent(R.id.image_play, playPendingIntent)
            setOnClickPendingIntent(R.id.image_play_next, nextPendingIntent)
            setOnClickPendingIntent(R.id.image_play_back, prePendingIntent)
        }
        return NotificationCompat.Builder(this, Constant.CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_music_player)
            .setCustomBigContentView(notificationLayout)
            .setContentIntent(pendingIntent)
            .setTicker(music.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    inner class PlayMusicBinder : Binder() {
        fun service(): MyMusicService = this@MyMusicService
    }

    interface CallBack {
        fun onUpTime(music: Song, progress: Int)
    }

    fun callBackService(callBack: CallBack) {
        this.callBack = callBack
    }
}
