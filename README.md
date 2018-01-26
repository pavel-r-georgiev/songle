# Songle

## Android location based app written in Kotlin.

Main objective is to guess songs from a list of preselected songs. To help your goal you can collecting words on the world map, mainly located around the University of Edinburgh central campus. Collecting a word reveals its location in the lyrics of the song.

Application uses Firebase Realtime Database and Authentication to guarantee enjoyable user experience and data persistance.


<img src="screenshots/login.png" alt="Login" style="width: 200px; margin: 20px"/>

<img src="screenshots/song_list.png" alt="Song List" style="width: 200px; margin: 20px"/>

<img src="screenshots/completed_songs.png" alt="Completed Songs" style="width: 200px; margin: 20px"/>

### Difficulties

The game has 5 difficulties, as seen below. With increase of difficulty the collect radius decreases and so does the total amount of available words on the map.

<img src="screenshots/difficulties.png" alt="Difficulties List" style="width: 200px; margin: 20px"/>

<img src="screenshots/map_view_1.png" alt="Map View" style="width: 200px; margin: 20px"/>

<img src="screenshots/guess_screen.png" alt="Map View" style="width: 200px; margin: 20px"/>

### Timer 

Last two difficulties include a timer. When the timer expires, all progress made is lost and the user has the option to either try the same song again or pick a different song. Word collection resets the timer.

When the user picks a difficulty, which has a timer, a brief explanation is showed of how the timer works.

Timer is being shown as a progress bar, near the bottom of the screen, on top of the words collected fragment.

<img src="screenshots/timer_info.png" alt="Timer Information" style="width: 200px; margin: 20px"/>

<img src="screenshots/timer.png" alt="Timer in Impossible difficulty" style="width: 200px; margin: 20px"/>

<img src="screenshots/timeout.png" alt="Timeout" style="width: 200px; margin: 20px"/>

### Guessing the song

Guessing the song is done through expanding the fragment at the bottom of the screen. There you can get a visual feedback of the lyrics you have collected and guess the name of the song. Once song is guessed you can find it in your "Completed Songs" screen and play it on Youtube.

<img src="screenshots/guess_fragment.png" alt="Guessing Screen" style="width: 200px; margin: 20px"/>

<img src="screenshots/guess_correct.png" alt="Correct Guess" style="width: 200px; margin: 20px"/>

<img src="screenshots/guess_wrong.png" alt="Wrong Guess" style="width: 200px; margin: 20px"/>


