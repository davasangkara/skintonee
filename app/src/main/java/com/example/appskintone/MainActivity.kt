// MainActivity.kt
package com.example.appskintone

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appskintone.ml.ImageClassifier
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var imageClassifier: ImageClassifier
    private lateinit var imageView: ImageView
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageClassifier = ImageClassifier(assets)
        imageView = findViewById(R.id.imageView)
        textViewResult = findViewById(R.id.textView_result)

        val buttonSelectImage: Button = findViewById(R.id.button_select_image)
        val buttonCaptureImage: Button = findViewById(R.id.button_capture_image)
        val buttonHistory: Button = findViewById(R.id.button_history)

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        buttonCaptureImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        buttonHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val originalBitmap: Bitmap? = when (requestCode) {
                REQUEST_IMAGE_PICK -> data?.data?.let { uri ->
                    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }
                REQUEST_IMAGE_CAPTURE -> data?.extras?.get("data") as? Bitmap
                else -> null
            }

            if (originalBitmap != null) {
                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)
                imageView.setImageBitmap(resizedBitmap)
                detectFaceAndClassify(resizedBitmap)
            } else {
                textViewResult.text = "Kesalahan: Gagal mengambil gambar."
            }
        }
    }

    private fun detectFaceAndClassify(bitmap: Bitmap) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

        val faceDetector = FaceDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    textViewResult.text = "Tidak ada wajah terdeteksi. Gunakan gambar wajah."
                    Toast.makeText(this, "Gagal: Tidak ada wajah terdeteksi.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                try {
                    val inputBuffer = convertBitmapToByteBuffer(bitmap)
                    val (result, score) = imageClassifier.classifyImage(inputBuffer)

                    if (result == "Error") {
                        textViewResult.text = "Kesalahan: Model gagal mengenali gambar."
                        return@addOnSuccessListener
                    }

                    showResultDialog(result, score)
                } catch (e: Exception) {
                    Log.e("TFLITE", "Error saat klasifikasi: ${e.message}")
                    textViewResult.text = "Kesalahan saat klasifikasi: ${e.message}"
                }
            }
            .addOnFailureListener { e ->
                textViewResult.text = "Gagal mendeteksi wajah: ${e.message}"
                Log.e("MLKIT", "Deteksi wajah gagal: ${e.message}")
            }
    }

    private fun showResultDialog(label: String, score: Float) {
        val cocokLevel = when {
            score > 0.85f -> "Sangat Cocok"
            score > 0.6f -> "Cocok"
            else -> "Kurang Cocok"
        }

        val formattedScore = String.format("%.2f", score * 100)

        AlertDialog.Builder(this)
            .setTitle("Hasil Klasifikasi")
            .setMessage("Kategori: $label\nAkurasi: $formattedScore%\nRekomendasi: $cocokLevel")
            .setPositiveButton("Simpan ke Riwayat") { _, _ ->
                saveToHistory(label, score, cocokLevel)
            }
            .setNegativeButton("Deteksi Ulang") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun saveToHistory(label: String, score: Float, rekomendasi: String) {
        val sharedPreferences = getSharedPreferences("history_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val waktu = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            .format(java.util.Date())

        val formattedScore = String.format("%.2f", score * 100)
        val entry = "$waktu|$label|$formattedScore|$rekomendasi"

        val historySet = sharedPreferences.getStringSet("history_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        historySet.add(entry)
        editor.putStringSet("history_list", historySet)
        editor.apply()

        Toast.makeText(this, "Disimpan ke riwayat", Toast.LENGTH_SHORT).show()
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        intValues.forEach { pixelValue ->
            inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
        return inputBuffer
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }
}