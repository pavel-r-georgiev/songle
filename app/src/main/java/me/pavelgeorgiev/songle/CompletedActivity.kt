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
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*

class CompletedActivity : AppCompatActivity(), NetworkReceiver.NetworkStateReceiverListener{
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: RecyclerView.Adapter<SongAdapter.ViewHolder>
    private lateinit var mDrawer: Drawer
    private lateinit var mToolbar: Toolbar
    private lateinit var mReceiver: NetworkReceiver
    private var mSnackbar: Snackbar? = null
    private var mCompletedSongs = mutableListOf<Song>()
    private lateinit var mDatabase: DatabaseReference
    private val TAG = "CompletedActivity"

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
        mAdapter = SongAdapter(mCompletedSongs, this, true)
        mRecyclerView.adapter = mAdapter

        mDatabase = FirebaseDatabase
                .getInstance()
                .reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid)

        getCompletedSongs()
        mReceiver =  NetworkReceiver()
        mReceiver.addListener(this)
        this.registerReceiver(mReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private fun buildDrawerNav() {
        val item1 = PrimaryDrawerItem().
                withIdentifier(1)
                .withName("All Songs")
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

    private fun getCompletedSongs() {
        if (!NetworkReceiver.isNetworkConnected(this)) {
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                    .setMessage("Songle requires Internet connection. Please connect to continue.")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
        }

        mDatabase.child("completed-songs").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.w(TAG, "loadCompletedSongs:onCancelled", databaseError?.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mCompletedSongs.clear()
                if (snapshot.exists()) {
                    for (song in snapshot.children) {
                        mCompletedSongs.add(Song(song.value as HashMap<String, Any>))
                    }
                }
            }
        })
        mAdapter.notifyDataSetChanged()
    }


    override fun networkAvailable() {
        getCompletedSongs()
        mSnackbar?.dismiss()
    }

    override fun networkUnavailable() {
        val snackbar = Snackbar.make(findViewById(R.id.layout_main), "Network status: OFFLINE",
                Snackbar.LENGTH_INDEFINITE)
        mSnackbar = snackbar
        snackbar.show()
    }
}
