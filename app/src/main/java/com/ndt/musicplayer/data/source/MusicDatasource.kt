package com.ndt.musicplayer.data.source

import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.data.model.OnDataLoadedCallback


interface MusicDatasource {
    interface Local {
        fun getMusics(onDataLoadedCallback: OnDataLoadedCallback<ArrayList<Song>>)
        
    }
}
