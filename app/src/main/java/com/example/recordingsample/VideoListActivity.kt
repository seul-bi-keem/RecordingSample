package com.example.recordingsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoListActivity : AppCompatActivity() {

    private lateinit var viewModel: VideoListViewModel
    private var adapter : VideoListViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_list)

        val recyclerView : RecyclerView = findViewById(R.id.video_list_recyclerview)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@VideoListActivity)
            setHasFixedSize(true)
        }

        viewModel = ViewModelProvider(this)[VideoListViewModel::class.java]

        viewModel.getVideoFiles(this)

        viewModel.videoList.observe(this) {
            if (it != null) {
                Toast.makeText(this@VideoListActivity, "video ${it.size}개!!", Toast.LENGTH_SHORT).show()
                if (adapter == null) {
                    adapter = VideoListViewAdapter(this@VideoListActivity, it)
                    recyclerView.adapter = adapter
                } else {
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }
}