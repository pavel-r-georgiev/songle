package me.pavelgeorgiev.songle

import com.google.android.gms.maps.model.LatLng

data class Placemark(val name: String, val description: String, val styleUrl: String, val location: LatLng, val style: KmlStyle?){
    companion object {
//        Marker type ID
        val UNCLASSIFIED = "unclassified"
        val BORING = "boring"
        val NOT_BORING = "notboring"
        val INTERESTING = "interesting"
        val VERY_INTERESTING = "veryinteresting"
    }
}