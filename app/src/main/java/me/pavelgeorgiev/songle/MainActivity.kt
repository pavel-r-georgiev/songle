package me.pavelgeorgiev.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter
import android.support.design.widget.Snackbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*




class MainActivity : AppCompatActivity(), DownloadFileCallback, NetworkReceiver.NetworkStateReceiverListener {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: RecyclerView.Adapter<SongAdapter.ViewHolder>
    private lateinit var mDrawer: Drawer
    private lateinit var mToolbar: Toolbar
    private lateinit var mReceiver: NetworkReceiver
    private var mSnackbar: Snackbar? = null
    private var mSongs = LinkedHashMap<String, Song>()
    private var mCompletedSongs = HashMap<String, Song>()
    private val mDatabase = FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    private val TAG = "MainActivity"
    private var mGuessedSongTitle: String? = null
    private var mGuessedSongDifficulty: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mToolbar = toolbar as Toolbar
        mToolbar.title = getString(R.string.app_name)

        buildDrawerNav()
        mRecyclerView = recyclerView
//        Improves perfomance on fixed size layout
        mRecyclerView.setHasFixedSize(true)

        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        mAdapter = SongAdapter(mSongs.values, this, false, mCompletedSongs)
        mRecyclerView.adapter = mAdapter

        mReceiver =  NetworkReceiver()
        mReceiver.addListener(this)
        this.registerReceiver(mReceiver, IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION))

        mDatabase.keepSynced(true)

        mGuessedSongTitle = intent.getStringExtra(getString(R.string.intent_song_title))
        mGuessedSongDifficulty  = intent.getStringExtra(getString(R.string.intent_song_difficulty))

        getSongList()
    }

    private fun getSongList() {
        mDatabase.child("completed-songs").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.w(TAG, "loadCompletedSongs:onCancelled", databaseError?.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (song in snapshot.children) {
                        mCompletedSongs.put(song.key, Song(song.value as HashMap<String, Any>))
                    }
                }
            }
        })
        getSongs()
    }

    private fun buildDrawerNav() {
        val item1 = PrimaryDrawerItem().
                withIdentifier(1)
                .withName(getString(R.string.completed_songs))
                .withIcon(R.drawable.ic_library_music_black_24dp)
                .withSelectable(false)


        mDrawer = CommonFunctions.buildDrawerNav(arrayOf(item1), this, mToolbar)
                .withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener { _, position, _ ->
                    when (position) {
                        1 -> {
                            startActivity(Intent(this, CompletedActivity::class.java))
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
        result.forEach({ mSongs.put(it.title, it) })
        markCompletedSong()
        mAdapter.notifyDataSetChanged()
    }

    private fun markCompletedSong() {
        if(mGuessedSongTitle != null && mGuessedSongDifficulty != null){
            val song = mSongs[mGuessedSongTitle as String]!!
            song.completed = true
            song.addCompletedDifficulty(mGuessedSongDifficulty as String)
            mCompletedSongs.put(song.title, song)
            val dbReference = mDatabase.child("completed-songs")
            val childUpdates = mutableMapOf<String, Song>()
            childUpdates[song.title] = song
            dbReference.updateChildren(childUpdates as Map<String, Any>?)
        }
    }

    override fun downloadFailed(errorMessage: String?, fileType: String) {
        AlertDialog.Builder(this@MainActivity).setTitle("Download Error")
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
