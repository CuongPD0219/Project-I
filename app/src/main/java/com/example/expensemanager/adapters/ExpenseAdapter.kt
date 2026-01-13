package com.example.expensemanager.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.R
import com.example.expensemanager.databinding.ItemExpenseBinding
import com.example.expensemanager.database.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val context = binding.root.context
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

            binding.tvCategory.text = expense.category
            binding.tvDescription.text = if (expense.description.isNotEmpty()) {
                expense.description
            } else {
                "Không có mô tả"
            }

            binding.tvAmount.text = formatter.format(expense.amount)
            binding.tvDate.text = formatDate(expense.date)

            // Set color, icon and background based on type
            if (expense.type == "Chi tiêu") {
                binding.tvAmount.setTextColor(Color.rgb(211, 47, 47)) // Red
                binding.cardView.setCardBackgroundColor(Color.rgb(255, 245, 245))
                binding.imgCategoryIcon.setImageResource(getCategoryIcon(expense.category, true))
                binding.imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.error_red))
            } else {
                binding.tvAmount.setTextColor(Color.rgb(56, 142, 60)) // Green
                binding.cardView.setCardBackgroundColor(Color.rgb(245, 255, 245))
                binding.imgCategoryIcon.setImageResource(getCategoryIcon(expense.category, false))
                binding.imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.success_green))
            }

            binding.btnEdit.setOnClickListener {
                onEditClick(expense)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(expense)
            }
        }

        private fun getCategoryIcon(category: String, isExpense: Boolean): Int {
            return if (isExpense) {
                when (category) {
                    "Ăn uống" -> R.drawable.ic_person
                    "Đi lại" -> R.drawable.ic_transport
                    "Mua sắm" -> R.drawable.ic_shopping
                    "Giải trí" -> R.drawable.ic_entertainment
                    "Y tế" -> R.drawable.ic_health
                    "Giáo dục" -> R.drawable.ic_education
                    "Hóa đơn" -> R.drawable.ic_bill
                    else -> R.drawable.ic_other
                }
            } else {
                when (category) {
                    "Lương" -> R.drawable.ic_salary
                    "Thưởng" -> R.drawable.ic_bonus
                    "Đầu tư" -> R.drawable.ic_up
                    "Kinh doanh" -> R.drawable.ic_business
                    else -> R.drawable.ic_other
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}