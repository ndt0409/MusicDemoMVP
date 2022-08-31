package com.ndt.musicplayer.ui


import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.data.model.OnDataLoadedCallback
import com.ndt.musicplayer.data.repository.MusicRepository
import kotlin.collections.ArrayList

class MusicPresenter(private val musicRepository: MusicRepository, private val musicActivity: MusicContract.View) : MusicContract.Presenter {
    override fun loadDisPlayListMusic() {
        musicRepository.getMusics(object : OnDataLoadedCallback<ArrayList<Song>> {
            override fun onDataLoaded(data: ArrayList<Song>) {
                musicActivity.displayListMusic(data)
            }

            override fun onDataNotAvailable(exception: Exception) {
                musicActivity.onError()
            }
        })
    }
}
