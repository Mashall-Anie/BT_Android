package vn.edu.tlu.audiorecordapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var recordButton: Button
    private lateinit var playButton: Button
    private lateinit var recordingsList: ListView

    private var audioFilePath: String = ""
    private val RECORD_AUDIO_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        playButton = findViewById(R.id.playButton)
        recordingsList = findViewById(R.id.recordingsList)

        // Kiểm tra và yêu cầu quyền
        if (!checkAudioPermissions()) {
            requestAudioPermissions()
        }

        recordButton.setOnClickListener {
            if (mediaRecorder == null) {
                startRecording()
            } else {
                stopRecording()
            }
        }

        playButton.setOnClickListener {
            if (audioFilePath.isNotEmpty()) {
                playRecording()
            }
        }

        // Tải danh sách các bản ghi
        loadRecordings()
    }

    private fun checkAudioPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            RECORD_AUDIO_PERMISSION_CODE
        )
    }

    private fun startRecording() {
        val fileName = "recording_${System.currentTimeMillis()}.mp3"
        val storageDir = getExternalFilesDir(null)

        try {
            val file = File(storageDir, fileName)
            audioFilePath = file.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }

            recordButton.text = "Dừng Ghi Âm"
            Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi ghi âm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        // Thêm file vào MediaStore
        addAudioToMediaStore(audioFilePath)

        recordButton.text = "Bắt Đầu Ghi Âm"
        Toast.makeText(this, "Ghi âm hoàn tất", Toast.LENGTH_SHORT).show()

        // Tải lại danh sách bản ghi
        loadRecordings()
    }

    private fun addAudioToMediaStore(filePath: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, File(filePath).name)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings")
        }

        contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun playRecording() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
            Toast.makeText(this, "Đang phát...", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi phát âm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecordings() {
        val projection = arrayOf(
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )

        val recordings = mutableListOf<String>()
        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            while (it.moveToNext()) {
                val name = it.getString(nameColumn)
                recordings.add(name)
            }
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            recordings
        )
        recordingsList.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền đã được cấp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Cần cấp quyền để ghi âm", Toast.LENGTH_SHORT).show()
            }
        }
    }
}