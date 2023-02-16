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
import com.google.android.gms.maps.model.LatLng

class MainAdapter(context: Context, var items: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val prefsManager = PrefsManager.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items.reverse()
        val item = items[position]

        if (holder is ItemViewHolder) {
            holder.tvTitle.text = item

            holder.ivDelete.setOnClickListener {
                removeItem(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(bn: ItemBinding) : RecyclerView.ViewHolder(bn.root) {
        var tvTitle: TextView
        var ivDelete: ImageView

        init {
            tvTitle = bn.tvText
            ivDelete = bn.ivDelete
        }
    }

    private fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)
        saveNewLocation(items)
    }

    fun addLocation(location: LatLng) {
        val str = "Latitude: ${location.latitude} , Longitude: ${location.longitude}"
        if (items.contains(str)) items.remove(str)
        items.add(str)
        saveNewLocation(items)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearHistory() {
        prefsManager?.removeWithKey(PrefsManager.KEY_LIST)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun saveNewLocation(list: ArrayList<String>) {
        prefsManager?.saveArrayList(PrefsManager.KEY_LIST, list)
        notifyDataSetChanged()
    }
}