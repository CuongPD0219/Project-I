package com.example.expensemanager.activities

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.expensemanager.R
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.User
import com.example.expensemanager.databinding.ActivityRegisterBinding
import com.example.expensemanager.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

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

    // Cho phép người dùng chọn ngày tháng(lịch) và cập nhật vào trong ô ngày sinh
    private fun showDatePicker(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDob.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupObservers(){
        viewModel.registerResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true

            result.onSuccess {
                Toast.makeText(
                    this,
                    "Dang ky thanh cong! Vui long dang nhap lai",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            result.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun setupListeners(){
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

    private fun registerUser(){
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPass = binding.etConfirmPassword.text.toString()
        val fullName = binding.etFullName.text.toString()
        val dateOfBirth = binding.etDob.text.toString()
        val address = binding.etAddress.text.toString()
        val occupation = binding.etOccupation.text.toString()

        when {
            username.isEmpty() || password.isEmpty() || fullName.isEmpty() -> {
                Toast.makeText(this, "Vui long nhap day du thong tin", Toast.LENGTH_SHORT).show()
            }

            password  != confirmPass ->{
                Toast.makeText(this, "Mat khau khong khop, vui long nhap lai", Toast.LENGTH_SHORT).show()
            }

            password.length < 6 -> {
                Toast.makeText(this, "Mat khau phai co it nhat 6 ky tu", Toast.LENGTH_SHORT).show()
            }

            else->{
                val user = User(
                    username = username,
                    password = password,
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    address = address,
                    occupation = occupation
                )

                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = false
                viewModel.register(user)
            }
        }
    }
}