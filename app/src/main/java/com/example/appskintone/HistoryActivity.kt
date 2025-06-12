package com.example.appskintone

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.StringBuilder

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private var historyList = mutableListOf<String>()
    private var historySet = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Klasifikasi"

        sharedPreferences = getSharedPreferences("history_data", MODE_PRIVATE)
        recyclerView = findViewById(R.id.recyclerView_history)
        emptyView = findViewById(R.id.textView_empty_history)

        loadHistory()
        setupRecyclerView()
        checkIfEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadHistory() {
        historySet = sharedPreferences.getStringSet("history_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        historyList = historySet.toList().sortedDescending().toMutableList()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            historyList,
            // Aksi untuk klik biasa: panggil dialog detail
            onItemClick = { item ->
                showHistoryDetailDialog(item)
            },
            // Aksi untuk tekan-lama: panggil dialog hapus
            onItemLongClick = { item, position ->
                showDeleteConfirmationDialog(item, position)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter
    }

    // FUNGSI BARU: Untuk menampilkan detail saat item di-klik
    private fun showHistoryDetailDialog(historyEntry: String) {
        val parts = historyEntry.split("|")
        if (parts.size < 4) return // Keluar jika format data tidak valid

        val date = parts[0]
        val label = parts[1]
        val score = parts[2]
        val recommendation = parts[3]

        // Mendapatkan deskripsi berdasarkan label
        val description = getSkinToneDescription(label)

        // Membangun teks untuk ditampilkan di dialog
        val message = StringBuilder().apply {
            append("Kategori: $label\n\n")
            append("Akurasi: $score%\n")
            append("Rekomendasi: $recommendation\n\n")
            append("Deskripsi:\n$description\n\n")
            append("Tanggal Deteksi:\n$date")
        }.toString()

        AlertDialog.Builder(this)
            .setTitle("Detail Hasil Klasifikasi")
            .setMessage(message)
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // FUNGSI BARU: Menyediakan deskripsi untuk setiap kategori warna kulit
    private fun getSkinToneDescription(label: String): String {
        return when (label.lowercase()) {
            "light" -> "Warna kulit terang memiliki jumlah melanin yang lebih sedikit, membuatnya lebih rentan terhadap kerusakan akibat sinar matahari. Penggunaan tabir surya sangat dianjurkan."
            "fair" -> "Serupa dengan warna kulit terang, kategori 'fair' juga sensitif terhadap paparan UV. Perawatan kulit yang fokus pada hidrasi dan perlindungan UV adalah kunci."
            "medium" -> "Warna kulit medium atau sawo matang memiliki keseimbangan melanin yang baik, memberikan sedikit perlindungan alami terhadap matahari, namun tetap membutuhkan proteksi."
            "tan" -> "Warna kulit 'tan' atau kecoklatan memiliki lebih banyak melanin, membuatnya lebih tahan terhadap paparan sinar matahari, tetapi tetap berisiko mengalami hiperpigmentasi."
            // Tambahkan kategori lain jika ada di model Anda
            else -> "Tidak ada deskripsi yang tersedia untuk kategori ini."
        }
    }

    private fun showDeleteConfirmationDialog(itemToDelete: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                historySet.remove(itemToDelete)
                saveHistory()
                historyList.removeAt(position)
                historyAdapter.notifyItemRemoved(position)
                historyAdapter.notifyItemRangeChanged(position, historyList.size)
                checkIfEmpty()
                Toast.makeText(this, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveHistory() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("history_list", historySet)
        editor.apply()
    }

    private fun checkIfEmpty() {
        if (historyList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}