package com.ndt.musicplayer.data.source

import android.os.AsyncTask
import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.data.model.OnDataLoadedCallback


class ReadExternalMusicTask(private val contentResolverData: ContentResolverData, private val callback: OnDataLoadedCallback<ArrayList<Song>>) : AsyncTask<Void, Void, ArrayList<Song>>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): ArrayList<Song> {
        return contentResolverData.getData()
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: ArrayList<Song>?) {
        if (result == null) {
            callback.onDataNotAvailable(NullPointerException())
        } else {
            callback.onDataLoaded(result)
        }
    }
}