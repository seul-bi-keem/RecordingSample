package com.example.recordingsample

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    private val _videoList = MutableLiveData<ArrayList<String>>()
    val videoList: LiveData<ArrayList<String>> = _videoList

    fun getVideoFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoItemHashSet = HashSet<String>()
            val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME)

            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val cursor: Cursor? = context.contentResolver.query(collection, projection, null, null, null)
            try {
                if (cursor != null) {
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        do {
                            videoItemHashSet.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)))
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                _videoList.value = ArrayList(videoItemHashSet)
            }
        }
    }
}

