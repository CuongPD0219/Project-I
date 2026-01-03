package com.example.expensemanager.activities

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.expensemanager.R
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.Expense
import com.example.expensemanager.databinding.ActivityAddExpenseBinding
import com.example.expensemanager.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private var userId: Int = -1
    private var expenseId: Int = -1
    private var selectedDate = ""
    private var selectedType = "chi"

    private val expenseCategories = arrayOf(
        "Ăn uống", "Đi lại", "Mua sắm", "Giải trí",
        "Y tế", "Giáo dục", "Hoá đơn", "Khác"
    )

    private val incomeCategories = arrayOf(
        "Lương", "Đầu tư", "Kinh doanh", "Thưởng", "Khác"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("userId", -1)
        expenseId = intent.getIntExtra("expenseId", -1)

        if(userId == -1){
            finish()
            return
        }

        setupViews()
        setupObservers()
        loadExistingExpense()
    }

    private fun setupViews(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // thiet lap thoi gian hien tai la mac dinh
        val calendar = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        binding.etDate.setText(selectedDate)

        binding.etDate.setOnClickListener{
            showDatePicker()
        }

        binding.radioGroupType.setOnCheckedChangeListener{_, checkedId ->
            when (checkedId){
                binding.radioExpense.id ->{
                    selectedType = "chi"
                    updateCategorySpinner(expenseCategories)
                }
                binding.radioIncome.id ->{
                    selectedType = "thu"
                    updateCategorySpinner(incomeCategories)
                }
            }
        }

        //thiet lap giao dien mac dinh ve chi
        updateCategorySpinner(expenseCategories)

        binding.btnSave.setOnClickListener{
            saveExpense()
        }
    }

    private fun setupObservers(){
        viewModel.operationResult.observe(this){result ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true

            result.onSuccess{success->
                if(success){
                    val message = if (expenseId == -1) "Thêm giao dịch thành công"
                    else "Cập nhật giao dịch thành công"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            result.onFailure { exception ->
                Toast.makeText(this, "Co loi xay ra: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // cap nhat giao dien theo tung loai chi hoac thu
    private fun updateCategorySpinner(categories: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    //
    private fun loadExistingExpense(){
        if(expenseId != -1) {
            supportActionBar?.title = "Chỉnh sửa giao dịch"

            viewModel.getExpenseById(expenseId) { expense ->
                expense?.let {
                    runOnUiThread {
                        binding.etAmount.setText(it.amount.toString())
                        binding.etDescription.setText(it.description)
                        binding.etDate.setText(it.date)
                        selectedDate = it.date
                        selectedType = it.type

                        if (selectedType == "chi") {
                            binding.radioExpense.isChecked = true
                            updateCategorySpinner(expenseCategories)
                            val position = expenseCategories.indexOf(it.category)
                            if (position > -1) binding.spinnerCategory.setSelection(position)
                        } else {
                            binding.radioIncome.isChecked = true
                            updateCategorySpinner(incomeCategories)
                            val position = incomeCategories.indexOf(it.category)
                            if (position > -1) binding.spinnerCategory.setSelection(position)
                        }
                    }
                }
            }
        }else{
            supportActionBar?.title = "Them giao dich moi"
        }
    }

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
                binding.etDate.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveExpense(){
        val amountStr = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()

        if(amountStr.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if(amount == null || amount <= 0){
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            id = expenseId,
            userId = userId,
            amount = amount,
            description = description,
            category = category,
            date = selectedDate,
            type = selectedType
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        if(expenseId == -1){
            viewModel.insertExpense(expense)
        }else{
            viewModel.updateExpense(expense)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}