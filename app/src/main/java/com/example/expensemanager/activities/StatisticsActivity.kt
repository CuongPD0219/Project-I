package com.example.expensemanager.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.expensemanager.R
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.Expense
import com.example.expensemanager.databinding.ActivityStatisticsBinding
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private var userId: Int = -1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Thống kê chi tiêu"

        userId = intent.getIntExtra("userId", -1)

        if(userId == -1){
            finish()
            return
        }

        setupSpinners()
        setupObservers()
        loadStatistics()
    }

    private fun setupObservers(){
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        viewModel.expenses.observe(this){expenses ->
            val totalExpense = expenses.filter{it.type == "chi"}.sumOf{it.amount}
            val totalIncome = expenses.filter{it.type == "thu"}.sumOf{it.amount}
            val balance = totalIncome - totalExpense

            binding.tvMonthlyExpense.text = formatter.format(totalExpense)
            binding.tvMonthlyIncome.text =formatter.format(totalIncome)
            binding.tvMonthlyBalance.text = formatter.format(balance)

            binding.tvMonthlyBalance.setTextColor(
                if(balance >= 0)
                    resources.getColor(android.R.color.holo_green_dark, null)
                else
                    resources.getColor(android.R.color.holo_red_dark, null)
            )

            setupExpensePieChart(expenses.filter{it.type == "chi"})
            setupIncomePieChart(expenses.filter{it.type == "thu"})
        }
    }

    private fun setupSpinners(){
        // spinner cua nam
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear).map{it.toString()}
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(years.size-1)

        // spinner cua thang
        val months = (1..12).map{String.format("Thang %02d", it)}
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))

        binding.spinnerYear.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long){
                selectedYear = years[position].toInt()
                loadStatistics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?){}
        }

        binding.spinnerMonth.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long){
                selectedMonth = position + 1
                loadStatistics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?){}
        }
    }

    private fun loadStatistics(){
        viewModel.loadExpensesByMonth(userId, month = selectedMonth, year = selectedYear)
    }

    private fun setupExpensePieChart(expenses: List<Expense>){
        if(expenses.isEmpty()){
            binding.pieChartExpense.visibility = View.GONE
            binding.tvNoExpenseData.visibility = View.VISIBLE
            return
        }


        binding.pieChartExpense.visibility = View.VISIBLE
        binding.tvNoExpenseData.visibility = View.GONE

        val categoryTotals = expenses.groupBy{it.category}
            .mapValues{entry -> entry.value.sumOf{it.amount}.toFloat()}

        val entries = categoryTotals.map{ PieEntry(it.value, it.key) }

        val dataSet = PieDataSet(entries, "Chi tieu theo danh muc")
        dataSet.colors = getRedColorScheme()
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
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)

            legend.apply{
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 12f
            }

            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun setupIncomePieChart(incomes : List<Expense>){
        if(incomes.isEmpty()){
            binding.pieChartIncome.visibility = View.GONE
            binding.tvNoIncomeData.visibility = View.VISIBLE
            return
        }

        binding.pieChartIncome.visibility = View.VISIBLE
        binding.tvNoIncomeData.visibility = View.GONE

        val categoryTotals = incomes.groupBy{it.category}
            .mapValues{entry -> entry.value.sumOf{it.amount}.toFloat()}

        val entries = categoryTotals.map{PieEntry(it.value, it.key)}

        val dataSet = PieDataSet(entries, "Thu nhap theo danh muc")
        dataSet.colors = getGreenColorScheme()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 8f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChartIncome))

        binding.pieChartIncome.apply{
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)

            legend.apply{
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 12f
            }

            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun getRedColorScheme(): List<Int>{
        return listOf(
            Color.rgb(239,83 , 80),   //Red 400
            Color.rgb(229, 57, 53),   //Red 600
            Color.rgb(198,40, 40),    //Red 700
            Color.rgb(183, 28, 28),   //Red 800
            Color.rgb(255, 138, 101), //Deep Orange 300
            Color.rgb(255, 87, 34),   //Deep Orange 600
            Color.rgb(244, 67, 54),   //Red 500
            Color.rgb(211, 47,47)     //Red 700
        )
    }

    private fun getGreenColorScheme(): List<Int> {
        return listOf(
            Color.rgb(102, 187, 106), // Green 400
            Color.rgb(76, 175, 80),   // Green 500
            Color.rgb(67, 160, 71),   // Green 600
            Color.rgb(56, 142, 60),   // Green 700
            Color.rgb(46, 125, 50),   // Green 800
            Color.rgb(129, 199, 132), // Green 300
            Color.rgb(165, 214, 167), // Green 200
            Color.rgb(27, 94, 32)     // Green 900
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}