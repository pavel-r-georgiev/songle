package me.pavelgeorgiev.songle.Activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_difficulty.*
import me.pavelgeorgiev.songle.Adapters.DifficultyAdapter
import me.pavelgeorgiev.songle.Objects.Difficulty
import me.pavelgeorgiev.songle.R
import me.pavelgeorgiev.songle.Objects.Song

/**
 * Activity contains difficulty option for a particular song
 */
class DifficultyActivity : AppCompatActivity(){
    private lateinit var mSongNumber: String
    private lateinit var mSongTitle: String
    private lateinit var mDifficultiesList: ListView
    private lateinit var mSong: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty)

        mSong = intent.getParcelableExtra(getString(R.string.intent_song_object))
        mSongNumber = mSong.number
        mSongTitle = mSong.title

        val difficulties = constructDifficulties()
        val adapter = DifficultyAdapter(this, R.layout.difficulty_list_item, difficulties, mSong)
        mDifficultiesList = difficulties_list as ListView
        mDifficultiesList.adapter = adapter

    }

    private fun constructDifficulties(): List<Difficulty>{
        val easy = Difficulty(
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_easy_description),
                Difficulty.EASY.toString(),
                R.drawable.easy)

        val medium = Difficulty(
                getString(R.string.difficulty_medium),
                getString(R.string.difficulty_medium_description),
                Difficulty.MEDIUM.toString(),
                R.drawable.medium)

        val hard = Difficulty(
                getString(R.string.difficulty_hard),
                getString(R.string.difficulty_hard_description),
                Difficulty.HARD.toString(),
                R.drawable.hard)


        val veryHard = Difficulty(
                getString(R.string.difficulty_very_hard),
                getString(R.string.difficulty_very_hard_description),
                Difficulty.VERY_HARD.toString(),
                R.drawable.very_hard)

        val impossible = Difficulty(
                getString(R.string.difficulty_impossible),
                getString(R.string.difficulty_impossible_description),
                Difficulty.IMPOSSIBLE.toString(),
                R.drawable.impossible)


        return listOf(easy, medium, hard, veryHard, impossible)
    }
}