package me.pavelgeorgiev.songle

import android.content.Context
import android.util.Xml
import com.google.android.gms.maps.model.LatLng
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * Parser for the KML data of songs
 */
class KmlParser {
    //    We don't use namespace
    private val ns = null
    private val DOCUMENT_TAG = "Document"
    private val STYLE_TAG = "Style"
    private val ICON_STYLE_TAG = "IconStyle"
    private val ICON_TAG = "Icon"
    private val SCALE_TAG = "scale"
    private val HREF_TAG = "href"
    private val PLACEMARK_TAG = "Placemark"
    private val NAME_TAG = "name"
    private val DESCRIPTION_TAG = "description"
    private val STYLE_URL_TAG = "styleUrl"
    private val POINT_TAG = "Point"
    private val COORDINATES_TAG = "coordinates"
    private lateinit var mContext: Context
    private val mStyles = HashMap<String, KmlStyle>()

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input : InputStream, context: Context): HashMap<String, Placemark>  {
        mContext = context
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
    private fun readPlacemarks(parser: XmlPullParser): HashMap<String, Placemark>  {
        val placemarks = HashMap<String, Placemark> ()

        parser.require(XmlPullParser.START_TAG, ns, DOCUMENT_TAG)

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when {
                parser.name == PLACEMARK_TAG -> {
                    val placemark = readPlacemark(parser)
                    placemarks.put(placemark.name, placemark)
                }
                parser.name == STYLE_TAG -> {
                    val style = readStyle(parser)
                    mStyles.put(style.id, style)
                }
                else -> skip(parser)
            }
            }
        return placemarks
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readStyle(parser: XmlPullParser): KmlStyle {
        parser.require(XmlPullParser.START_TAG, ns, STYLE_TAG)
        val style = KmlStyle(parser.getAttributeValue(null, "id"))

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }

            when(parser.name){
                ICON_STYLE_TAG -> readIconStyle(parser, style)
                else -> skip(parser)
            }
        }

        return style
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readIconStyle(parser: XmlPullParser, style: KmlStyle) {
        parser.require(XmlPullParser.START_TAG, ns, ICON_STYLE_TAG)

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }

            when(parser.name){
                SCALE_TAG -> style.scale = readTagText(parser, SCALE_TAG).toFloat()
                ICON_TAG -> style.iconUrl = readIconHref(parser, style)
                else -> skip(parser)
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readIconHref(parser: XmlPullParser, style: KmlStyle): String {
        parser.require(XmlPullParser.START_TAG, ns, ICON_TAG)
        var href = ""

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }

            when(parser.name){
                HREF_TAG -> href = readTagText(parser, HREF_TAG)
                else -> skip(parser)
            }
        }

        return href
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
        val styleID = styleUrl.substring(1)
        return Placemark(
                name,
                description,
                LatLng(mapCoordinates[1], mapCoordinates[0]),
                styleID)
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