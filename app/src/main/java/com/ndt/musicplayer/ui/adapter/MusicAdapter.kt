package com.ndt.musicplayer.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ndt.musicplayer.R
import com.ndt.musicplayer.data.model.Song
import com.ndt.musicplayer.databinding.ItemSongBinding
import java.util.logging.Filter

class MusicAdapter(
    private val onItemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    //private var musics = ArrayList<Song>()
    private var musics = mutableListOf<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(
            ItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = musics.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSongList(songList: List<Song>) {
        this.musics = songList as MutableList<Song>
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.onBindData(musics, position, onItemClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun upDateAdapter(newMusics: List<Song>) {
        musics.clear()
        musics.addAll(newMusics)
        notifyDataSetChanged()
    }

    class ViewHolder(
        private var binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBindData(
            musics: MutableList<Song>,
            position: Int,
            onItemClickListener: OnItemClickListener
        ) {
            val music = musics[position]

            itemView.run {
                binding.textItemTitlePlay.text = music.title
                binding.textItemArtistPlay.text = music.artist

                Glide.with(context)
                    .load((music.uri))
                    .placeholder(R.drawable.icon_music_player).into(binding.imageAvatar)

                binding.constraintItem.setOnClickListener {
                    onItemClickListener.loadMusic(musics as ArrayList<Song>, position)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun loadMusic(music: ArrayList<Song>, position: Int)
    }
}