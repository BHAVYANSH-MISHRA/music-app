package com.example.musicplayerapp

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.ui.theme.MusicPlayerAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicPlayerAppTheme {
                Surface(color = Color.Transparent) {
                    MusicPlayerUI()
                }
            }
        }
    }
}

@Composable
fun MusicPlayerUI() {
    val context = LocalContext.current

    val albumArts = listOf(
        R.drawable.ic_music,
        R.drawable.ic_music2,
        R.drawable.ic_music3
    )
    val songList = listOf(
        R.raw.sample_music,
        R.raw.sample_music2,
        R.raw.sample_music3
    )
    val songTitles = listOf("Track 1", "Track 2", "Track 3")

    var currentSongIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(0) }

    var mediaPlayer: MediaPlayer? by remember {
        mutableStateOf(MediaPlayer.create(context, songList[currentSongIndex]))
    }

    // Release player when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    fun playSong(index: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, songList[index])
        mediaPlayer?.start()
        isPlaying = true
        totalDuration = mediaPlayer?.duration ?: 0

        mediaPlayer?.setOnCompletionListener {
            currentSongIndex = (currentSongIndex + 1) % songList.size
            playSong(currentSongIndex)
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            } else {
                it.start()
                isPlaying = true
            }
        }
    }

    fun playNext() {
        currentSongIndex = (currentSongIndex + 1) % songList.size
        playSong(currentSongIndex)
    }

    fun playPrevious() {
        currentSongIndex =
            if (currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
        playSong(currentSongIndex)
    }

    fun formatTime(millis: Int): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Auto update seek bar every second
    LaunchedEffect(mediaPlayer, isPlaying) {
        while (true) {
            if (isPlaying) {
                currentPosition = mediaPlayer?.currentPosition ?: 0
                totalDuration = mediaPlayer?.duration ?: 0
            }
            delay(1000L)
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1E2E), Color(0xFF23242B))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Album Art
            Image(
                painter = painterResource(id = albumArts[currentSongIndex]),
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .padding(8.dp)
            )

            // Track Title
            Text(
                text = songTitles[currentSongIndex],
                style = MaterialTheme.typography.h6.copy(color = Color.White),
                textAlign = TextAlign.Center
            )

            // Seekbar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { currentPosition = it.toInt() },
                    onValueChangeFinished = {
                        mediaPlayer?.seekTo(currentPosition)
                    },
                    valueRange = 0f..(totalDuration.coerceAtLeast(1)).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.caption,
                        color = Color.White
                    )
                    Text(
                        text = formatTime(totalDuration),
                        style = MaterialTheme.typography.caption,
                        color = Color.White
                    )
                }
            }

            // Player Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playPrevious() }) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = { togglePlayPause() }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                IconButton(onClick = { playNext() }) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
