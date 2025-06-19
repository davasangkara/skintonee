package com.example.appskintone.ml

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifier(assetManager: AssetManager) {

    private val interpreter: Interpreter
    private val labelList: List<String>

    init {
        interpreter = Interpreter(loadModelFile(assetManager, "model.tflite"))
        labelList = loadLabelList(assetManager, "labels.txt")
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    fun classifyImage(inputBuffer: ByteBuffer): Pair<String, Float> {
        return try {
            val output = Array(1) { FloatArray(labelList.size) }
            interpreter.run(inputBuffer, output)

            val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            if (maxIndex == -1) return Pair("Error", 0f)

            val label = labelList[maxIndex]
            val score = output[0][maxIndex]
            Pair(label, score)
        } catch (e: Exception) {
            Log.e("TFLITE", "Error: ${e.message}")
            Pair("Error", 0f)
        }
    }
}
