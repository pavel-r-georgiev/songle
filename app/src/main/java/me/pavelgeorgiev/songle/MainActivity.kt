package me.pavelgeorgiev.songle

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DownloadCallback {
    private var receiver = NetworkReceiver()
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: RecyclerView.Adapter<SongAdapter.ViewHolder>
    private var mSongs = mutableListOf<Song>()
    private var mContext = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//
//        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        this.registerReceiver(receiver, filter)

        mRecyclerView = recyclerView
//        Improves perfomance on fixed size layout
        mRecyclerView.setHasFixedSize(true)

        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        mAdapter = SongAdapter(mSongs)
        mRecyclerView.adapter = mAdapter

        getSongs()

    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // Unregisters BroadcastReceiver when app is destroyed.
//        if (receiver != null) {
//            this.unregisterReceiver(receiver)
//        }
//    }

    private fun getSongs() {
        DownloadXmlService(this).execute(getString(R.string.songs_xml_url))
    }

    fun onMapSelect(number : String){
        val intent = Intent(mContext, MapsActivity::class.java)
        startActivity(intent)
    }

    override fun downloadComplete(result: List<Song>) {
        mSongs.clear()
        mSongs.addAll(result)
        mAdapter.notifyDataSetChanged()
    }

}
