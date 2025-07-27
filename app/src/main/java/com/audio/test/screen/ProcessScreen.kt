package com.audio.test.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessScreen(
    rawText: String?
) {
    val inputText by remember { mutableStateOf(rawText ?: "") }
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val coroutineScope = rememberCoroutineScope()

    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    var currentLineIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val annotatedText = remember(inputText, spokenText) {
        buildAnnotatedString {
            val originalWords = inputText.split(" ")
            val spokenWords = spokenText.split(" ")

            originalWords.forEachIndexed { index, word ->
                val match =
                    index < spokenWords.size && word.equals(
                        spokenWords[index],
                        ignoreCase = true
                    )
                withStyle(style = SpanStyle(color = if (match) Color.Green else Color.Red)) {
                    append("$word ")
                }
            }
        }
    }

    val lines: List<String> = getLines(rawText)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My App") },
                actions = {
                    IconButton(onClick = {
                        if (!isListening) {
                            isListening = true
                            spokenText = ""

                            coroutineScope.launch(Dispatchers.IO) {
                                try {


                                    val modelPath = File(context.filesDir, "model-en-us")
                                    copyAssetsFolder(context, "model-en-us", modelPath)

                                    Log.d(
                                        "VOSK",
                                        "-------->" + modelPath.absolutePath + "<---------"
                                    )

                                    val model = Model(modelPath.absolutePath)


                                    val recognizer = Recognizer(model, 16000f)
                                    recognizer.setWords(true)
                                    val audioRecord = AudioRecord(
                                        MediaRecorder.AudioSource.MIC,
                                        16000,
                                        AudioFormat.CHANNEL_IN_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT,
                                        AudioRecord.getMinBufferSize(
                                            16000,
                                            AudioFormat.CHANNEL_IN_MONO,
                                            AudioFormat.ENCODING_PCM_16BIT
                                        )
                                    )

                                    audioRecord.startRecording()

                                    val buffer = ByteArray(4096)
                                    while (isListening) {

                                        val read = audioRecord.read(buffer, 0, buffer.size)

                                        if (recognizer.acceptWaveForm(buffer, read)) {

                                            val resultJson = recognizer.result
                                            val resultText =
                                                JSONObject(resultJson).getString("text")

                                            Log.d("VOSK", "RESULT ---> " + resultJson)
                                            withContext(Dispatchers.Main) {
                                                spokenText = resultText
                                            }
                                            currentLineIndex += 1
                                        } else {
                                            val partial =
                                                JSONObject(recognizer.partialResult).getString("partial")
                                            Log.d("VOSK", "PARTIAL  ---> " + partial)

                                            withContext(Dispatchers.Main) {
                                                spokenText = partial
                                            }
                                        }
                                    }

                                    audioRecord.stop()
                                    audioRecord.release()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            }
                        } else {
                            isListening = false
                        }
                    }) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Speak"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(Modifier.padding(padding)) {
            Spacer(modifier = Modifier.height(26.dp))
            Text(spokenText, style = MaterialTheme.typography.displaySmall)
        }




        LaunchedEffect(currentLineIndex) {
            listState.animateScrollToItem(currentLineIndex)
        }




        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = padding,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(lines) { index, line ->
                if (index == 0) {
                    Spacer(modifier = Modifier.height(46.dp))
                }
                Text(
                    text = line,
                    style = if (index == currentLineIndex) {
                        TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    } else {
                        TextStyle(fontSize = 18.sp, color = Color.Gray)
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        // }
    }


}

/**
 * this is a text, other. other. other.
 */


fun main() {
   val a =  listOf(
         "notex",
        "Lorem ipsum dolor sit amet.",
        "Ea atque quia et quasi deserunt eum consequatur consequuntur et vero sint et fuga totam!",
        "Ut alias excepturi et accusantium quas et quae corrupti est aspernatur natus? ",
        "Qui beatae dolorem qui ullam omnis aut perferendis distinctio ex nihil consequatur est distinctio nulla 33 molestiae velit hic rerum deserunt.",
        "Id velit magnam a quos consectetur non delectus molestias aut asperiores quibusdam et adipisci molestias aut alias aliquam.",
        "Ex dolorem voluptatum non laudantium voluptatum aut internos perspiciatis sed maiores accusamus et eveniet illo aut suscipit laborum est consequuntur laborum.",
        "Eum suscipit provident eos rerum rerum et earum dolore! ",
        "Qui eius dolorem ab perspiciatis consequatur et Quis quod eos repellat porro nam nisi veniam et quia veritatis.",
        "Eum suscipit provident eos rerum rerum et earum dolore! ",
        "Qui eius dolorem ab perspiciatis consequatur et Quis quod eos repellat porro nam nisi veniam et quia veritatis.",
        " Et sunt Quis a ducimus voluptates est veritatis quisquam hic iure debitis et incidunt nulla."
    )

   val b =  getLines(  "Ex dolorem voluptatum non laudantium voluptatum aut internos perspiciatis, sed maiores accusamus et eveniet illo aut suscipit laborum est consequuntur laborum.",
    )

    println(b)
    println(b.size)
}
fun getLines(rawText: String?): List<String> {
    return rawText?.trim()?.splitToSequence(
        "\r\n",
        "\n",
        "\r",
        ",",
        ".",
        "?",
        "!",

        )?.filter { it.isNotBlank() }?.toList() ?: emptyList()


}

fun copyAssetsFolder(context: Context, assetFolderName: String, outputDir: File) {
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
