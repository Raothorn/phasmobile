package com.example.phasmobile.ui.emf

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.phasmobile.R
import com.example.phasmobile.util.MainViewModel
import kotlinx.coroutines.launch

private const val TAG="Interface"
class Emf : Fragment() {

    private var emf = 0
    val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView: ImageView = view.findViewById(R.id.ivEmf)
        imageView.setImageResource(R.drawable.emf_reader_base)


        val files = arrayOf(R.raw.emf1, R.raw.emf2, R.raw.emf3, R.raw.emf4, R.raw.emf5)
        val mediaPlayers: Array<MediaPlayer> = Array(5) {MediaPlayer.create(context, files[it]) }
        for (player in mediaPlayers) {
            player.isLooping = true
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (uiState.gamestate?.emfLevel != emf) {
                    emf = uiState.gamestate?.emfLevel ?: 0

                    val image = when (emf) {
                        1 -> R.drawable.emf_reader_1
                        2 -> R.drawable.emf_reader_2
                        3 -> R.drawable.emf_reader_3
                        4 -> R.drawable.emf_reader_4
                        5 -> R.drawable.emf_reader_5
                        else -> R.drawable.emf_reader_base
                    }

                    for (mediaPlayer in mediaPlayers) {
                        if (mediaPlayer.isPlaying) mediaPlayer.pause()
                    }
                    if (emf > 0) {
                        // Stop all media players

                        val player = mediaPlayers[emf-1];

                        mediaPlayers[emf - 1].isLooping = true
                        mediaPlayers[emf - 1].start()
                    }

                    imageView.setImageResource(image)
                }
            }
        }
    }

}