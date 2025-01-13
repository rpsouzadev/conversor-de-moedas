package com.example.conversordemoedas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.conversordemoedas.databinding.ActivityMainBinding
import com.example.conversordemoedas.databinding.ContentExchangeRateSuccessBinding
import com.example.conversordemoedas.network.model.CurrencyType
import com.example.conversordemoedas.ui.CurrencyTypesAdapter
import com.example.conversordemoedas.viewmodel.CurrencyExchangeViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CurrencyExchangeViewModel by viewModels()

    private var exchangeRate: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.requireCurrencyType()
        binding.lExchangeRateSuccess.etFromExchange.addCurrencyMask()
        binding.lExchangeRateSuccess.etToExchange.addCurrencyMask()

        binding.lExchangeRateError.btnTryAgain.setOnClickListener {
            binding.showContentLoading()
            viewModel.requireCurrencyType()
        }

        lifecycleScope.apply {
            launch {
                viewModel.currencyTypes.collect { result ->
                    result.onSuccess { currencyTypes ->
                        binding.showContentSuccess()
                        binding.lExchangeRateSuccess.configureSpinner(currencyTypes = currencyTypes)
                    }.onFailure {
                        binding.showContentError()
                    }
                }
            }

            launch {
                viewModel.exchangeRate.collect { result ->
                    result.onSuccess { exchangeRateResult ->
                        exchangeRateResult?.let {
                            binding.showContentSuccess()
                            exchangeRate = it.exchangeRate
                            binding.lExchangeRateSuccess.generateConvertedValue()
                        }
                    }.onFailure {
                        binding.showContentError()
                    }
                }
            }
        }
    }

    private fun ContentExchangeRateSuccessBinding.configureSpinner(currencyTypes: List<CurrencyType>) {
        spnFromExchange.apply {
            adapter = CurrencyTypesAdapter(currencyTypes)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val from = currencyTypes[position]
                    val to = currencyTypes[spnToExchange.selectedItemPosition]

                    tvFromCurrencySymbol.text = from.symbol

                    viewModel.requireExchangeRate(from = from.acronym, to = to.acronym)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }
        }


        spnToExchange.apply {
            adapter = CurrencyTypesAdapter(currencyTypes)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val from = currencyTypes[spnFromExchange.selectedItemPosition]
                    val to = currencyTypes[position]

                    tvToCurrencySymbol.text = to.symbol

                    viewModel.requireExchangeRate(from = from.acronym, to = to.acronym)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    currencyTypes.firstOrNull()?.let { firstCurrencyType ->
                        tvFromCurrencySymbol.text = firstCurrencyType.symbol
                        tvToCurrencySymbol.text = firstCurrencyType.symbol
                        viewModel.requireExchangeRate(
                            from = firstCurrencyType.acronym,
                            to = firstCurrencyType.acronym
                        )
                    }
                }

            }
        }
    }

    private fun EditText.addCurrencyMask() {
        addTextChangedListener(object : TextWatcher {
            private var currencyText = ""

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != currencyText) {
                    removeTextChangedListener(this)
                    val cleanedString = s.toString().replace("[,.]".toRegex(), "")
                    val currencyValue = cleanedString.toDoubleOrNull() ?: 0.0

                    val formattedValue = DecimalFormat(
                        "#,##0.00",
                        DecimalFormatSymbols(Locale.getDefault())
                    ).format(currencyValue / 100)

                    currencyText = formattedValue
                    setText(formattedValue)
                    setSelection(formattedValue.length)

                    binding.lExchangeRateSuccess.generateConvertedValue()

                    addTextChangedListener(this)
                }
            }

        })
    }

    private fun ContentExchangeRateSuccessBinding.generateConvertedValue() {
        exchangeRate?.let { value ->
            val cleanedString = etFromExchange.text.toString().replace("[,.]".toRegex(), "")
            val currencyValue = cleanedString.toDoubleOrNull() ?: 0.0

            val formattedValue = DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols(Locale.getDefault())
            ).format((currencyValue * value) / 100)

            etToExchange.setText(formattedValue)
        }
    }

    private fun ActivityMainBinding.showContentError() {
        pbLoading.visibility = View.GONE
        lExchangeRateError.root.visibility = View.VISIBLE
        lExchangeRateSuccess.root.visibility = View.GONE
    }

    private fun ActivityMainBinding.showContentSuccess() {
        pbLoading.visibility = View.GONE
        lExchangeRateError.root.visibility = View.GONE
        lExchangeRateSuccess.root.visibility = View.VISIBLE
    }

    private fun ActivityMainBinding.showContentLoading() {
        pbLoading.visibility = View.VISIBLE
        lExchangeRateError.root.visibility = View.GONE
        lExchangeRateSuccess.root.visibility = View.GONE
    }
}