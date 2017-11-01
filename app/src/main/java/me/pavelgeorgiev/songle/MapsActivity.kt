package me.pavelgeorgiev.songle

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.location.LocationListener;
import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.data.Feature
import com.google.maps.android.data.kml.KmlLayer
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import kotlinx.android.synthetic.main.activity_maps.*


@Suppress("DEPRECATION")
class MapsActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        DownloadFileCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    private var mLayer: KmlLayer? = null
    private lateinit var mSongNumber: String
    private lateinit var mSongTitle: String
    private lateinit var mSongMapVersion: String
    private var mLyrics = HashMap<Int, List<String>>()
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mDrawer: Drawer
    private var mCollectedWords = HashMap<String, String>()
    private lateinit var mWordsAdapter: WordAdapter
    private lateinit var mWordsListView: ListView
    private lateinit var mPlacemarks: HashSet<Placemark>
    private lateinit var mLastPlacemarkLocation: LatLng

    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    val TAG = "MapsActivity"
    val LOCATION_REQUEST_INTERVAL: Long = 5000
    val LOCATION_REQUEST_FASTEST_INTERVAL: Long = 1000
    val KML_TYPE = "KML"
    val TXT_TYPE = "TXT"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buildDrawerNav()
        buildSlidingPanel()
        buildGoogleApiClient()

        mSongNumber = intent.getStringExtra(getString(R.string.intent_song_number))
        mSongTitle = intent.getStringExtra(getString(R.string.intent_song_title))
        mSongMapVersion = intent.getStringExtra(getString(R.string.intent_song_map_version))

        val baseUrl = "${getString(R.string.maps_base_url)}/$mSongNumber"
        val mapVersion = "map$mSongMapVersion.kml"

        DownloadFileService(this, KML_TYPE).execute("$baseUrl/$mapVersion")
        DownloadFileService(this, TXT_TYPE).execute("$baseUrl/words.txt")
    }


    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onResume() {
        super.onResume()
        onUiChange()
    }
    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onPause() {
        super.onPause()

//        Stop location updates when Activity is not active
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        mLayer?.removeLayerFromMap()
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mMap.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mMap.isMyLocationEnabled = true
        }

        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true

    }

    /**
     * Invoked after KML file has been downloaded. Loads KML layout on the map.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    override fun downloadComplete(bytes: ByteArray, fileType: String) {
        if (fileType == KML_TYPE) {
            onKmlDownload(bytes)
        }
        if (fileType == TXT_TYPE) {
            onTxtDownload(bytes)
        }
    }

    private fun onTxtDownload(bytes: ByteArray) {
        val inputStream = bytes.inputStream()

        inputStream.bufferedReader().useLines { lines ->
            lines.forEach {
                parseLine(it)
            }
        }
    }

    private fun parseLine(line: String) {
        val lineList = line.trim().split("\\s+".toRegex())
        val lineNumber = lineList[0].toInt()

        if (lineList.size < 2) {
            return
        }

        val lineWords = lineList.subList(1, lineList.size)
        mLyrics.put(lineNumber, lineWords)
    }

    private fun onKmlDownload(bytes: ByteArray) {
//        mLayer = KmlLayer(mMap, bytes.inputStream(), applicationContext)
//        mLayer?.addLayerToMap()
//        mLayer?.setOnFeatureClickListener({ onFeatureClick(it)})
        mPlacemarks = KmlParser().parse(bytes.inputStream(), this)

        mMap.setOnMarkerClickListener { onMarkerClick(it) }
        mPlacemarks.forEach({createMarker(it, it.location)})
        if(mCurrLocationMarker != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLocationMarker!!.position, 17.5F))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastPlacemarkLocation,17.5F))
        }
    }

    private fun createMarker(placemark: Placemark, location: LatLng){
        mLastPlacemarkLocation = location
        mMap.addMarker(MarkerOptions()
                        .position(location)
                        .title(placemark.name)
                        .snippet(placemark.description))

    }

//    private fun onFeatureClick(marker: Feature) {
//        if(feature == null){
//            return
//        }
//        val wordCoord = feature.getProperty("name").split(":").map { it.toInt() }
//        val line = wordCoord[0]
//        val word = wordCoord[1] - 1
//
//        Log.i("KmlClick", "Feature clicked: " + feature.getProperty("name"))
//        val locationInText = "Line: $line, position: ${wordCoord[1]}"
//        collectWord(mLyrics[line]?.get(word), locationInText)
//    }

    private fun onMarkerClick(marker: Marker): Boolean {
        if(marker == null){
            return false
        }
        val wordCoord = marker.title.split(":").map { it.toInt() }
        val line = wordCoord[0]
        val word = wordCoord[1] - 1

        marker.remove()

        Log.i("Marker click", "Marker clicked: " + marker.title)
        val locationInText = "Line: $line, position: ${wordCoord[1]}"

        if(marker.)
        collectWord(mLyrics[line]?.get(word), locationInText)

        return true
    }

    /**
     * Invoked once GoogleApiClient is connected.
     * If location permission is given request location update.
     */
    override fun onConnected(connectionHint: Bundle?) {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = LOCATION_REQUEST_INTERVAL
        mLocationRequest.fastestInterval = LOCATION_REQUEST_FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    /**
     * Checks if location permission is given.
     * If that is not the case, it prompts the informs the user that location permission is required
     * and prompts them to give location permission to the app.
     */
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                        })
                        .create()
                        .show()

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted. Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true
                    }

                } else {
                    // Permission denied. Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    /**
     * Puts the activity in immersive sticky mode
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    /**
     * Brings the activity back in immersive mode after key press or keyboard pop up
     */
    private fun onUiChange() {
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun buildDrawerNav() {
        val item1 = PrimaryDrawerItem().
                withIdentifier(1)
                .withName("Songs")
                .withIcon(R.drawable.ic_music_note_black_24dp)
                .withSelectable(false)
                .withSelectedTextColor(resources.getColor(R.color.primaryColor))

        val user = FirebaseAuth.getInstance().currentUser

        val header = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(ProfileDrawerItem().withEmail(user?.email).withIcon(R.drawable.ic_account_circle_black_24dp))
                .build()

        mDrawer = DrawerBuilder()
                .withAccountHeader(header)
                .withActivity(this)
                .addDrawerItems(
                        item1
                )
                .withFullscreen(true)
                .withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener{ _, position, _ ->
                        when(position) {
                            1 -> run {
                                startActivity(Intent(this, MainActivity::class.java))
                                return@OnDrawerItemClickListener true
                            }
                            else -> {
                                return@OnDrawerItemClickListener false
                            }
                        }
                    }).build()

        mDrawer.setSelection(-1)

        // Code makes sure the drawer doesn't brake the fullscreen mode
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }


        if (Build.VERSION.SDK_INT >= 19) {
            mDrawer.drawerLayout.fitsSystemWindows = false;
        }
        mapMenuButton.setOnClickListener({toggleMenu(it)})
    }


    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes;
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams;
    }


    private fun buildSlidingPanel() {
        sliding_layout_header.text = buildWordsCollectedString(mCollectedWords.size)
        song_name_input.setOnEditorActionListener({view, actionId, event -> 
            var handled = false

            if(actionId === EditorInfo.IME_ACTION_DONE){
                guessSong(view.text.toString())
                handled = true
            }
            return@setOnEditorActionListener handled
        })
        mWordsAdapter = WordAdapter(mCollectedWords, this)
        mWordsListView = lyrics_word_list
        mWordsListView.adapter = mWordsAdapter
    }

    private fun guessSong(songName: String) {
        var dialog: Dialog
        if(mSongTitle.toLowerCase() == songName.toLowerCase()){
           dialog = AlertDialog.Builder(this)
                    .setTitle("Success!")
                    .setMessage("Congratulations you guessed the song.")
                    .setNeutralButton("New Song", { _, _ ->
                        startActivity(Intent(this, MainActivity::class.java))
                    })
                    .create()
        } else {
            dialog = AlertDialog.Builder(this)
                    .setTitle("Wrong")
                    .setMessage("Keep trying you are close!")
                    .setNeutralButton("New Song", { _, _ ->
                        startActivity(Intent(this, MainActivity::class.java))
                    })
                    .setPositiveButton("Try again", { dialog, _ ->
                        dialog.dismiss()
                    })
                    .create()
        }

        dialog.show()
    }


    private fun toggleMenu(view: View) {
        if (mDrawer.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawer.closeDrawer()
        } else {
            mDrawer.openDrawer()
        }
    }

     private fun collectWord(word: String?, location: String) {
         if(word == null){
             return
         }

         mCollectedWords.put(word, location)
         sliding_layout_header.text = buildWordsCollectedString(mCollectedWords.size)
         mWordsAdapter.notifyDataSetChanged()
     }

    private fun buildWordsCollectedString(size: Int) : String{
        val word = if(size == 1) "word" else "words"
        return "$size $word collected"
    }

    override fun onConnectionSuspended(flag : Int) {
        println(">>>> Connection to Google APIs suspended. $TAG [onConnectionSuspended")
    }

    override fun onConnectionFailed(result : ConnectionResult) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently
        println(">>>> Connection to Google APIs could not be established. $TAG [onConnectionFailed")
    }

    override fun onLocationChanged(location: Location?) {
        if(location == null) {
            println("$TAG [onLocationChanged] Location unknown")
            return
        }

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }

        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        mCurrLocationMarker = mMap.addMarker(markerOptions)

        //Move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5F));
    }

}

