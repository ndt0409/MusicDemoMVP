package com.ndt.musicplayer.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.ndt.musicplayer.R
import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.data.repository.MusicRepository
import com.ndt.musicplayer.data.source.ContentResolverData
import com.ndt.musicplayer.data.source.MusicLocalDatasource
import com.ndt.musicplayer.databinding.ActivityMusicBinding
import com.ndt.musicplayer.service.MyMusicService
import com.ndt.musicplayer.ui.adapter.MusicAdapter
import com.ndt.musicplayer.utils.Constant
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.contains as contains

class MusicActivity : AppCompatActivity(), MusicContract.View, MyMusicService.CallBack,
    View.OnClickListener, MusicAdapter.OnItemClickListener {

    private lateinit var musicRepository: MusicRepository
    private lateinit var musicLocalDataSource: MusicLocalDatasource
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var simpleDateFormat: SimpleDateFormat
    private lateinit var myMusicService: MyMusicService
    private lateinit var binding: ActivityMusicBinding
    private var songs = mutableListOf<Song>()


    private val musicPresenter: MusicContract.Presenter by lazy {
        MusicPresenter(musicRepository, this)
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MyMusicService.PlayMusicBinder
            myMusicService = binder.service()
            myMusicService.callBackService(this@MusicActivity)
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initEvent()

        binding.svMusic.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val songsFilter = mutableListOf<Song>()
                songs.forEach {
                    if (it.title.contains(newText.toString(), ignoreCase = true)) {
                        songsFilter.add(it)
                    }
                }
                musicAdapter.setSongList(songsFilter)
                return false
            }
        })
    }

    private fun initData() {
        val contentProvider = ContentResolverData(this)
        musicLocalDataSource = MusicLocalDatasource(contentProvider)
        musicRepository = MusicRepository(musicLocalDataSource)
        simpleDateFormat = SimpleDateFormat("mm:ss", Locale.US)
        val intent = Intent(this, MyMusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        musicAdapter = MusicAdapter(this)
        binding.recyclerMusic.run {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = musicAdapter
        }
    }

    private fun initEvent() {
        binding.apply {
            listOf(imagePlay, imagePlayNext, imagePlayBack).forEach {
                it.setOnClickListener(this@MusicActivity)
            }
        }
        binding.seekBarSong.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                myMusicService.onProgress(binding.seekBarSong.progress)
            }
        })
    }

    override fun displayListMusic(musicList: ArrayList<Song>) {
        musicAdapter.upDateAdapter(musicList)
    }

    override fun onError() {
        Toast.makeText(this, "Error data", Toast.LENGTH_LONG).show()
    }

    override fun loadMusic(music: ArrayList<Song>, position: Int) {
        binding.constraintPlay.visibility = View.VISIBLE
        binding.textTitlePlay.text = music[position].title
        binding.textArtistPlay.text = music[position].artist
        if (myMusicService.isPlaying() == 1) {
            binding.imagePlay.setImageResource(R.drawable.ic_play)
        } else {
            binding.imagePlay.setImageResource(R.drawable.ic_pause)
        }
        Glide
            .with(applicationContext)
            .load(music[position].uri)
            .placeholder(R.drawable.icon_music_player).into(binding.imageAvatarPlay)
        binding.textSongTotalDurationLabel.text =
            simpleDateFormat.format(music[position].duration.toLong())

        myMusicService.chooseMusic(this, music, position)
    }

    override fun onUpTime(music: Song, progress: Int) {
        binding.textTitlePlay.text = music.title
        binding.textArtistPlay.text = music.artist
        Glide
            .with(applicationContext)
            .load(music.uri)
            .placeholder(R.drawable.icon_music_player).into(binding.imageAvatarPlay)
        binding.textSongTotalDurationLabel.text = simpleDateFormat.format(music.duration.toLong())
        binding.textSongCurrentDurationLabel.text =
            simpleDateFormat.format(progress.toLong()).toString()
        val timeSong = (progress * 100 / music.duration)
        binding.seekBarSong.progress = timeSong
    }

    private fun isRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Constant.MY_PERMISSIONS_REQUEST_WRITE
            )
        } else {
            musicPresenter.loadDisPlayListMusic()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.MY_PERMISSIONS_REQUEST_WRITE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    musicPresenter.loadDisPlayListMusic()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isRequestPermissions()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imagePlay -> {
                if (myMusicService.isPlaying() == 0) {
                    binding.imagePlay.setImageResource(R.drawable.ic_play)
                    myMusicService.pauseSong()
                } else {
                    binding.imagePlay.setImageResource(R.drawable.ic_pause)
                    myMusicService.playSong()
                }
            }
            R.id.imagePlayNext -> myMusicService.nextSong()
            R.id.imagePlayBack -> myMusicService.previousSong()
        }
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, MusicActivity::class.java)
    }
}
