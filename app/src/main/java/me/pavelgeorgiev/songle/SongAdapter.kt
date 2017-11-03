package me.pavelgeorgiev.songle

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.song_list_item.view.*
import android.R.attr.radius
import com.squareup.picasso.Callback
import jp.wasabeef.picasso.transformations.BlurTransformation


class SongAdapter (private val songs: List<Song>, private val context: Context)

    : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    override fun getItemCount() = songs.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.song_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
//     Create intent to go to Map activity
        val intent = Intent(holder.cv.context, DifficultyActivity::class.java)
//     Put number of song. Used to load corresponding words on map
        intent.putExtra(context.getString(R.string.intent_song_number), song.number)
        intent.putExtra(context.getString(R.string.intent_song_title), song.title)


        holder.artist.text = song.artist
        holder.title.text = if(Math.random() > 0.5) song.title else context.getString(R.string.unknown)
        holder.link.text = if(holder.title.text != context.getString(R.string.unknown)) song.link else ""

        val youtubeLink = song.link.trim().split("/")
        val imageUrlBase = "http://img.youtube.com/vi/${youtubeLink.last()}"
        val imageUrlHighRes = "$imageUrlBase/maxresdefault.jpg"
        val imageUrlNormal = "$imageUrlBase/0.jpg"
        val picasso = Picasso.with(context).load(imageUrlHighRes).error(R.drawable.placeholder_song_image)

        if(holder.title.text == context.getString(R.string.unknown)){
            picasso.transform(BlurTransformation(context, 25, 5))
        }

        picasso.into(holder.image, object: Callback{
            override fun onSuccess() {}
            override fun onError() {
                val picasso = Picasso.with(context).load(imageUrlNormal)
                if(holder.title.text == context.getString(R.string.unknown)){
                    picasso.transform(BlurTransformation(context, 15, 5))
                }
                picasso.into(holder.image)
            }
        })


        holder.cv.setOnClickListener({ holder.cv.context.startActivity(intent) })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cv = view.cv
        val title = view.song_name
        val artist = view.song_artist
        val link = view.song_link
        val image = view.song_image
    }


}