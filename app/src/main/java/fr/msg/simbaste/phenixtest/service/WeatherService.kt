package fr.msg.simbaste.phenixtest.service

import fr.msg.simbaste.phenixtest.model.CityWeather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("/data/2.5/forecast/daily")
    suspend fun getCitiesWeather(
        @Query("q") countryName: String,
        @Query("cnt") dailyCount: Int,
        @Query("appid") apiKey: String
    ): Response<CityWeather>

}