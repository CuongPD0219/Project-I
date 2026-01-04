package com.example.expensemanager.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.database.Expense
import com.example.expensemanager.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()){


    // Tạo ViewHolder cho mỗi mục trong danh sách
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseAdapter.ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ExpenseViewHolder(binding)
    }

    // liên kết dữ liệu của mục với ViewHolder
    override fun onBindViewHolder(holder: ExpenseAdapter.ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))    }

    // thiết kế dữ liệu cho mục expense
    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ): RecyclerView.ViewHolder(binding.root){

        // gắn dữ liệu của mục vào thành phần giao diện tương ứng
        fun bind(expense: Expense) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

            binding.tvCategory.text = expense.category
            binding.tvDescription.text = if (expense.description.isEmpty()) {
                "Khong co mo ta"
            } else {
                expense.description
            }

            binding.tvAmount.text = formatter.format(expense.amount)
            binding.tvDate.text = formatDate(expense.date)

            // thay đổi màu sắc khi phân loại thu và chi
            if (expense.type == "chi") {//nếu là chi
                binding.tvAmount.setTextColor(Color.rgb(211, 47, 47)) //màu đỏ
                binding.cardView.setCardBackgroundColor(Color.rgb(255, 235, 238))
            } else {
                binding.tvAmount.setTextColor(Color.rgb(56, 142, 60)) //màu xanh lá cây
                binding.cardView.setCardBackgroundColor(Color.rgb(232, 245, 233))
            }

            // gán sự kiện click cho nút chỉnh sửa
            binding.btnEdit.setOnClickListener {
                onEditClick(expense)
            }

            // gán sự kiện click cho nút xóa
            binding.btnDelete.setOnClickListener {
                onDeleteClick(expense)
            }
        }

        // định dạng ngày tháng
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

    // so sánh hai mục expense để xác định xem chúng có giống nhau hay không
    class ExpenseDiffCallback: DiffUtil.ItemCallback<Expense>(){
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean{
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean{
            return oldItem == newItem
        }
    }



}
