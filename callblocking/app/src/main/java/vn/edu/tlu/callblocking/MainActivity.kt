package vn.edu.tlu.callblocking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_READ_PHONE_STATE = 1
    }

    private lateinit var numberEditText: EditText
    private lateinit var addButton: Button
    private lateinit var numberListView: ListView
    private lateinit var blockedNumberAdapter: ArrayAdapter<String>
    private lateinit var logListView: ListView
    private lateinit var logAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request permissions
        checkPermissions()

        // Initialize views
        numberEditText = findViewById(R.id.numberEditText)
        addButton = findViewById(R.id.addButton)
        numberListView = findViewById(R.id.numberListView)

        // Set up the adapter for blocked numbers
        blockedNumberAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            CallBlockingReceiver.BLOCKED_NUMBERS
        )
        numberListView.adapter = blockedNumberAdapter

        // Set up add button functionality
        addButton.setOnClickListener {
            val number = numberEditText.text.toString().trim()
            if (number.isNotEmpty()) {
                // Add number to blocked list
                CallBlockingReceiver.BLOCKED_NUMBERS.add(number)
                blockedNumberAdapter.notifyDataSetChanged()

                // Clear input field
                numberEditText.text.clear()

                Toast.makeText(this, "Đã thêm số $number vào danh sách chặn", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }

        // Add long click to remove number
        numberListView.setOnItemLongClickListener { _, _, position, _ ->
            val removedNumber = CallBlockingReceiver.BLOCKED_NUMBERS.removeAt(position)
            blockedNumberAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Đã gỡ số $removedNumber khỏi danh sách chặn", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Đã cấp quyền", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}