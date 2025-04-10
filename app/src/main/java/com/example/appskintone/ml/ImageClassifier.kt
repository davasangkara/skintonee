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
        try {
            interpreter = Interpreter(loadModelFile(assetManager, "model.tflite"))
            labelList = loadLabelList(assetManager, "labels.txt")

            if (labelList.isEmpty()) {
                throw RuntimeException("Error: labels.txt kosong atau tidak bisa dimuat.")
            }
        } catch (e: Exception) {
            Log.e("TFLITE", "Gagal memuat model atau label: ${e.message}")
            throw RuntimeException("Gagal memuat model atau label: ${e.message}")
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }


    fun classifyImage(inputBuffer: ByteBuffer): Pair<String, Float> {
        return try {
            val outputArray = Array(1) { FloatArray(labelList.size) }
            interpreter.run(inputBuffer, outputArray)

            // Debugging: Log hasil output dari model
            Log.d("TFLITE", "Output Model: ${outputArray[0].toList()}")

            val maxIndex = outputArray[0].indices.maxByOrNull { outputArray[0][it] } ?: return Pair("Error", 0.0f)
            val maxScore = outputArray[0][maxIndex]

            // Cek apakah indeks valid
            if (maxIndex < 0 || maxIndex >= labelList.size) {
                Log.e("TFLITE", "Error: Indeks hasil prediksi tidak valid ($maxIndex)")
                return Pair("Error", 0.0f)
            }

            val label = labelList[maxIndex] // Ambil label dari daftar

            Log.d("TFLITE", "Prediksi: $label dengan skor $maxScore")

            Pair(label, maxScore)
        } catch (e: Exception) {
            Log.e("TFLITE", "Error saat membaca output model: ${e.message}")
            Pair("Error", 0.0f)
        }
    }
}
