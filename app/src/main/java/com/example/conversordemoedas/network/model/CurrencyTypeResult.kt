package com.example.conversordemoedas.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyType(
    val name: String,
    val acronym: String,
    val symbol: String,
    @SerialName("country_flag_image")
    val countryFlagImageUrl: String,
)

@Serializable
data class CurrencyTypesResult(
    val values: List<CurrencyType>
)
