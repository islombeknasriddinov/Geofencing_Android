package com.example.geofencing.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geofencing.databinding.ItemBinding
import com.example.geofencing.manager.PrefsManager
import com.example.geofencing.model.Marker

class MainAdapter(context: Context, var items: ArrayList<Marker>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val prefsManager = PrefsManager.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items.reverse()
        val item = items[position]

        if (holder is ItemViewHolder) {
            holder.tvLatitude.text = "Latitude: ${item.latLng?.latitude}"
            holder.tvLongitude.text = "Longitude: ${item.latLng?.longitude}"

            holder.ivDelete.setOnClickListener {
                removeItem(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(bn: ItemBinding) : RecyclerView.ViewHolder(bn.root) {
        var tvLatitude: TextView
        var tvLongitude: TextView
        var ivDelete: ImageView

        init {
            tvLatitude = bn.tvLatitude
            tvLongitude = bn.tvLongitude
            ivDelete = bn.ivDelete
        }
    }

    private fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)
        saveNewLocation(items)
    }

    fun addLocation(marker: Marker) {
        if (items.contains(marker)) items.remove(marker)
        items.add(marker)
        saveNewLocation(items)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearHistory() {
        prefsManager?.removeWithKey(PrefsManager.KEY_LIST)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun saveNewLocation(list: ArrayList<Marker>) {
        prefsManager?.saveArrayList(PrefsManager.KEY_LIST, list)
        notifyDataSetChanged()
    }
}