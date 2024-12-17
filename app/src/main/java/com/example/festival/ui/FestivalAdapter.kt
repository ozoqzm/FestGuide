package com.example.festival.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.festival.data.FestivalEntity
import com.example.festival.databinding.ListItemBinding

class FestivalAdapter : RecyclerView.Adapter<FestivalAdapter.ItemHolder>() {
    var items: List<FestivalEntity>? = null

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = items?.get(position)

        holder.itemBinding.tvItem.text = item?.name // 축제 이름
        holder.itemBinding.tvPlace.text = item?.place // 장소

    }

    class ItemHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)
}