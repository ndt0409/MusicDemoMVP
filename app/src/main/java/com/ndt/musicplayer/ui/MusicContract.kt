package com.ndt.musicplayer.ui

import com.ndt.musicplayer.data.model.Song

interface MusicContract {
    interface View{
        fun displayListMusic(musicList:ArrayList<Song>)
        fun onError()
    }
    interface Presenter{
        fun loadDisPlayListMusic()
    }
}
