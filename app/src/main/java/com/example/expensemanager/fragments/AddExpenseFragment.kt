package com.example.expensemanager.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.expensemanager.R
import com.example.expensemanager.activities.MainAppActivity
import com.example.expensemanager.database.Expense
import com.example.expensemanager.databinding.FragmentAddExpenseBinding
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment: Fragment() {
    private var _binding: FragmentAddExpenseBinding?= null
    private val binding get() = _binding!!
    private val viewModelExpense: ExpenseViewModel by activityViewModels()
    private val viewModelUser: UserViewModel by activityViewModels()
    private var userId: Int = -1
    private var expenseId: Int = -1
    private var selectedDate = ""
    private var selectedType = "Chi tiêu"
    private var isEditMode = false


    private val expenseCategories = arrayOf(
        "Ăn uống", "Đi lại", "Mua sắm", "Giải trí",
        "Y tế", "Giáo dục", "Hóa đơn", "Khác"
    )

    private val incomeCategories = arrayOf(
        "Lương", "Thưởng", "Đầu tư", "Kinh doanh", "Khác"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            viewModelExpense.clearExpenseForEdit()
            findNavController().popBackStack()
        }
    }

    private fun setupViews() {
        // Set current date as default for new expense
        if (!isEditMode) {
            val calendar = Calendar.getInstance()
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            binding.etDate.setText(selectedDate)
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioExpense.id -> {
                    selectedType = "Chi tiêu"
                    updateCategorySpinner(expenseCategories)
                }
                binding.radioIncome.id -> {
                    selectedType = "Thu nhập"
                    updateCategorySpinner(incomeCategories)
                }
            }
        }

        // Default to expense
        updateCategorySpinner(expenseCategories)

        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnClear.setOnClickListener {
            clearForm()
        }
    }

    private fun setupObservers() {
        viewModelUser.user.observe(viewLifecycleOwner){user->
            user?.let{
                userId = user.id
            }
        }


        viewModelExpense.expense.observe(viewLifecycleOwner){expense ->
            expense?.let{
                expenseId = expense.id
            }
            loadExistingExpense()

        }

        viewModelExpense.operationResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            result.onSuccess { success ->
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        if (isEditMode) "Cập nhật giao dịch thành công" else "Thêm giao dịch thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearForm()
                    if(isEditMode) viewModelExpense.clearExpenseForEdit()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }

            result.onFailure { exception ->
                Toast.makeText(requireContext(), "Có lỗi xảy ra: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun loadExistingExpense(){
        if(expenseId != -1){
            binding.titleFragment.text = "Chỉnh sửa giao dịch"
            isEditMode = true

            viewModelExpense.getExpenseById(expenseId){expense->
                expense?.let{
                    binding.etAmount.setText(expense.amount.toString())
                    //luu chi tiet giao dich
                    binding.etDescription.setText(expense.description)

                    // luu ngay giao dich
                    selectedDate = expense.date
                    binding.etDate.setText(expense.date)

                    //luu loai giao dich
                    selectedType = expense.type

                    if (expense.type == "Chi tiêu") {
                        binding.radioExpense.isChecked = true
                        updateCategorySpinner(expenseCategories,expense.category)

                    } else {
                        binding.radioIncome.isChecked = true
                        updateCategorySpinner(incomeCategories,expense.category)
                    }
                }
            }
        }else{
            binding.titleFragment.text = "Thêm giao dịch"
        }
    }

    private fun updateCategorySpinner(
        categories: Array<String>,
        selectedCategory: String? = null
    ) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        selectedCategory?.let {
            val position = categories.indexOf(it)
            if (position >= 0) {
                binding.spinnerCategory.setSelection(position, false)
            }
        }
    }

    private fun clearForm(){
        binding.etAmount.text?.clear()
        binding.etDescription.text?.clear()
        binding.radioExpense.isChecked = true
        binding.spinnerCategory.setSelection(0)

        val calendar = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        binding.etDate.setText(selectedDate)
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Parse selected date if exists
        if (selectedDate.isNotEmpty()) {
            try {
                val parts = selectedDate.split("-")
                if (parts.size == 3) {
                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDate.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString().trim()


        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày giao dịch", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            id = if (isEditMode) expenseId else 0,
            userId = userId,
            amount = amount,
            category = category,
            description = description,
            date = selectedDate,
            type = selectedType
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        if (isEditMode) {
            viewModelExpense.updateExpense(expense)
        } else {
            viewModelExpense.insertExpense(expense)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


