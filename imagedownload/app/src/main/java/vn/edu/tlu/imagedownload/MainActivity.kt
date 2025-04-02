package vn.edu.tlu.imagedownload

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import vn.edu.tlu.imagedownload.R
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var urlEditText: EditText
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ các view
        urlEditText = findViewById(R.id.urlEditText)
        downloadButton = findViewById(R.id.downloadButton)
        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.imageView)

        // Xử lý sự kiện click nút tải
        downloadButton.setOnClickListener {
            val imageUrl = urlEditText.text.toString().trim()
            if (imageUrl.isNotEmpty()) {
                // Thực thi AsyncTask để tải ảnh
                ImageDownloadTask().execute(imageUrl)
            } else {
                Toast.makeText(this, "Vui lòng nhập URL ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // AsyncTask để tải ảnh
    inner class ImageDownloadTask : AsyncTask<String, Int, Bitmap?>() {
        // Được gọi trước khi bắt đầu tải
        override fun onPreExecute() {
            // Hiển thị progress bar
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0

            // Ẩn ảnh cũ
            imageView.setImageBitmap(null)
        }

        // Thực hiện tải ảnh trong background
        override fun doInBackground(vararg params: String): Bitmap? {
            val imageUrl = params[0]
            return try {
                // Mở kết nối và tải ảnh
                val connection = URL(imageUrl).openConnection()
                connection.connect()

                // Lấy kích thước của ảnh
                val totalSize = connection.contentLength

                // Mở input stream để đọc ảnh
                val input = connection.getInputStream()

                // Tải ảnh và cập nhật tiến độ
                val buffer = ByteArray(1024)
                var downloaded = 0
                var count: Int

                // Sử dụng ByteArrayOutputStream để lưu ảnh
                val output = java.io.ByteArrayOutputStream()

                // Đọc và ghi dữ liệu
                while (input.read(buffer).also { count = it } != -1) {
                    downloaded += count
                    output.write(buffer, 0, count)

                    // Tính và cập nhật phần trăm tải
                    if (totalSize > 0) {
                        val progress = (downloaded * 100) / totalSize
                        publishProgress(progress)
                    }
                }

                // Đóng các luồng
                input.close()
                output.close()

                // Chuyển đổi sang Bitmap
                BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size())
            } catch (e: IOException) {
                // Xử lý lỗi kết nối
                null
            }
        }

        // Cập nhật progress bar
        override fun onProgressUpdate(vararg values: Int?) {
            values[0]?.let {
                progressBar.progress = it
            }
        }

        // Được gọi sau khi tải xong
        override fun onPostExecute(result: Bitmap?) {
            // Ẩn progress bar
            progressBar.visibility = View.GONE

            // Kiểm tra kết quả tải
            if (result != null) {
                // Hiển thị ảnh
                imageView.setImageBitmap(result)
                Toast.makeText(this@MainActivity, "Tải ảnh thành công", Toast.LENGTH_SHORT).show()
            } else {
                // Thông báo lỗi nếu không tải được
                Toast.makeText(this@MainActivity, "Không thể tải ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }
}