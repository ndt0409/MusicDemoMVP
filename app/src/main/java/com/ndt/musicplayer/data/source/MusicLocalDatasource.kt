package com.ndt.musicplayer.data.source

import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.data.model.OnDataLoadedCallback


class MusicLocalDatasource(private val contentResolverData: ContentResolverData) : MusicDatasource.Local {

    override fun getMusics(onDataLoadedCallback: OnDataLoadedCallback<ArrayList<Song>>) {
        ReadExternalMusicTask(contentResolverData, onDataLoadedCallback).execute()
    }
}