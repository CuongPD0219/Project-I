package com.example.expensemanager.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.databinding.ActivityRegisterBinding
import com.example.expensemanager.database.User
import com.example.expensemanager.viewmodel.AuthViewModel
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true

            result.onSuccess {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                finish()
            }

            result.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDob.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val occupation = binding.etOccupation.text.toString().trim()

        when {
            username.isEmpty() || password.isEmpty() || fullName.isEmpty() -> {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show()
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            }
            password.length < 6 -> {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val user = User(
                    username = username,
                    password = password,
                    fullName = fullName,
                    dateOfBirth = dob,
                    address = address,
                    occupation = occupation
                )

                binding.progressBar.visibility = View.VISIBLE
                binding.btnRegister.isEnabled = false
                viewModel.register(user)
            }
        }
    }
}