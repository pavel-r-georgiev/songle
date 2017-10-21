package me.pavelgeorgiev.songle

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.song_list_item.view.*
import java.util.*


class SongAdapter (private val songs: List<Song>)

    : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    override fun getItemCount() = songs.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.song_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
//     Create intent to go to Map activity
        val intent = Intent(holder.cv.context, MapsActivity::class.java)
//     Put number of song. Used to load corresponding words on map
        intent.putExtra("NUMBER", song.number)



        holder.title.text = if(Math.random() > 0.5) song.title else "Unknown"
        holder.artist.text = song.artist
        holder.title.text = song.title
        holder.link.text = song.link

        holder.cv.setOnClickListener({ holder.cv.context.startActivity(intent) })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cv = view.cv
        val title = view.song_name
        val artist = view.song_artist
        val link = view.song_link
    }


}