package com.audio.test.data.repository

import android.content.Context
import com.audio.test.domain.repository.SpeechRecognitionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoskSpeechRecognitionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechRecognitionRepository {

    private var speechService: SpeechService? = null
    private var model: Model? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    override suspend fun initialize() {
        if (model != null) return
        withContext(Dispatchers.IO) {

            val modelPath = File(context.filesDir, "model-en-us")
            if (!modelPath.exists()) {
                copyAssetsFolder(context, "model-en-us", modelPath)
            }
            model = Model(modelPath.absolutePath)
        }
    }

    override fun startListening(): Flow<WordProcess> = callbackFlow {
        if (model == null) {
            initialize()
        }

        val recognizer = Recognizer(model, 16000.0f)
        speechService = SpeechService(recognizer, 16000.0f)

        val listener = object : RecognitionListener {
            override fun onPartialResult(hypothesis: String?) {
                hypothesis?.let {
                    val text = parseVoskResult(it, "partial")
                    if (!text.isEmpty()) {
                        println("SENDING onPartialResult [$text]")
                        coroutineScope.launch { send(WordProcess.Partial(text)) }
                    }
                }
            }

            override fun onResult(hypothesis: String?) {
                hypothesis?.let {
                    val text = parseVoskResult(it, "text")
                    if (!text.isEmpty()) {
                        println("SENDING onResult [$text]")
                        coroutineScope.launch { send(WordProcess.Final(text)) }
                    }

                }
            }

            override fun onFinalResult(hypothesis: String?) {}

            override fun onError(onError: Exception?) {
                println("onError::${onError?.message}")

                close(onError)
            }

            override fun onTimeout() {
                println("onTimeout::")

                // Handle timeout if needed
            }
        }

        speechService?.startListening(listener)

        awaitClose {
            speechService?.stop()
            speechService?.shutdown()
            speechService = null
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun stopListening() {
        withContext(Dispatchers.Main) { // SpeechService methods often need to be called from main or specific threads, but Vosk is flexible. Safe to stop.
            speechService?.stop()
            speechService = null
        }
    }

    private fun parseVoskResult(json: String, key: String): String {
        return try {
            JSONObject(json).optString(key, "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun copyAssetsFolder(context: Context, assetFolderName: String, outputDir: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetFolderName) ?: return

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        for (filename in files) {
            val assetPath = "$assetFolderName/$filename"
            val outFile = File(outputDir, filename)

            val subFiles = assetManager.list(assetPath)
            if (subFiles?.isNotEmpty() == true) {
                copyAssetsFolder(context, assetPath, outFile)
            } else {
                if (!outFile.exists()) {
                    assetManager.open(assetPath).use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }
}
