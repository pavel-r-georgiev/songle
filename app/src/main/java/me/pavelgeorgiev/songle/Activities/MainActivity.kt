package me.pavelgeorgiev.songle.Activities

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import me.pavelgeorgiev.songle.*
import me.pavelgeorgiev.songle.Adapters.SongAdapter
import me.pavelgeorgiev.songle.Objects.Song
import me.pavelgeorgiev.songle.Parsers.SongsXmlParser


/**
 * Activity contains list of all songs - completed and not
 */
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
    private var mGuessedSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mToolbar = toolbar as Toolbar
        mToolbar.title = getString(R.string.app_name)

        buildDrawerNav()
        mRecyclerView = recyclerView
//        Improves perfomance on fixed size layout
        mRecyclerView.setHasFixedSize(true)
//      Setup recycler view for the list of songs
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        mAdapter = SongAdapter(mSongs.values, this, false, mCompletedSongs)
        mRecyclerView.adapter = mAdapter

//      Setup network receiver
        mReceiver = NetworkReceiver()
        mReceiver.addListener(this)
        this.registerReceiver(mReceiver, IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
//      Retrieve the guessed songs - object only exists if user guessed a song and is redirected to MainActivity
        mGuessedSong = intent.getParcelableExtra(getString(R.string.intent_song_object))
    }

    /**
     * Retrieve completed songs from the Firebase database
     */
    private fun getCompletedSongs() {
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
    }

    /**
     * Builds drawer navigation for the activity
     */
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

    /**
     * Retrieve completed songs and try to retrieve list of all songs.
     * If network is available - download the list
     * If network is NOT available - try to retrieve from Firebase database cache
     */
    private fun getSongs() {
        getCompletedSongs()
        if (NetworkReceiver.isNetworkConnected(this)) {
            DownloadFileService(this, DownloadFileService.XML_TYPE).execute(getString(R.string.songs_xml_url))
        } else {
            getSongsFromDatabase()
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                    .setMessage(getString(R.string.offline_disclaimer_songs_list))
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
        }
    }

    /**
     * Callback for DownloadFileService
     */
    override fun downloadComplete(result: ByteArray, fileType: String) {
        val result = SongsXmlParser().parse(result.inputStream())
        mSongs.clear()
        result.forEach({ mSongs.put(it.title, it) })
        markSongCompleted()
        mAdapter.notifyDataSetChanged()
    }

    /**
     * Callback for DownloadFileService
     */
    override fun downloadFailed(errorMessage: String?, fileType: String) {
        AlertDialog.Builder(this@MainActivity).setTitle("Download Error")
                .setMessage("Failed downloading the song list.")
                .setPositiveButton(getString(R.string.try_again)) { _, _ ->
                    if(fileType == DownloadFileService.XML_TYPE){
                        getSongs()
                    }
                }.show()
    }

    /**
     * Mark song as completed if a completed song object is passed with the intent leading to MainActivity
     */
    private fun markSongCompleted() {
        if(mGuessedSong != null){
            val song = mGuessedSong as Song
            mCompletedSongs.put(song.title , song)
        }
    }

    /**
     * Retrieve song list from Firebase DB
     */
    private fun getSongsFromDatabase(){
        mDatabase.child("song-list").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.w(TAG, "loadTimestamp:onCancelled", databaseError?.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children
                            .map({ Song(it.value as java.util.HashMap<String, Any>) })
                            .forEach({mSongs.put(it.title, it)})
                }
            }
        })
        mAdapter.notifyDataSetChanged()
    }

    /**
     * NetworkReceiver callback
     */
    override fun networkAvailable() {
        if(mSongs.isEmpty()){
            getSongs()
        } else {
            mAdapter.notifyDataSetChanged()
        }

       mSnackbar?.dismiss()
    }

    /**
     * NetworkReceiver callback
     */
    override fun networkUnavailable() {
        if(mSongs.isEmpty()){
            getSongs()
        }
        val snackbar = Snackbar.make(findViewById(R.id.layout_main), "Network status: OFFLINE",
                Snackbar.LENGTH_INDEFINITE)
        mSnackbar = snackbar
        snackbar.show()
    }

}
