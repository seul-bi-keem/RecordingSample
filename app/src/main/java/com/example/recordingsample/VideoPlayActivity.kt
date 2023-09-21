package com.example.recordingsample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.VideoView

class VideoPlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        val uri = intent.getStringExtra("uri")
        val videoView : VideoView = findViewById(R.id.videoview)
        videoView.setVideoURI(Uri.parse(uri))
        videoView.requestFocus()
        videoView.start()
    }
}