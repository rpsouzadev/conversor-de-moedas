package com.example.conversordemoedas

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.conversordemoedas.databinding.ActivityMainBinding
import com.example.conversordemoedas.ui.CurrencyTypesAdapter
import com.example.conversordemoedas.viewmodel.CurrencyExchangeViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CurrencyExchangeViewModel by viewModels()

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

        lifecycleScope.apply {
            launch {
                viewModel.currencyTypes.collect { result ->
                    result.onSuccess { currencyTypes ->
                        binding.spnFromExchange.adapter = CurrencyTypesAdapter(currencyTypes)
                        binding.spnToExchange.adapter = CurrencyTypesAdapter(currencyTypes)
                    }.onFailure {
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            launch {
                viewModel.exchangeRate.collect { result ->
                    result.onSuccess { exchangeRate ->
                        Log.d("MainActivity", exchangeRate.toString())
                    }.onFailure {
                        Log.d("MainActivity", it.message.toString())
                    }
                }
            }
        }
    }
}