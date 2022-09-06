package com.ndt.musicplayer.data.model

interface OnDataLoadedCallback<T> {

    fun onDataLoaded(data: T)

    fun onDataNotAvailable(exception: Exception)

}