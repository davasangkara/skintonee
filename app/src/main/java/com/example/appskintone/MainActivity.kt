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
import androidx.appcompat.app.AppCompatActivity
import com.example.appskintone.ml.ImageClassifier
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var imageClassifier: ImageClassifier
    private lateinit var imageView: ImageView
    private lateinit var textViewResult: TextView

    // Tentukan ambang batas untuk menentukan validitas prediksi
    private val THRESHOLD = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageClassifier = ImageClassifier(assets)
        imageView = findViewById(R.id.imageView)
        textViewResult = findViewById(R.id.textView_result)

        val buttonSelectImage: Button = findViewById(R.id.button_select_image)
        val buttonCaptureImage: Button = findViewById(R.id.button_capture_image)

        // Tombol untuk memilih gambar dari galeri
        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // Tombol untuk menangkap gambar dari kamera
        buttonCaptureImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            try {
                val originalBitmap: Bitmap? = when (requestCode) {
                    REQUEST_IMAGE_PICK -> data?.data?.let { uri ->
                        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    }
                    REQUEST_IMAGE_CAPTURE -> data?.extras?.get("data") as? Bitmap
                    else -> null
                }

                if (originalBitmap != null) {
                    // **Konversi gambar ke 224x224**
                    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)

                    imageView.setImageBitmap(resizedBitmap) // Tampilkan di UI
                    classifyImage(resizedBitmap) // Kirim ke model
                } else {
                    textViewResult.text = "Kesalahan: Gagal mengambil gambar."
                }
            } catch (e: Exception) {
                Log.e("TFLITE", "Error saat mengambil gambar: ${e.message}")
                textViewResult.text = "Kesalahan saat mengambil gambar: ${e.message}"
            }
        }
    }


    private fun classifyImage(bitmap: Bitmap) {
        try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

            val (result, score) = imageClassifier.classifyImage(inputBuffer)

            Log.d("TFLITE", "Hasil Prediksi: $result, Score: $score")

            if (result == "Error") {
                textViewResult.text = "Kesalahan: Model gagal mengenali gambar."
                return
            }

            val cocokLevel = if (score > 0.8f) "Sangat Cocok" else "Cocok"
            textViewResult.text = "Kategori : $result\nTingkat Kecocokan: $cocokLevel"
        } catch (e: Exception) {
            Log.e("TFLITE", "Error saat klasifikasi: ${e.message}")
            textViewResult.text = "Kesalahan saat klasifikasi: ${e.message}"
        }
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
