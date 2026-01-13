package com.example.expensemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expensemanager.R
import com.example.expensemanager.databinding.ActivityLoginBinding
import com.example.expensemanager.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //kiem tra neu da dang nhap
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        if(userId != -1){
            navigateToMain(userId)
            return
        }

        binding.imgLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))
        binding.cardLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_up))

        setupObservers()
        setupListeners()
    }

    private fun setupObservers(){
        viewModel.loginResult.observe(this){result ->
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            result.onSuccess{user->
                //luu thong tin dang nhap vao shared preferences
                val prefs = getSharedPreferences("ExpenseManagerPrefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("userId", user.id).apply()

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT)
                navigateToMain(user.id)
            }

            result.onFailure{exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                viewModel.login(username, password)
            }
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun navigateToMain(userId: Int){
        val intent = Intent(this, MainAppActivity::class.java)
        intent.putExtra("userId",userId)
        startActivity(intent)
        finish()
    }
}