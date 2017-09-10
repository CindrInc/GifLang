package com.cindrinc.giflang

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton


class MainActivity : AppCompatActivity(), RecognitionListener {

	private lateinit var toggleButton : ToggleButton
	private lateinit var textView : TextView
	private lateinit var progressBar : ProgressBar

	private lateinit var speech : SpeechRecognizer
	private lateinit var recognizerIntent : Intent
	private val LOG_TAG = "VoiceRecogActivity"

	private var isHearing = false



	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		textView = findViewById(R.id.textbox) as TextView
		toggleButton = findViewById(R.id.toggleButton) as ToggleButton
		progressBar = findViewById(R.id.progressbar) as ProgressBar


		progressBar.visibility = View.INVISIBLE

		speech = SpeechRecognizer.createSpeechRecognizer(this)
		speech.setRecognitionListener(this)

		recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en")
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

		toggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
			if (isChecked) {
				progressBar.visibility = View.VISIBLE
				progressBar.isIndeterminate = true
				speech.startListening(recognizerIntent)
			} else {
				progressBar.isIndeterminate = false
				progressBar.visibility = View.INVISIBLE
				speech.stopListening()
			}
			isHearing = !isHearing
//			textView.setText("Google is your friend.", TextView.BufferType.EDITABLE)
		}
	}

	public override fun onResume() {
		super.onResume()
	}

	override fun onPause() {
		super.onPause()
		if (speech != null) {
			speech.destroy()
			Log.i(LOG_TAG, "destroy")
		}

	}

	override fun onBeginningOfSpeech() {
		Log.i(LOG_TAG, "onBeginningOfSpeech")
		progressBar.isIndeterminate = false
		progressBar.max = 10
	}

	override fun onBufferReceived(buffer: ByteArray) {
		Log.i(LOG_TAG, "onBufferReceived: " + buffer)
	}

	override fun onEndOfSpeech() {
		Log.i(LOG_TAG, "onEndOfSpeech")
		progressBar.isIndeterminate = true
		toggleButton.setChecked(false)
	}

	override fun onError(errorCode: Int) {
		val errorMessage = getErrorText(errorCode)
		Log.d(LOG_TAG, "FAILED " + errorMessage)
		textView.setText(errorMessage)
		toggleButton.setChecked(false)
	}

	override fun onEvent(arg0: Int, arg1: Bundle) {
		Log.i(LOG_TAG, "onEvent")
	}

	override fun onPartialResults(arg0: Bundle) {
		Log.i(LOG_TAG, "onPartialResults")
	}

	override fun onReadyForSpeech(arg0: Bundle) {
		Log.i(LOG_TAG, "onReadyForSpeech")
	}

	override fun onResults(results: Bundle) {
		Log.i(LOG_TAG, "onResults")
		val matches = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
		var text = ""
		for (result in matches)
			text += result + "\n"

		textView.setText(text)
	}

	override fun onRmsChanged(rmsdB: Float) {
		Log.i(LOG_TAG, "onRmsChanged: " + rmsdB)
		progressBar.progress = rmsdB.toInt()
	}

	fun getErrorText(errorCode: Int): String {
		val message: String
		when (errorCode) {
			SpeechRecognizer.ERROR_AUDIO -> message = "Audio recording error"
			SpeechRecognizer.ERROR_CLIENT -> message = "Client side error"
			SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "Insufficient permissions"
			SpeechRecognizer.ERROR_NETWORK -> message = "Network error"
			SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "Network timeout"
			SpeechRecognizer.ERROR_NO_MATCH -> message = "No match"
			SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "RecognitionService busy"
			SpeechRecognizer.ERROR_SERVER -> message = "error from server"
			SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "No speech input"
			else -> message = "Didn't understand, please try again."
		}
		return message
	}
}
