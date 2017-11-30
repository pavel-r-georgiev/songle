package me.pavelgeorgiev.songle

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_difficulty.*

class DifficultyActivity : AppCompatActivity(){
    private lateinit var mSongNumber: String
    private lateinit var mSongTitle: String
    private lateinit var mDifficultiesList: ListView
    private lateinit var mSong: Song

//    Map versions depending on the difficulty
    private val EASY = "5"
    private val MEDIUM = "4"
    private val HARD = "3"
    private val VERY_HARD = "2"
    private val IMPOSSIBLE = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty)

        mSong = intent.getParcelableExtra(getString(R.string.intent_song_object))
        mSongNumber = mSong.number
        mSongTitle = mSong.title

        val difficulties = constructDifficulties(mSongNumber, mSongTitle)
        val adapter = DifficultyAdapter(this, R.layout.difficulty_list_item, difficulties, mSong)
        mDifficultiesList = difficulties_list as ListView
        mDifficultiesList.adapter = adapter

    }

    private fun constructDifficulties(songNumber: String, songTitle: String): List<Difficulty>{
        val easy = Difficulty(
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_easy_description),
                EASY,
                R.drawable.easy)

        val medium = Difficulty(
                getString(R.string.difficulty_medium),
                getString(R.string.difficulty_medium_description),
                MEDIUM,
                R.drawable.medium)

        val hard = Difficulty(
                getString(R.string.difficulty_hard),
                getString(R.string.difficulty_hard_description),
                HARD,
                R.drawable.hard)


        val veryHard = Difficulty(
                getString(R.string.difficulty_very_hard),
                getString(R.string.difficulty_very_hard_description),
                VERY_HARD,
                R.drawable.very_hard)

        val impossible = Difficulty(
                getString(R.string.difficulty_impossible),
                getString(R.string.difficulty_impossible_description),
                IMPOSSIBLE,
                R.drawable.impossible)


        return listOf(easy, medium, hard, veryHard, impossible)
    }
}