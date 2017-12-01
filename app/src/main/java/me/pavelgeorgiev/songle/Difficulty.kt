package me.pavelgeorgiev.songle

data class Difficulty(val difficulty: String,
                      val description: String,
                      val mapVersion: String,
                      val iconResource: Int){
    companion object {
//    Map versions depending on the difficulty
        val EASY = 5
        val MEDIUM = 4
        val HARD = 3
        val VERY_HARD = 2
        val IMPOSSIBLE = 1

        fun fromMapVersion(mapVersion: Int): String{
           return when(mapVersion){
                EASY -> "Easy"
                MEDIUM -> "Medium"
                HARD -> "Hard"
                VERY_HARD -> "Very Hard"
                IMPOSSIBLE -> "Impossible"
                else -> ""
            }
        }
    }
}
