package com.example.expensemanager.activities

import android.content.Context
import android.content.Intent
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
import com.example.expensemanager.databinding.ActivityLoginBinding
import com.example.expensemanager.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel : AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Kiểm tra đã đăng nhập từ trước chưa(nếu rồi thì chuyển luôn không cần kiểm tra lại từ database)
        val prefs = getSharedPreferences("ExpenseManager", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        if(userId != -1){
            navigateToMain(userId)
            return
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers(){
        viewModel.loginResult.observe(this){result ->
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            result.onSuccess{user ->

                val prefs = getSharedPreferences("ExpenseManager", Context.MODE_PRIVATE)
                prefs.edit().putInt("userId", user.id).apply()

                Toast.makeText(this, "Dang nhap thanh cong! Chao mung ban den voi ung dung", Toast.LENGTH_SHORT).show()
                navigateToMain(user.id)
            }

            result.onFailure{exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners(){
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Vui long nhap day du thong tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            viewModel.login(username, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Truy cập vào ứng dụng khi đăng nhập thành công
    private fun navigateToMain(userId: Int){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
    }
}