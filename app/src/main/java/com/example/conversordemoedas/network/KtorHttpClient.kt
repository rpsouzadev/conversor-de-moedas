package com.example.conversordemoedas.network

import com.example.conversordemoedas.network.model.CurrencyTypesResult
import com.example.conversordemoedas.network.model.ExchangeRateResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json

object KtorHttpClient {

    private const val BASE_URL = "http://192.168.100.203:8080/"

    val client = HttpClient(OkHttp) {
        install(Logging)
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getCurrencyTypes(): Result<CurrencyTypesResult> {
        return requireGet("$BASE_URL/currency_types")
    }

    suspend fun getExchangeRate(from: String, to: String): Result<ExchangeRateResult> {
        return requireGet("$BASE_URL/exchange_rate/$from/$to")
    }


    private suspend inline fun <reified T> requireGet(url: String): Result<T> {
        return try {
            Result.success(client.get(url).body<T>())
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }
}