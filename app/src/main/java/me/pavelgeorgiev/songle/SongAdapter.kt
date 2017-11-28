package me.pavelgeorgiev.songle
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.song_list_item.view.*
import com.squareup.picasso.Callback
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.android.synthetic.main.song_list_item_completed.view.*
import android.widget.RelativeLayout




class SongAdapter (private val songs: MutableCollection<Song>, private val context: Context, private val interactive: Boolean, private val completedSongs: HashMap<String, Song> = HashMap())
    : RecyclerView.Adapter<SongAdapter.ViewHolder>(){
    private var mExpandedPosition = -1
    override fun getItemCount() = songs.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemLayout =
                if(interactive) R.layout.song_list_item_completed
                else R.layout.song_list_item
        return  ViewHolder(layoutInflater.inflate(itemLayout, parent, false), interactive)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songsArray = ArrayList<Song>(songs)
        val song = songsArray[position]
//     Create intent to go to Map activity
        val intent = Intent(holder.cv.context, DifficultyActivity::class.java)
//     Put number of song. Used to load corresponding words on map
        intent.putExtra(context.getString(R.string.intent_song_number), song.number)
        intent.putExtra(context.getString(R.string.intent_song_title), song.title)

        if(completedSongs.containsKey(song.title) && completedSongs[song.title] == song){
            song.completed = true
        }

        holder.artist.text = song.artist
        holder.title.text = if(interactive || song.completed) song.title else context.getString(R.string.unknown)
//        holder.link.text = if(holder.title.text != context.getString(R.string.unknown)) song.link else ""

        val youtubeLink = song.link.trim().split("/")
        val imageUrlBase = "http://img.youtube.com/vi/${youtubeLink.last()}"
        val imageUrlHighRes = "$imageUrlBase/maxresdefault.jpg"
        val imageUrlNormal = "$imageUrlBase/0.jpg"
        val picasso = Picasso.with(context).load(imageUrlHighRes).error(R.drawable.placeholder_song_image)

//        Needed?
        if(!song.completed){
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


        if(interactive){
            holder.arrow?.setOnClickListener({
                holder.expand_area?.visibility = View.VISIBLE
                holder.arrow.setImageResource(R.drawable.arrow_drop_up_black)
                holder.image.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 360)
                val params =  RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.addRule(RelativeLayout.BELOW, holder.image.id)
                holder.text.layoutParams = params
            })
        } else {
            holder.cv.setOnClickListener({ holder.cv.context.startActivity(intent) })
        }
    }

    class ViewHolder(view: View, interactive: Boolean) : RecyclerView.ViewHolder(view) {
        val cv = if(interactive) view.cv_completed else view.cv
        val title = if(interactive) view.song_name_completed else view.song_name
        val text = if(interactive) view.song_text_completed else view.song_text
        val artist = if(interactive) view.song_artist_completed else view.song_artist
        val link = if(interactive) view.song_link_completed else view.song_link
        val image = if(interactive) view.song_image_completed else view.song_image
        val arrow = if(interactive) view.expand_arrow else null
        val expand_area = if(interactive) view.expand_area else null
    }
}