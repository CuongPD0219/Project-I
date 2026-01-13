package com.example.expensemanager.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.example.expensemanager.R
import com.example.expensemanager.databinding.FragmentStatisticsBinding
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.UserViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale


class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding?= null
    private val binding get() = _binding!!
    private val expenseViewModel: ExpenseViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private var userId: Int = -1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupObservers()

    }

    private fun setupObservers(){
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        userViewModel.user.observe(viewLifecycleOwner){user->
            user?.let{
                userId = user.id
                loadStatistics()
            }
        }

        expenseViewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            val expenseList = expenses.filter { it.type == "Chi tiêu" }
            val incomeList = expenses.filter { it.type == "Thu nhập" }

            val totalExpense = expenseList.sumOf { it.amount }
            val totalIncome = incomeList.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            binding.tvMonthlyExpense.text = formatter.format(totalExpense)
            binding.tvMonthlyIncome.text = formatter.format(totalIncome)
            binding.tvMonthlyBalance.text = formatter.format(balance)

            binding.tvMonthlyBalance.setTextColor(
                if (balance >= 0)
                    resources.getColor(R.color.success_green, null)
                else
                    resources.getColor(R.color.error_red, null)
            )

            // Setup pie charts with legends
            setupExpensePieChart(expenseList)
            setupIncomePieChart(incomeList)
        }

    }
    private fun setupSpinners(){
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear).map { it.toString() }
        val yearAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(years.size - 1)

        val months = (1..12).map { String.format("Tháng %02d", it) }
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))

        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedYear = years[position].toInt()
                loadStatistics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position + 1
                loadStatistics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadStatistics(){
        expenseViewModel.loadExpensesByMonth(userId, selectedYear, selectedMonth)
    }

    private fun setupExpensePieChart(expenses: List<com.example.expensemanager.database.Expense>) {
        if (expenses.isEmpty()) {
            binding.pieChartExpense.visibility = View.GONE
            binding.tvNoExpenseData.visibility = View.VISIBLE
            binding.llExpenseLegend.visibility = View.GONE
            return
        }

        binding.pieChartExpense.visibility = View.VISIBLE
        binding.tvNoExpenseData.visibility = View.GONE
        binding.llExpenseLegend.visibility = View.VISIBLE

        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val total = categoryTotals.values.sum()
        val entries = categoryTotals.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = getExpenseColorScheme()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 8f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChartExpense))

        binding.pieChartExpense.apply {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.isEnabled = false

            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }

        // Create custom legend
        createCustomLegend(binding.llExpenseLegend, categoryTotals, total, getExpenseColorScheme())
    }

    private fun setupIncomePieChart(incomes: List<com.example.expensemanager.database.Expense>) {
        if (incomes.isEmpty()) {
            binding.pieChartIncome.visibility = View.GONE
            binding.tvNoIncomeData.visibility = View.VISIBLE
            binding.llIncomeLegend.visibility = View.GONE
            return
        }

        binding.pieChartIncome.visibility = View.VISIBLE
        binding.tvNoIncomeData.visibility = View.GONE
        binding.llIncomeLegend.visibility = View.VISIBLE

        val categoryTotals = incomes.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val total = categoryTotals.values.sum()
        val entries = categoryTotals.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = getIncomeColorScheme()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 8f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChartIncome))

        binding.pieChartIncome.apply {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.isEnabled = false

            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }

        // Create custom legend
        createCustomLegend(binding.llIncomeLegend, categoryTotals, total, getIncomeColorScheme())
    }

    private fun createCustomLegend(
        container: android.widget.LinearLayout,
        categoryTotals: Map<String, Double>,
        total: Double,
        colors: List<Int>
    ) {
        container.removeAllViews()
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        categoryTotals.entries.forEachIndexed { index, entry ->
            val percentage = (entry.value / total * 100).toInt()

            val legendView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_legend, container, false)

            val colorIndicator = legendView.findViewById<View>(R.id.viewColorIndicator)
            val tvCategory = legendView.findViewById<android.widget.TextView>(R.id.tvCategory)
            val tvAmount = legendView.findViewById<android.widget.TextView>(R.id.tvAmount)
            val progressBar = legendView.findViewById<android.widget.ProgressBar>(R.id.progressBar)

            colorIndicator.setBackgroundColor(colors[index % colors.size])
            tvCategory.text = entry.key
            tvAmount.text = formatter.format(entry.value)
            progressBar.progress = percentage

            container.addView(legendView)
        }
    }

    private fun getExpenseColorScheme(): List<Int> {
        return listOf(
            Color.rgb(66, 165, 245),   // Xanh dương
            Color.rgb(239, 83, 80),    // Đỏ
            Color.rgb(102, 187, 106),  // Xanh lá
            Color.rgb(255, 167, 38),   // Cam
            Color.rgb(171, 71, 188),   // Tím
            Color.rgb(255, 238, 88),   // Vàng
            Color.rgb(38, 198, 218),   // Xanh ngọc
            Color.rgb(255, 112, 167)   // Hồng
        )
    }

    private fun getIncomeColorScheme(): List<Int> {
        return listOf(
            Color.rgb(156, 39, 176),   // Tím đậm
            Color.rgb(0, 150, 136),    // Xanh lục lam
            Color.rgb(255, 193, 7),    // Vàng đậm
            Color.rgb(33, 150, 243),   // Xanh dương đậm
            Color.rgb(255, 87, 34),    // Cam đậm
            Color.rgb(76, 175, 80),    // Xanh lá đậm
            Color.rgb(121, 85, 72),    // Nâu
            Color.rgb(96, 125, 139)    // Xám xanh
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}