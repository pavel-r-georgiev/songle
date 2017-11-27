package me.pavelgeorgiev.songle

import com.google.android.gms.maps.model.LatLng
import java.util.HashMap

data class Placemark(val name: String, val description: String, val location: LatLng, val styleId: String){
    companion object {
//        Marker type ID
        val UNCLASSIFIED = "unclassified"
        val BORING = "boring"
        val NOT_BORING = "notboring"
        val INTERESTING = "interesting"
        val VERY_INTERESTING = "veryinteresting"

        fun build(map: HashMap<String, Any>): Placemark{
            val locationMap = map["location"] as HashMap<String, Double>
            val location = LatLng(locationMap["latitude"]!!, locationMap["longitude"]!!)
            val name = map["name"] as String
            val description = map["description"] as String
            val styleId = map["styleId"] as String
            return Placemark(name, description, location, styleId)
        }
    }
}