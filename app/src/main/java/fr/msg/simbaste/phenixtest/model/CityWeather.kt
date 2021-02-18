package fr.msg.simbaste.phenixtest.model


import com.google.gson.annotations.SerializedName

data class CityWeather(
    @SerializedName("city")
    val city: City,
    @SerializedName("cnt")
    val cnt: Int,
    @SerializedName("cod")
    val cod: String,
    @SerializedName("list")
    var list: List<WeatherInfo>,
    @SerializedName("message")
    val message: Double
)