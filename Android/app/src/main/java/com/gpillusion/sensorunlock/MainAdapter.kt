package com.gpillusion.sensorunlock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class MainAdapter(private var list: ArrayList<Data>): RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var hrm: MaterialTextView = view.findViewById(R.id.hrm_text)
        var acc: MaterialTextView = view.findViewById(R.id.acc_text)
        var gyr: MaterialTextView = view.findViewById(R.id.gyr_text)
        var prs: MaterialTextView = view.findViewById(R.id.prs_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_main_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.hrm.text = list[position].hrm
        holder.acc.text = list[position].acc
        holder.gyr.text = list[position].gyr
        holder.prs.text = list[position].prs
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addText(data: Data) {
        list.add(data)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }
}