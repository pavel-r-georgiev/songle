package me.pavelgeorgiev.songle

import android.util.Log
import android.util.Xml
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * Parses song list data from XML
 */
class SongsXmlParser {
//    We don't use namespace
    private val ns = null
    private val SONGS_TAG = "Songs"
    private val SONG_TAG = "Song"
    private val NUMBER_TAG = "Number"
    private val ARTIST_TAG = "Artist"
    private val TITLE_TAG = "Title"
    private val LINK_TAG = "Link"
    private val TIMESTAMP_TAG = "timestamp"
    private val TAG = "SongXmlParser"

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input : InputStream): List<Song> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            return readSongs(parser)
        }
     }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSongs(parser: XmlPullParser): List<Song> {
        val songs = ArrayList<Song>()

        parser.require(XmlPullParser.START_TAG, ns, SONGS_TAG)
        val timestamp: String
        val dbReference =  FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)

        if(parser.name == SONGS_TAG){
            timestamp = parser.getAttributeValue(null, TIMESTAMP_TAG)


//          Check if database timestamp is same
            dbReference.addValueEventListener(object: ValueEventListener {
                        override fun onCancelled(databaseError: DatabaseError?) {
                            Log.w(TAG, "loadTimestamp:onCancelled", databaseError?.toException())
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists() && snapshot.child("last-updated").value == timestamp) {
                                    snapshot.child("song-list").children.mapTo(songs) { Song(it.value as HashMap<String, Any>) }
                                } else {
                                    dbReference.child("last-updated").setValue(timestamp)
                                }
                        }
                    })
        }

        if(!songs.isEmpty()){
            return songs
        }

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when(parser.name){
                SONG_TAG -> songs.add(readSong(parser))
                else -> skip(parser)
            }

        }

        dbReference.child("song-list").setValue(songs.toList())
        return songs
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser): Song {
        parser.require(XmlPullParser.START_TAG, ns, SONG_TAG)

        var number = ""
        var artist = ""
        var title = ""
        var link = ""

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }

            when(parser.name){
                NUMBER_TAG -> number = readTagText(parser, NUMBER_TAG)
                ARTIST_TAG -> artist = readTagText(parser, ARTIST_TAG)
                TITLE_TAG -> title = readTagText(parser, TITLE_TAG)
                LINK_TAG -> link = readTagText(parser, LINK_TAG)
                else -> skip(parser)
            }
        }
        return Song(number, artist, title, link)
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