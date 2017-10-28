package me.pavelgeorgiev.songle

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DownloadXmlCallback {
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
        mAdapter = SongAdapter(mSongs, this)
        mRecyclerView.adapter = mAdapter

        if (isNetworkConnected()) {
            getSongs()
        } else {
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                    .setMessage("Please check your internet connection and try again")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
        }



    }

    /**
     * Checks if device is connected to the Internet
     */
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
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

    override fun downloadComplete(result: List<Song>) {
        mSongs.clear()
        mSongs.addAll(result)
        mAdapter.notifyDataSetChanged()
    }

}
