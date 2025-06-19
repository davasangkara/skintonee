package com.example.appskintone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var historyList: MutableList<String>,
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (String, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelTextView: TextView = itemView.findViewById(R.id.textView_history_label)
        val detailsTextView: TextView = itemView.findViewById(R.id.textView_history_details)
        val dateTextView: TextView = itemView.findViewById(R.id.textView_history_date)

        fun bind(
            item: String,
            position: Int,
            clickListener: (String) -> Unit,
            longClickListener: (String, Int) -> Unit
        ) {
            itemView.setOnClickListener { clickListener(item) }
            itemView.setOnLongClickListener {
                longClickListener(item, position)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        val parts = item.split("|")
        if (parts.size >= 4) {
            val date = parts[0]
            val label = parts[1]
            val score = parts[2]
            val recommendation = parts[3]

            holder.labelTextView.text = "Kategori: $label"
            holder.detailsTextView.text = "Akurasi: $score% | Rekomendasi: $recommendation"
            holder.dateTextView.text = date
        }

        holder.bind(item, position, onItemClick, onItemLongClick)
    }

    override fun getItemCount() = historyList.size
}
