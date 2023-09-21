package com.example.recordingsample

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoListViewAdapter(private val context: Context, private val items : ArrayList<String>) : RecyclerView.Adapter<VideoListViewAdapter.VideoListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_list_layout, parent, false)
        return VideoListViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VideoListViewHolder, position: Int) {
        val textView : TextView = holder.itemView.findViewById(R.id.textview)
        textView.text = items[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(context, VideoPlayActivity::class.java)
            intent.putExtra("uri", items[position])
            context.startActivity(intent)
        }
    }

    inner class VideoListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
}