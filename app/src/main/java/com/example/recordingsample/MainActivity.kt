package com.example.recordingsample

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

    private var previewView: PreviewView? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector? = null
    private var preview: Preview? = null
    private var qualitySelector: QualitySelector? = null
    private var recorder: Recorder? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    private var mediaFileUri: Uri? = null

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        var allGranted = false

        for (item in it) {
            allGranted = allGranted or item.value
        }

        if (allGranted) {
            Toast.makeText(this@MainActivity, "all permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val captureListener = Consumer { videoRecordEvent: VideoRecordEvent? ->
        if (videoRecordEvent is Finalize) {
            val options = videoRecordEvent.outputOptions
            when (videoRecordEvent.error) {
                Finalize.ERROR_NONE -> {
                    mediaFileUri = videoRecordEvent.outputResults.outputUri
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // On Android <= 9 we need to update the display name after writing the data to ensure the DISPLAY_NAME is correctly set
                        val resolver: ContentResolver = contentResolver
                        newVideoDetails!!.clear()
                        newVideoDetails!!.put(MediaStore.Video.Media.DISPLAY_NAME, mediaFileName + ".mp4")
                        try {
                            resolver.update(mediaFileUri!!, newVideoDetails, null, null)
                        } catch (e: IllegalArgumentException) {
                        }
                    }
                    val newVideoDetails = ContentValues()
                    newVideoDetails.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                    contentResolver.update(mediaFileUri!!, newVideoDetails, null, null)
                }
                Finalize.ERROR_UNKNOWN, Finalize.ERROR_RECORDER_ERROR, Finalize.ERROR_ENCODING_FAILED, Finalize.ERROR_NO_VALID_DATA -> if (options is FileOutputOptions) {
                    options.file.delete()
                } else if (options is MediaStoreOutputOptions) {
                    val uri = videoRecordEvent.outputResults.outputUri
                    if (uri !== Uri.EMPTY) {
                        contentResolver.delete(uri, null, null)
                    }
                } else if (options is FileDescriptorOutputOptions) { // User has to clean up the referenced target of the file descriptor.
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_VIDEO))
        }

        previewView = findViewById(R.id.previewview)
        val start = findViewById<Button>(R.id.record_start)
        val stop = findViewById<Button>(R.id.record_stop)
        val rec = findViewById<TextView>(R.id.rec_text)
        val list = findViewById<Button>(R.id.record_list)

        preparePreview()

        start.setOnClickListener {
            rec.visibility = View.VISIBLE
            startRecording()
        }

        stop.setOnClickListener {
            rec.visibility = View.GONE
            stopRecording()
        }

        list.setOnClickListener {
            startActivity(Intent(this, VideoListActivity::class.java))
        }
    }

    private fun getMediaUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }

    private var blackBoxFileName: String = ""
    private var mediaFileName: String = ""
    private var newVideoDetails: ContentValues? = null
    private var mediaStoreOutputOptions: MediaStoreOutputOptions? = null


    private fun setOutputMediaFile() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREAN).format(Date())
        blackBoxFileName = timeStamp
        mediaFileName = "blackboxsampleapp_VID_$timeStamp"
        newVideoDetails = ContentValues()
        newVideoDetails!!.put(MediaStore.Video.Media.DISPLAY_NAME, "$mediaFileName.mp4")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newVideoDetails!!.put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/" + "blackboxsampleapp")
        }
        mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(contentResolver, getMediaUri()).setContentValues(newVideoDetails!!).build()
    }

    private fun startRecording() {
        setOutputMediaFile()
        try {
            val pendingRecording: PendingRecording = videoCapture!!.output.prepareRecording(this, mediaStoreOutputOptions!!)
            currentRecording = pendingRecording.start(ContextCompat.getMainExecutor(this), captureListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        currentRecording?.stop()
        currentRecording = null
    }

    private fun preparePreview() {
        bindCaptureUseCase()
    }

    private fun bindCaptureUseCase() {
        try {
            cameraProvider = ProcessCameraProvider.getInstance(this).get()
            cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            preview = Preview.Builder().build()
            preview!!.setSurfaceProvider(previewView!!.surfaceProvider)
            qualitySelector = QualitySelector.from(Quality.LOWEST)
            recorder = Recorder.Builder().setQualitySelector(qualitySelector!!).build()
            videoCapture = VideoCapture.withOutput(recorder!!)
            cameraProvider!!.unbindAll()
            cameraProvider!!.bindToLifecycle(this as LifecycleOwner, cameraSelector!!, videoCapture, preview)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}