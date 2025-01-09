package com.example.conversordemoedas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import coil3.load
import coil3.svg.SvgDecoder
import com.example.conversordemoedas.databinding.ItemCurrencyTypeBinding
import com.example.conversordemoedas.network.model.CurrencyType

class CurrencyTypesAdapter(
    private val currencyTypes: List<CurrencyType>
) : BaseAdapter() {
    override fun getCount(): Int {
        return currencyTypes.size
    }

    override fun getItem(position: Int): Any {
        return currencyTypes[position]
    }

    override fun getItemId(position: Int): Long {
        return currencyTypes[position].hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return convertView ?: run {
            val item = currencyTypes[position]
            val binding = ItemCurrencyTypeBinding.inflate(LayoutInflater.from(parent?.context))

            with(binding) {
                tvCurrencyAcronym.text = item.acronym
                ivFlag.load(item.countryFlagImageUrl) {
                    decoderFactory { result, options, _ ->
                        SvgDecoder(result.source, options)
                    }
                }
            }

            binding.root
        }
    }
}