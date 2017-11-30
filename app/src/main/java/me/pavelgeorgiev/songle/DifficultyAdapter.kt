package me.pavelgeorgiev.songle

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.difficulty_list_item.view.*


class DifficultyAdapter(context: Context,
                        resource: Int,
                        private val difficulties: List<Difficulty>,
                        private val song: Song)
    :ArrayAdapter<Difficulty>(context, resource, difficulties) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val difficulty = difficulties.elementAt(position)

        val view: View?
        val vh: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.difficulty_list_item, parent, false)
            vh = ViewHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }


        vh.difficulty.text = difficulty.difficulty
        vh.description.text = difficulty.description
        vh.icon.setImageResource(difficulty.iconResource)

        vh.layout.setOnClickListener({onDifficultyClick(difficulty.mapVersion)})

        return view
    }

    private fun onDifficultyClick(mapVersion: String){
        val intent = Intent(context, MapsActivity::class.java)
        intent.putExtra(context.getString(R.string.intent_song_map_version), mapVersion)
        intent.putExtra(context.getString(R.string.intent_song_object), song)
        context.startActivity(intent)
    }

    private class ViewHolder(row: View?) {
        var layout = row?.difficulty_list_item as LinearLayout
        var difficulty: TextView = row?.difficulty_title as TextView
        var description: TextView = row?.difficulty_description as TextView
        var icon: ImageView = row?.difficulty_icon as ImageView
    }
}