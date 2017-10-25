package me.pavelgeorgiev.songle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.LocationListener;
import android.os.Build
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.widget.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.kml.KmlLayer
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
    private lateinit var mLayer: KmlLayer
    private lateinit var mSongNumber: String
    private var mLyrics = HashMap<Int, List<String>>()
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mDrawerList: ListView
    private lateinit var mMenuAdapter: ArrayAdapter<String>
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private var mCollectedWords = HashMap<String, String>()
    private lateinit var mWordsAdapter: WordAdapter
    private lateinit var mWordsListView: ListView

    val MENU_ARRAY = arrayOf("Songs List", "Lyrics of current song")
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

        mSongNumber = intent.getStringExtra("NUMBER")
        var baseUrl = "${getString(R.string.maps_base_url)}/$mSongNumber"

        DownloadFileService(this, KML_TYPE).execute("$baseUrl/map5.kml")
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
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        mLayer.removeLayerFromMap()
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
        mLayer = KmlLayer(mMap, bytes.inputStream(), applicationContext)
        mLayer.addLayerToMap()
        val containers = mLayer.containers

        mLayer.setOnFeatureClickListener({ feature ->
            if(feature == null){
                return@setOnFeatureClickListener
            }
            val wordCoord = feature.getProperty("name").split(":").map { it.toInt() }
            val line = wordCoord[0]
            val word = wordCoord[1] - 1

            Log.i("KmlClick", "Feature clicked: " + feature.getProperty("name"))

            collectWord(mLyrics[line]?.get(word), feature.getProperty("name"))
        })
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
        mDrawerList = navList as ListView

        mMenuAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, MENU_ARRAY)
        mDrawerList.adapter = mMenuAdapter
        mDrawerLayout = drawer_layout

        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        mapMenuButton.setOnClickListener({ toggleMenu(it) })

        mDrawerList.setOnItemClickListener({ _: AdapterView<*>, _: View, position: Int, _: Long ->
            when(position) {
                0 -> startActivity(Intent(this, MainActivity::class.java))
            }}
        )
    }

    private fun buildSlidingPanel() {
        sliding_layout_header.text = "${mCollectedWords.size} words collected"
        mWordsAdapter = WordAdapter(mCollectedWords, this)
        mWordsListView = lyrics_word_list
        mWordsListView.adapter = mWordsAdapter
    }

    private fun toggleMenu(view: View) {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }

     private fun collectWord(word: String?, location: String) {
         if(word == null){
             return
         }

         mCollectedWords.put(word, location)
         sliding_layout_header.text = "${mCollectedWords.size} words collected"
         mWordsAdapter.notifyDataSetChanged()
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F));
    }

}

