package com.example.mcp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * HTTP клиент для работы с Open-Meteo API.
 * Предоставляет функциональность для получения информации о погоде.
 */
class OpenMeteoAPIClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Получает информацию о погоде для заданных координат.
     *
     * @param latitude Широта в градусах (от -90 до 90)
     * @param longitude Долгота в градусах (от -180 до 180)
     * @return WeatherResponse содержащий информацию о погоде
     */
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        val response = client.get("https://api.open-meteo.com/v1/forecast") {
            url {
                parameters.append("latitude", latitude.toString())
                parameters.append("longitude", longitude.toString())
                parameters.append("hourly", "temperature_2m,relative_humidity_2m,precipitation,weather_code")
                parameters.append("timezone", "auto")
            }
        }
        return response.body()
    }

    /**
     * Закрывает HTTP клиент и освобождает ресурсы.
     * Должен быть вызван при завершении работы с клиентом.
     */
    fun close() {
        client.close()
    }
}

/**
 * Ответ от Open-Meteo API, содержащий информацию о погоде.
 */
@Serializable
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly: HourlyWeather,
    val hourly_units: HourlyUnits
)

/**
 * Почасовая информация о погоде.
 */
@Serializable
data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>,
    val precipitation: List<Double>,
    val weather_code: List<Int>
)

/**
 * Единицы измерения для почасовых данных.
 */
@Serializable
data class HourlyUnits(
    val time: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val precipitation: String,
    val weather_code: String
)
