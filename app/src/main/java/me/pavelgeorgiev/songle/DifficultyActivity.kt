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
    private lateinit var mDifficultiesList: ListView

//    Map versions depending on the difficulty
    private val EASY = "5"
    private val MEDIUM = "4"
    private val HARD = "3"
    private val VERY_HARD = "2"
    private val IMPOSSIBLE = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty)

        mSongNumber = intent.getStringExtra(getString(R.string.intent_song_number))
        val difficulties = constructDifficulties(mSongNumber)
        val adapter = DifficultyAdapter(this, R.layout.difficulty_list_item, difficulties)
        mDifficultiesList = difficulties_list as ListView
        mDifficultiesList.adapter = adapter

    }

    private fun constructDifficulties(songNumber: String): List<Difficulty>{
        val easy = Difficulty(
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_easy_description),
                EASY,
                R.drawable.ic_sentiment_very_satisfied_black_24dp, songNumber)

        val medium = Difficulty(
                getString(R.string.difficulty_medium),
                getString(R.string.difficulty_easy_description),
                MEDIUM,
                R.drawable.ic_sentiment_satisfied_black_24dp, songNumber)

        val hard = Difficulty(
                getString(R.string.difficulty_hard),
                getString(R.string.difficulty_easy_description),
                HARD,
                R.drawable.ic_sentiment_neutral_black_24dp, songNumber)

        val veryHard = Difficulty(
                getString(R.string.difficulty_very_hard),
                getString(R.string.difficulty_easy_description),
                VERY_HARD,
                R.drawable.ic_sentiment_dissatisfied_black_24dp, songNumber)

        val impossible = Difficulty(
                getString(R.string.difficulty_impossible),
                getString(R.string.difficulty_easy_description),
                IMPOSSIBLE,
                R.drawable.ic_sentiment_very_dissatisfied_black_24dp, songNumber)

        return listOf(easy, medium, hard, veryHard, impossible)
    }
}