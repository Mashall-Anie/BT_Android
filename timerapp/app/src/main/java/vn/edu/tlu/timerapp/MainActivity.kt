package vn.edu.tlu.timerapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private var seconds = 0
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ TextView từ layout
        timeTextView = findViewById(R.id.timeTextView)

        // Bắt đầu thread đếm giờ bằng Coroutine
        startTimerCoroutine()
    }

    private fun startTimerCoroutine() {
        isRunning = true
        lifecycleScope.launch(Dispatchers.Default) {
            while (isRunning) {
                delay(1000) // Tương đương Thread.sleep(1000)

                // Tăng giá trị giây
                seconds++

                // Cập nhật UI trên Main Dispatcher
                withContext(Dispatchers.Main) {
                    timeTextView.text = "Thời gian đã trôi qua: $seconds giây"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dừng timer
        isRunning = false
    }
}