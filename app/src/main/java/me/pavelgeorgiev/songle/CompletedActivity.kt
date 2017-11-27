package me.pavelgeorgiev.songle

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*

class CompletedActivity : AppCompatActivity(), DownloadFileCallback, NetworkReceiver.NetworkStateReceiverListener{
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: RecyclerView.Adapter<SongAdapter.ViewHolder>
    private lateinit var mDrawer: Drawer
    private lateinit var mToolbar: Toolbar
    private lateinit var mReceiver: NetworkReceiver
    private var mSnackbar: Snackbar? = null
    private var mSongs = mutableListOf<Song>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mToolbar = toolbar as Toolbar
        mToolbar.title = getString(R.string.completed_songs)

        buildDrawerNav()
        mRecyclerView = recyclerView
//        Improves perfomance on fixed size layout
        mRecyclerView.setHasFixedSize(true)

        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        mAdapter = SongAdapter(mSongs, this, true)
        mRecyclerView.adapter = mAdapter

        getSongs()
        mReceiver =  NetworkReceiver()
        mReceiver.addListener(this)
        this.registerReceiver(mReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private fun buildDrawerNav() {
        val item1 = PrimaryDrawerItem().
                withIdentifier(1)
                .withName("Completed Songs")
                .withIcon(R.drawable.ic_library_music_black_24dp)
                .withSelectable(false)


        mDrawer = CommonFunctions.buildDrawerNav(arrayOf(item1), this, mToolbar)
                .withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener { _, position, _ ->
                    when (position) {
                        1 -> {
                            startActivity(Intent(this, MainActivity::class.java))
                            return@OnDrawerItemClickListener true
                        }
                        else -> {
                            return@OnDrawerItemClickListener false
                        }
                    }
                }).build()

        mDrawer.setSelection(-1)
    }

    public override fun onDestroy() {
        super.onDestroy()
        mReceiver.removeListener(this)
        this.unregisterReceiver(mReceiver)
    }

    private fun getSongs() {
        if (NetworkReceiver.isNetworkConnected(this)) {
            DownloadFileService(this, DownloadFileService.XML_TYPE).execute(getString(R.string.songs_xml_url))
        } else {
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                    .setMessage("Songle requires Internet connection. Please connect to continue.")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
        }
    }


    override fun downloadComplete(result: ByteArray, fileType: String) {
        val result = SongsXmlParser().parse(result.inputStream())
        mSongs.clear()
        mSongs.addAll(result)
        mAdapter.notifyDataSetChanged()
    }

    override fun downloadFailed(errorMessage: String?, fileType: String) {
        AlertDialog.Builder(this@CompletedActivity).setTitle("Download Error")
                .setMessage("Failed downloading the song list.")
                .setPositiveButton(getString(R.string.try_again)) { _, _ ->
                    if(fileType == DownloadFileService.XML_TYPE){
                        getSongs()
                    }
                }.show()
    }

    override fun networkAvailable() {
        getSongs()
        mSnackbar?.dismiss()
    }

    override fun networkUnavailable() {
        val snackbar = Snackbar.make(findViewById(R.id.layout_main), "Network status: OFFLINE",
                Snackbar.LENGTH_INDEFINITE)
        mSnackbar = snackbar
        snackbar.show()
    }
}
