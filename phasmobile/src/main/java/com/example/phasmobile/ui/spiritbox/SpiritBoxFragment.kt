package com.example.phasmobile.ui.spiritbox

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.phasmobile.R
import com.google.android.material.button.MaterialButton
import kotlin.random.Random


private const val TAG = "SpiritBox"

class SpiritBoxFragment : Fragment() {
    private lateinit var speechRecognizer: SpeechRecognizer;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spirit_box, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val responseTxt: TextView = view.findViewById(R.id.tvResponse)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech begun")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d(TAG, "RmsChanged")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d(TAG, "BufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
            }

            override fun onError(error: Int) {
                Log.e(TAG, "ERROR: $error")
            }

            override fun onResults(results: Bundle?) {
                val data: List<String> =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.toArray()
                        ?.map { it.toString() } ?: emptyList()

                val result = chooseResponse(data)
                responseTxt.text = result

                playAudio(result)

                Handler(Looper.getMainLooper()).postDelayed({ responseTxt.text = "" }, 3000)
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        });

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE, "en-US"
        )

        val micButton: MaterialButton = view.findViewById(R.id.ivMic);

        micButton.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "ACTION_UP")
                speechRecognizer.stopListening()
            }
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "ACTION_DOWN")
                speechRecognizer.startListening(speechRecognizerIntent)
            }
            false
        }

    }

    fun chooseResponse(input: List<String>): String {
        for (alternative in input) {
            val response = chooseResponse(alternative)
            if (response != null) {
                return response
            }
        }

        return "E"
    }

    fun chooseResponse(input: String): String? {
        val input = input.lowercase()

        return if (input.contains("age") || input.contains("old")) {
            "ADULT"
        } else if (input.contains("want") || input.contains("sign") || input.contains("talk")) {
            val options = arrayOf("ATTACK", "DIE", "KILL")
            chooseRandom(options)
        } else if (input.contains("where") || input.contains("close") || input.contains("near") || input.contains("room")) {
            chooseRandom(arrayOf("BEHIND", "CLOSE", "HERE", "NEXT", "FAR"))
        } else if (input.contains("why")) {
            chooseRandom(arrayOf("DEATH", "DIE"))
        } else{
            null
        }
    }

    private fun chooseRandom(input: Array<String>): String {
        val ix = Random.nextInt(input.size)
        return input[ix]
    }

    private fun playAudio(input: String) {
        val file = when (input) {
            "ADULT" -> R.raw.sb_adult
            "ATTACK" -> R.raw.sb_attack
            "DIE" -> R.raw.sb_die
            "KILL" -> R.raw.sb_kill
            "BEHIND" -> R.raw.sb_behind
            "CLOSE" -> R.raw.sb_close
            "HERE" -> R.raw.sb_here
            "NEXT" -> R.raw.sb_next
            "FAR" -> R.raw.sb_far
            "DEATH" -> R.raw.sb_death
            else -> R.raw.sb_e
        }

        val mediaPlayer = MediaPlayer.create(context, file)
        mediaPlayer.start()
    }

}