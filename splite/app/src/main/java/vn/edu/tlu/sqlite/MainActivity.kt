package vn.edu.tlu.sqlite

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnShow: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        btnAdd = findViewById(R.id.btnAdd)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnShow = findViewById(R.id.btnShow)
        tvResult = findViewById(R.id.tvResult)

        setupListeners()
    }

    private fun setupListeners() {
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                showToast("Vui lòng nhập đầy đủ thông tin")
                return@setOnClickListener
            }

            dbHelper.addContact(name, phone)
            showToast("Thêm thành công")
            clearInputFields()
        }

        btnUpdate.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                showToast("Vui lòng nhập đầy đủ thông tin")
                return@setOnClickListener
            }

            dbHelper.updateContact(name, phone)
            showToast("Cập nhật thành công")
            clearInputFields()
        }

        btnDelete.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                showToast("Vui lòng nhập tên để xóa")
                return@setOnClickListener
            }

            dbHelper.deleteContact(name)
            showToast("Xóa thành công")
            clearInputFields()
        }

        btnShow.setOnClickListener {
            val data = dbHelper.getAllContacts()
            tvResult.text = if (data.isNotEmpty()) data else "Không có dữ liệu"
        }
    }

    private fun clearInputFields() {
        etName.text.clear()
        etPhone.text.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
