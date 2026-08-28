package com.idealista.challenge.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idealista.challenge.databinding.ItemStatCardBinding

/** Renders the detail screen's characteristics as a 2-column grid of stat cards. */
class CharacteristicsAdapter : ListAdapter<String, CharacteristicsAdapter.StatViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemStatCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatViewHolder(private val binding: ItemStatCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            (binding.root as TextView).text = text
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
