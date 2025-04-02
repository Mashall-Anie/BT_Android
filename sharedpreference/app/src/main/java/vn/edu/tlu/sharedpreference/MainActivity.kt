package vn.edu.tlu.sharedpreference

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var btnShow: Button
    private lateinit var tvResult: TextView

    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = PreferenceHelper(this)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        btnShow = findViewById(R.id.btnShow)
        tvResult = findViewById(R.id.tvResult)

        setupListeners()
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            preferenceHelper.saveUserCredentials(username, password)
            Toast.makeText(this, "Đã lưu thông tin thành công", Toast.LENGTH_SHORT).show()
            clearInputFields()
        }

        btnDelete.setOnClickListener {
            preferenceHelper.clearUserCredentials()
            tvResult.text = ""
            clearInputFields()
            Toast.makeText(this, "Đã xóa thông tin thành công", Toast.LENGTH_SHORT).show()
        }

        btnShow.setOnClickListener {
            if (!preferenceHelper.hasUserCredentials()) {
                tvResult.text = "Chưa có dữ liệu nào được lưu"
                return@setOnClickListener
            }

            val savedUsername = preferenceHelper.getUsername()
            val savedPassword = preferenceHelper.getPassword()
            tvResult.text = "Thông tin đã lưu:\nTên người dùng: $savedUsername\nMật khẩu: $savedPassword"
        }
    }

    private fun clearInputFields() {
        etUsername.text.clear()
        etPassword.text.clear()
    }
}
