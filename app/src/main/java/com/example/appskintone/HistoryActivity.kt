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
            onItemClick = { item -> showHistoryDetailDialog(item) },
            onItemLongClick = { item, position -> showDeleteConfirmationDialog(item, position) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter
    }

    private fun showHistoryDetailDialog(historyEntry: String) {
        val parts = historyEntry.split("|")
        if (parts.size < 4) return

        val date = parts[0]
        val label = parts[1]
        val score = parts[2]
        val recommendation = parts[3]

        val description = getSkinToneDescription(label)

        val message = StringBuilder().apply {
            append("Kategori: $label\n\n")
            append("Akurasi: $score%\n")
            append("Rekomendasi: $recommendation\n\n")
            append("Deskripsi Wajah:\n$description\n\n")
            append("Tanggal Deteksi:\n$date")
        }.toString()

        AlertDialog.Builder(this)
            .setTitle("Detail Hasil Klasifikasi")
            .setMessage(message)
            .setPositiveButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getSkinToneDescription(label: String): String {
        return when (label.lowercase()) {
            "light" -> "Kulit terang, sensitif terhadap sinar matahari. Cocok dengan warna makeup lembut."
            "mid-light" -> "Kulit cerah alami dengan rona kuning langsat. Cocok dengan makeup natural."
            "mid-dark" -> "Kulit cokelat keemasan. Fleksibel terhadap sinar matahari, cocok dengan makeup hangat."
            "dark" -> "Kulit gelap intens, tahan sinar matahari. Cocok dengan warna makeup kontras."
            else -> "Deskripsi tidak tersedia untuk kategori ini."
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
