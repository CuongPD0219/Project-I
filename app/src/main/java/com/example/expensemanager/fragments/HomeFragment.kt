package com.example.expensemanager.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.R
import com.example.expensemanager.adapters.ExpenseAdapter
import com.example.expensemanager.database.Expense
import com.example.expensemanager.databinding.FragmentHomeBinding
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.UserViewModel
import java.text.NumberFormat
import java.util.Locale


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val expenseViewModel: ExpenseViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animate balance card
        binding.cardView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in))

        // Animate balance number với scale
        binding.tvBalance.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in))

        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }


    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            onEditClick = { expense -> editExpense(expense) },
            onDeleteClick = { expense -> deleteExpense(expense) }
        )
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }
    }

    private fun setupObservers() {
        // Observe user data for greeting
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvGreeting.text = "Xin chào, ${user.fullName}"
                userId = user.id
                loadData()
            }
        }

        //Observe ảnh đại diện
        userViewModel.imgProfile.observe(viewLifecycleOwner) { imgProfile ->
            val currImageProfile = imgProfile ?: R.drawable.mu
            binding.imgProfile.setImageResource(currImageProfile)
        }

        // Observe expenses list
        expenseViewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)

            if (expenses.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvExpenses.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvExpenses.visibility = View.VISIBLE
            }
        }

        // Observe summary data with animation
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        expenseViewModel.totalExpense.observe(viewLifecycleOwner) { total ->
            animateTextChange(binding.tvTotalExpense, formatter.format(total))
        }

        expenseViewModel.totalIncome.observe(viewLifecycleOwner) { total ->
            animateTextChange(binding.tvTotalIncome, formatter.format(total))
        }

        expenseViewModel.balance.observe(viewLifecycleOwner) { balance ->
            animateTextChange(binding.tvBalance, formatter.format(balance))
            binding.tvBalance.setTextColor(
                if (balance >= 0)
                    resources.getColor(R.color.success_green, null)
                else
                    resources.getColor(R.color.error_red, null)
            )
        }

        // Observe operation results
        expenseViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                loadData()
            }
        }
    }

    private fun animateTextChange(view: android.widget.TextView, newText: String) {
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)

        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                view.text = newText
                view.startAnimation(fadeIn)
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        view.startAnimation(fadeOut)
    }

    private fun loadData() {
        expenseViewModel.loadAllExpenses(userId)
        expenseViewModel.loadSummary(userId)
    }

    private fun editExpense(expense: Expense) {
        expenseViewModel.setExpenseForEdit(expense)
        findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
    }

    private fun deleteExpense(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa giao dịch")
            .setMessage("Bạn có chắc muốn xóa giao dịch này?")
            .setPositiveButton("Xóa") { _, _ ->
                expenseViewModel.deleteExpense(expense)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}