package com.gpillusion.motionunlock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.gpillusion.motionunlock.data.SensorData

class MainAdapter(private var list: ArrayList<SensorData>): RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pos: MaterialTextView = view.findViewById(R.id.position)
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
        val pos = position + 1
        holder.pos.text = "#$pos"
        holder.hrm.text = list[position].hrm
        holder.acc.text = list[position].acc
        holder.gyr.text = list[position].gyr
        holder.prs.text = list[position].prs
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addText(sensorData: SensorData) {
        list.add(sensorData)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }
}