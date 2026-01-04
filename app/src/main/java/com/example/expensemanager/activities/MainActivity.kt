package com.example.expensemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.R
import com.example.expensemanager.adapters.ExpenseAdapter
import com.example.expensemanager.databinding.ActivityMainBinding
import com.example.expensemanager.database.Expense
import com.example.expensemanager.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Quản lý Chi tiêu"

        val prefs = getSharedPreferences("ExpenseManagerPrefs", Context.MODE_PRIVATE)
        userId = intent.getIntExtra("userId", prefs.getInt("userId", -1))

        if (userId == -1) {
            finish()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        viewModel.loadAllExpenses(userId)
        viewModel.loadSummary(userId)
    }

    private fun setupObservers() {
        // Observe expenses list
        viewModel.expenses.observe(this) { expenses ->
            expenseAdapter.submitList(expenses)

            if (expenses.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvExpenses.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvExpenses.visibility = View.VISIBLE
            }
        }

        // Observe summary data
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        viewModel.totalExpense.observe(this) { total ->
            binding.tvTotalExpense.text = formatter.format(total)
        }

        viewModel.totalIncome.observe(this) { total ->
            binding.tvTotalIncome.text = formatter.format(total)
        }

        viewModel.balance.observe(this) { balance ->
            binding.tvBalance.text = formatter.format(balance)
            binding.tvBalance.setTextColor(
                if (balance >= 0)
                    resources.getColor(android.R.color.holo_green_dark, null)
                else
                    resources.getColor(android.R.color.holo_red_dark, null)
            )
        }

        // Observe operation results
        viewModel.operationResult.observe(this) { result ->
            result.onSuccess {
                loadData()
            }
            result.onFailure { exception ->
                exception.printStackTrace()
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
                true
            }
            R.id.action_statistics -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            onEditClick = { expense -> editExpense(expense) },
            onDeleteClick = { expense -> deleteExpense(expense) }
        )
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }
    }

    private fun editExpense(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("expenseId", expense.id)
        startActivity(intent)
    }

    private fun deleteExpense(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Xóa giao dịch")
            .setMessage("Bạn có chắc muốn xóa giao dịch này?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton("Hủy", null)
            .show()
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
}