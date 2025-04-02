package vn.edu.tlu.b11

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val TAG = "UDPChatApp"
    private val PORT_LISTENING = 8888
    private val PORT_SENDING = 8889
    private val PERMISSION_REQUEST_CODE = 101

    private lateinit var etMessage: EditText
    private lateinit var etIpAddress: EditText
    private lateinit var btnSend: Button
    private lateinit var tvStatus: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    private val messageList = mutableListOf<Message>()
    private val executor: ExecutorService = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var udpReceiverRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo các view
        etMessage = findViewById(R.id.etMessage)
        etIpAddress = findViewById(R.id.etIpAddress)
        btnSend = findViewById(R.id.btnSend)
        tvStatus = findViewById(R.id.tvStatus)
        recyclerView = findViewById(R.id.recyclerView)

        // Thiết lập RecyclerView
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Kiểm tra và yêu cầu quyền INTERNET nếu cần
        checkInternetPermission()

        // Thiết lập listener cho nút gửi
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            val ipAddress = etIpAddress.text.toString().trim()

            if (message.isNotEmpty() && ipAddress.isNotEmpty()) {
                sendUDPMessage(message, ipAddress)
                etMessage.text.clear()
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn và địa chỉ IP", Toast.LENGTH_SHORT).show()
            }
        }

        // Bắt đầu nhận tin nhắn
        startUDPReceiver()
    }

    private fun checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), PERMISSION_REQUEST_CODE)
        }
    }

    private fun sendUDPMessage(message: String, ipAddress: String) {
        executor.execute {
            try {
                val socket = DatagramSocket()
                val sendData = message.toByteArray()
                val destinationAddress = InetAddress.getByName(ipAddress)
                val packet = DatagramPacket(sendData, sendData.size, destinationAddress, PORT_SENDING)

                socket.send(packet)
                socket.close()

                // Thêm tin nhắn vào danh sách
                val sentMessage = Message(message, true)
                mainHandler.post {
                    messageList.add(sentMessage)
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)
                    updateStatus("Đã gửi tin nhắn tới $ipAddress")
                }

                Log.d(TAG, "Đã gửi tin nhắn: $message tới $ipAddress")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi gửi tin nhắn: ${e.message}")
                mainHandler.post {
                    updateStatus("Lỗi khi gửi tin nhắn: ${e.message}")
                    Toast.makeText(this, "Lỗi khi gửi tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startUDPReceiver() {
        if (!udpReceiverRunning) {
            udpReceiverRunning = true

            executor.execute {
                try {
                    val socket = DatagramSocket(PORT_LISTENING)
                    val buffer = ByteArray(1024)

                    mainHandler.post {
                        updateStatus("Đang lắng nghe tin nhắn trên cổng $PORT_LISTENING")
                    }

                    while (udpReceiverRunning) {
                        val receivePacket = DatagramPacket(buffer, buffer.size)

                        try {
                            socket.receive(receivePacket)
                            val senderAddress = receivePacket.address.hostAddress
                            val data = receivePacket.data
                            val message = String(data, 0, receivePacket.length)

                            Log.d(TAG, "Nhận được tin nhắn: $message từ $senderAddress")

                            mainHandler.post {
                                val receivedMessage = Message(message, false, senderAddress)
                                messageList.add(receivedMessage)
                                messageAdapter.notifyItemInserted(messageList.size - 1)
                                recyclerView.scrollToPosition(messageList.size - 1)
                                updateStatus("Đã nhận tin nhắn từ $senderAddress")
                            }
                        } catch (e: Exception) {
                            if (udpReceiverRunning) {
                                Log.e(TAG, "Lỗi khi nhận tin nhắn: ${e.message}")
                                mainHandler.post {
                                    updateStatus("Lỗi khi nhận tin nhắn: ${e.message}")
                                }
                            }
                        }
                    }

                    socket.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi khi khởi tạo socket lắng nghe: ${e.message}")
                    mainHandler.post {
                        updateStatus("Lỗi khi khởi tạo socket lắng nghe: ${e.message}")
                        udpReceiverRunning = false
                    }
                }
            }
        }
    }

    private fun updateStatus(status: String) {
        tvStatus.text = status
    }

    override fun onDestroy() {
        udpReceiverRunning = false
        executor.shutdown()
        super.onDestroy()
    }
}