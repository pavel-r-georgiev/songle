package me.pavelgeorgiev.songle

import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.collections.Map.*
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.word_list_item.*
import kotlinx.android.synthetic.main.word_list_item.view.*
import java.nio.file.Files.size
import android.R.attr.keySet
import android.content.Context


class WordAdapter(private val mData: HashMap<String, String>, private val mContext: Context) : BaseAdapter() {

    private val mKeys: MutableSet<String> = mData.keys
    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): String? {
        return mData[mKeys.elementAt(position)]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val word = getItem(position).toString()
        val location = mKeys.elementAt(position)
            val view: View?
            val vh: ListRowHolder
            if (convertView == null) {
                view = this.mInflater.inflate(R.layout.word_list_item, parent, false)
                vh = ListRowHolder(view)
                view?.tag = vh
            } else {
                view = convertView
                vh = view.tag as ListRowHolder
            }

        vh.word.text = word
        vh.location.text = location
        return view
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    private class ListRowHolder(row: View?) {
        var word: TextView = row?.lyrics_word as TextView
        var location: TextView = row?.lyrics_word_location as TextView
    }
}

