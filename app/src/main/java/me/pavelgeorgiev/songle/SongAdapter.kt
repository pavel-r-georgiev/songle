package me.pavelgeorgiev.songle

import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.song_list_item.view.*


class SongAdapter (private val songs: List<Song>)

    : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    override fun getItemCount() = songs.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.song_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val song = songs[position]

        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.title.text = song.title
        holder.link.text = song.link
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cv = view.cv
        val title = view.song_name
        val artist = view.song_artist
        val link = view.song_link
    }


}