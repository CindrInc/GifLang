package com.cindrinc.giflang

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

class MainActivity : AppCompatActivity(), RecognitionListener {

	private lateinit var fab : FloatingActionButton
	private lateinit var textView : TextView
	private lateinit var progressBar : ProgressBar
	private lateinit var speech : SpeechRecognizer
	private lateinit var recognizerIntent : Intent



	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		textView = findViewById(R.id.textbox) as TextView
		fab = findViewById(R.id.fab) as FloatingActionButton
		progressBar = findViewById(R.id.progressbar) as ProgressBar


		progressBar.visibility = View.INVISIBLE

		speech = SpeechRecognizer.createSpeechRecognizer(this)
		speech.setRecognitionListener(this)

		recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en")
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

		fab.setOnClickListener { view ->
//			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
			textView.setText("Google is your friend.", TextView.BufferType.EDITABLE)
		}
	}
}
