package com.example.expensemanager.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.expensemanager.R
import com.example.expensemanager.databinding.ActivityMainAppBinding
import com.example.expensemanager.fragments.AddExpenseFragment
import com.example.expensemanager.fragments.HomeFragment
import com.example.expensemanager.fragments.ProfileFragment
import com.example.expensemanager.fragments.StatisticsFragment
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainAppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainAppBinding
    private val viewModelUser: UserViewModel by viewModels()
    // Khai báo thêm ViewModel dùng chung để dữ liệu luôn sẵn sàng
    private val viewModelExpense: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Lấy NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Tự động kết nối BottomNavigation với NavController
        // Cách này sẽ tự động chuyển Fragment dựa trên ID trùng khớp giữa Menu và NavGraph
        binding.bottomNavigation.setupWithNavController(navController)

        // 3. Xử lý dữ liệu User
        val prefs = getSharedPreferences("ExpenseManagerPrefs", MODE_PRIVATE)
        val userId = intent.getIntExtra("userId", prefs.getInt("userId", -1))

        // Nạp dữ liệu vào Shared ViewModel ngay tại Activity
        // Tất cả các Fragment sử dụng activityViewModels() sẽ nhận được dữ liệu này ngay lập tức
        viewModelUser.loadUser(userId)

        // 4. Riêng nút Đăng xuất (Logout) không có trong NavGraph, ta xử lý riêng
        setupSpecialMenuActions()

        setupBottomNavigationVisibility()
    }
    private fun setupBottomNavigationVisibility() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.i("MainAppActivity", "Current destination: ${destination.label} (ID: ${destination.id})")

            val isEditMode = viewModelExpense.expense.value != null
            // Kiểm tra ID của Fragment đích
            // Chú ý: Phải dùng ID của Fragment (addExpenseFragment), KHÔNG dùng ID của Action
            if (destination.id == R.id.addExpenseFragment) {
                if(isEditMode)
                    binding.bottomNavigation.visibility = View.GONE
                else
                    binding.bottomNavigation.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSpecialMenuActions() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
            val navController = navHostFragment.navController

            when (item.itemId) {
                R.id.logout -> {
                    showLogoutDialog()
                    true
                }
                else -> {
                    // Đối với các ID khác (home, stats, profile), hãy để NavController xử lý
                    // Điều này giúp giữ nguyên instance hoặc quay lại đúng vị trí trong BackStack
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                val prefs = getSharedPreferences("ExpenseManagerPrefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.navHost)
        if(navController.currentDestination?.id == R.id.addExpenseFragment){
            viewModelExpense.clearExpenseForEdit()
        }
        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }

}
