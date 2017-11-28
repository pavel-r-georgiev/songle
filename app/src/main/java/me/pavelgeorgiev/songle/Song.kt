package me.pavelgeorgiev.songle

import java.util.HashMap

data class Song(val number: String,
                val artist: String,
                val title: String,
                val link: String,
                var completed: Boolean = false,
                var difficultiesCompleted: ArrayList<String>? = ArrayList()){
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
        difficultiesCompleted?.add(difficulty)
    }
}