package com.example.geofencing.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geofencing.databinding.ItemBinding

class MainAdapter(var items: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is ItemViewHolder) {
            holder.tv_title.text = item
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(bn: ItemBinding) : RecyclerView.ViewHolder(bn.root) {
        var tv_title: TextView

        init {
            tv_title = bn.tvText
        }
    }
}