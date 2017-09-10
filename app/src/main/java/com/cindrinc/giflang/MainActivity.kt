package com.cindrinc.giflang

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import org.json.JSONObject
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.services.language.v1beta2.CloudNaturalLanguage
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse
import com.google.api.services.language.v1beta2.model.Document
import com.google.api.services.language.v1beta2.model.Features
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL


class MainActivity : AppCompatActivity(), RecognitionListener {

	private lateinit var toggleButton : ToggleButton
	private lateinit var textView : TextView
	private lateinit var progressBar : ProgressBar
	private lateinit var fab : FloatingActionButton

	private lateinit var speech : SpeechRecognizer
	private lateinit var recognizerIntent : Intent
	private val LOG_TAG = "VoiceRecogActivity"
	val naturalLanguageService = CloudNaturalLanguage.Builder(
			AndroidHttp.newCompatibleTransport(),
			AndroidJsonFactory(), null).setCloudNaturalLanguageRequestInitializer(
			CloudNaturalLanguageRequestInitializer("AIzaSyClg2LxhxHGWBy44Tsnn4_xIsovjpc7Uzg")).build()

	private var isHearing = false



	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)


		textView = findViewById(R.id.textbox) as TextView
		toggleButton = findViewById(R.id.toggleButton) as ToggleButton
		progressBar = findViewById(R.id.progressbar) as ProgressBar
		fab = findViewById(R.id.fab) as FloatingActionButton


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

		fab.setOnClickListener{v ->
			var features = Features()
			features.extractDocumentSentiment = true
			features.extractEntities = true
			var document =  Document()


			document.type = "PLAIN_TEXT"
			document.language = "en-US"
			document.content = textView.text.toString()


			val request = AnnotateTextRequest()
			request.document = document
			request.features = features

			AsyncTask.execute{
				var response : AnnotateTextResponse = naturalLanguageService.documents()
													.annotateText(request).execute()

				val sentiment = response.documentSentiment.score
				val entitiesList = response.entities

				runOnUiThread{
					Snackbar.make(v, sentiment.toString(), Snackbar.LENGTH_LONG)
							.setAction("Action", null).show()
				}
				var baseUrl = "http://api.giphy.com/v1/gifs/random?api_key=98e6c67ea7d342e48d859b91751e6bd8"
				var getGif = URL(baseUrl)

				var reader = BufferedReader(InputStreamReader(getGif.openStream()))
				var jsonText = readAll(reader)
				var jObj = JSONObject(jsonText)

				runOnUiThread{
					textView.text = jObj.getJSONObject("data").getString("fixed_height_small_url")
				}

				for(entity in entitiesList) {
					var inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
					var gif = inflater.inflate(R.layout.gif_view, findViewById(R.id.gifHolder) as ViewGroup)

					gif.set



				}
			}
		}
	}

	@Throws(IOException::class)
	private fun readAll(rd: Reader): String {
		val sb = StringBuilder()
		var cp: Int = rd.read()
		while (cp != -1) {
			sb.append(cp.toChar())
			cp = rd.read()
		}
		return sb.toString()
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

		textView.text = matches[0]
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
