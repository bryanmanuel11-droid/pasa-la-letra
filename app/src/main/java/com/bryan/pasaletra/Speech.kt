package com.bryan.pasaletra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Reconocimiento de voz de Android, en español de Colombia. */
class SpeechHelper(private val context: Context) {

    var onListening: (Boolean) -> Unit = {}
    var onResults: (List<String>) -> Unit = {}
    var onMessage: (String) -> Unit = {}

    private var recognizer: SpeechRecognizer? = null

    val available: Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        try {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                    it.setRecognitionListener(listener)
                }
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            onListening(true)
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            onListening(false)
            onMessage("No se pudo activar el micrófono. Puedes escribir la palabra.")
        }
    }

    fun stop() {
        try {
            recognizer?.stopListening()
        } catch (e: Exception) {
        }
    }

    fun destroy() {
        try {
            recognizer?.destroy()
        } catch (e: Exception) {
        }
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onError(error: Int) {
            onListening(false)
            onMessage(
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "No te entendí. Toca el micrófono y repite."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER ->
                        "La voz necesita conexión. Prueba otra vez o escribe la palabra."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Falta el permiso del micrófono."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        "El micrófono está ocupado. Espera un segundo."
                    else -> "No se pudo escuchar. Prueba otra vez."
                }
            )
        }

        override fun onResults(results: Bundle?) {
            onListening(false)
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
            if (list.isEmpty()) onMessage("No te entendí. Toca el micrófono y repite.") else onResults(list)
        }
    }
}
