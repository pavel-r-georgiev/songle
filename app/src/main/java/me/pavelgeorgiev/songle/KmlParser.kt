package me.pavelgeorgiev.songle

import android.util.Xml
import com.google.android.gms.maps.model.LatLng
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class KmlParser {
    //    We don't use namespace
    private val ns = null
    private val DOCUMENT_TAG = "Document"
    private val PLACEMARK_TAG = "Placemark"
    private val NAME_TAG = "name"
    private val DESCRIPTION_TAG = "description"
    private val STYLE_URL_TAG = "styleUrl"
    private val POINT_TAG = "Point"
    private val COORDINATES_TAG = "coordinates"

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input : InputStream): HashMap<LatLng, Placemark> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
//            Skip xml and kml tags
            parser.nextTag()
            parser.nextTag()
            return readPlacemarks(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPlacemarks(parser: XmlPullParser): HashMap<LatLng, Placemark>{
        val placemarks = HashMap<LatLng, Placemark>()

        parser.require(XmlPullParser.START_TAG, ns, DOCUMENT_TAG)

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if(parser.name == PLACEMARK_TAG){
                val placemark = readPlacemark(parser)
                placemarks.put(LatLng(placemark.lat, placemark.lng), placemark)
            } else {
                skip(parser)
            }
        }
        return placemarks
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPlacemark(parser: XmlPullParser): Placemark {
        parser.require(XmlPullParser.START_TAG, ns, PLACEMARK_TAG)

        var name = ""
        var description = ""
        var styleUrl = ""
        var coordinates = ""

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }

            when(parser.name){
                NAME_TAG -> name = readTagText(parser, NAME_TAG)
                DESCRIPTION_TAG -> description = readTagText(parser, DESCRIPTION_TAG)
                STYLE_URL_TAG -> styleUrl = readTagText(parser, STYLE_URL_TAG)
                POINT_TAG -> coordinates = readCoordinates(parser)
                else -> skip(parser)
            }
        }

        val mapCoordinates = coordinates.split(",").map { it.toDouble() }
        return Placemark(name, description, styleUrl, mapCoordinates[0], mapCoordinates[1])
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTagText(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return result
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCoordinates(parser: XmlPullParser): String{
        parser.require(XmlPullParser.START_TAG, ns, POINT_TAG)

        var coordinates = ""

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }

            when(parser.name){
                COORDINATES_TAG -> coordinates = readTagText(parser, COORDINATES_TAG)
                else -> skip(parser)
            }
        }

        return coordinates
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}