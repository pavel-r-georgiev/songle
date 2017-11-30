package me.pavelgeorgiev.songle

import android.os.Parcel
import android.os.Parcelable
import java.util.HashMap

data class Song(val number: String,
                val artist: String,
                val title: String,
                val link: String,
                var completed: Boolean = false,
                var difficultiesCompleted: ArrayList<String>? = ArrayList()): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.createStringArrayList())

    constructor(map: HashMap<String, Any>) :
            this(
                    map["number"] as String,
                    map["artist"] as String,
                    map["title"] as String,
                    map["link"] as String,
                    map["completed"] as Boolean,
                    map["difficultiesCompleted"] as ArrayList<String>?
                )

    fun addCompletedDifficulty(difficulty: String){
        if(!difficultiesCompleted!!.contains(difficulty)){
            difficultiesCompleted!!.add(difficulty)
        }
    }

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