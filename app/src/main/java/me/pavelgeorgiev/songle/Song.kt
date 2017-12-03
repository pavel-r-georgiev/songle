package me.pavelgeorgiev.songle

import android.os.Parcel
import android.os.Parcelable
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * Data class containing all information about a class.
 * Implements Parcelable so it can be sent across activities
 */
data class Song(val number: String,
                val artist: String,
                val title: String,
                val link: String,
                var completed: Boolean = false,
                var difficultiesCompleted: ArrayList<String>? = ArrayList()): Parcelable{
    /**
     * Secondary contructor to build from parcel
     * @param parcel data for the song
     */
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.createStringArrayList())
    /**
     * Secondary contructor to build from database entry
     * @param map data for the song
     */
    constructor(map: HashMap<String, Any>) :
            this(
                    map["number"] as String,
                    map["artist"] as String,
                    map["title"] as String,
                    map["link"] as String,
                    map["completed"] as Boolean,
                    map["difficultiesCompleted"] as ArrayList<String>?
                )

    /**
     * Adds difficulty to the completed difficulties in the song
     * @param difficulty String representing the difficulty completed
     */
    fun addCompletedDifficulty(difficulty: String){
        if(!difficultiesCompleted!!.contains(difficulty)){
            difficultiesCompleted!!.add(difficulty)
        }
    }

    /**
     * Writes Song object to a Parcel
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(number)
        parcel.writeString(artist)
        parcel.writeString(title)
        parcel.writeString(link)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeStringList(difficultiesCompleted)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}