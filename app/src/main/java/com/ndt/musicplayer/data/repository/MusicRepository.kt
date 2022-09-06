package com.ndt.musicplayer.data.repository

import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.data.model.OnDataLoadedCallback
import com.ndt.musicplayer.data.source.MusicDatasource
import com.ndt.musicplayer.data.source.MusicLocalDatasource
import java.net.MalformedURLException

class MusicRepository(private val localDataSource: MusicDatasource.Local) : MusicDatasource.Local {

    override fun getMusics(onDataLoadedCallback: OnDataLoadedCallback<ArrayList<Song>>) {
        try {
            localDataSource.getMusics(onDataLoadedCallback)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            onDataLoadedCallback.onDataNotAvailable(e)
        }
    }
}
