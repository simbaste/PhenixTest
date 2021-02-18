package fr.msg.simbaste.phenixtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import fr.msg.simbaste.phenixtest.model.CityWeather
import fr.msg.simbaste.phenixtest.service.RetrofitInstance
import fr.msg.simbaste.phenixtest.service.WeatherService
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var weatherService: WeatherService
    private var citiesWeatherLiveData = MutableLiveData<MutableList<CityWeather?>>()
    private val twoWeekDayCount: Int = 14
    private val weekDayCount: Int = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherService = RetrofitInstance
                .getRetrofitInstance()
                .create(WeatherService::class.java)

        backgroundTask()
        observeResult()

    }

    private fun getDayOfWeekNumber(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }

    private fun backgroundTask() {
        GlobalScope.launch(Dispatchers.Unconfined) {
            while (isActive) {
                try {
                    val dayOfWeekNumber = getDayOfWeekNumber()
                    val parisWeatherResponse = async { weatherService.getCitiesWeather("Paris", twoWeekDayCount-dayOfWeekNumber, BuildConfig.API_KEY) }
                    val lyonWeatherResponse = async { weatherService.getCitiesWeather("Lyon", twoWeekDayCount-dayOfWeekNumber, BuildConfig.API_KEY) }
                    val strasbourgWeatherResponse = async { weatherService.getCitiesWeather("Strasbourg", twoWeekDayCount-dayOfWeekNumber, BuildConfig.API_KEY) }

                    if (parisWeatherResponse.await().isSuccessful && lyonWeatherResponse.await().isSuccessful && strasbourgWeatherResponse.await().isSuccessful) {
                        citiesWeatherLiveData.postValue( mutableListOf(
                                parisWeatherResponse.await().body(),
                                lyonWeatherResponse.await().body(),
                                strasbourgWeatherResponse.await().body()
                        ) )
                    } else {
                        Log.e("MyTag", "Failed to fetch remote weather data")
                    }
                    Log.i("MyTags", "Task Completed")
                    delay(300000)
                } catch (e: Exception) {
                    Log.e("MyTag", "Network request failed")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Network request failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun getBiggerWeather(cityWeatherList: List<CityWeather?>, index: Int): CityWeather {
        if (cityWeatherList.count() == 1) return cityWeatherList.first()!!
        var bigger = cityWeatherList[0];
        if (bigger!!.list[index].temp.max < cityWeatherList[1]!!.list[index].temp.max) bigger = cityWeatherList[1]
        if (cityWeatherList.size == 3) {
            if (bigger!!.list[index].temp.max < cityWeatherList[2]!!.list[index].temp.max) bigger = cityWeatherList[2]
        }
        return bigger!!
    }

    private fun firstUpdateUI(cityWeather: CityWeather, index: Int) {
        if (cityWeather.city.id == 2988507) {
            findViewById<ImageView>(R.id.bgImageView).setImageResource(R.drawable.paris_bg)
        } else if (cityWeather.city.id == 2996943) {
            findViewById<ImageView>(R.id.bgImageView).setImageResource(R.drawable.lyon_bg)
        } else {
            findViewById<ImageView>(R.id.bgImageView).setImageResource(R.drawable.strasbourg_bg)
        }
        findViewById<TextView>(R.id.firstDayTextView).text = "Lundi"
        findViewById<TextView>(R.id.cityNameTextView).text = cityWeather.city.name
        findViewById<TextView>(R.id.weatherTextView).text = cityWeather.list[index].temp.max.toString()
    }

    private fun secondUpdateUI(cityWeather: CityWeather, index: Int) {
        findViewById<TextView>(R.id.secondDayTextView).text = "Mercredi"
        findViewById<TextView>(R.id.secondCityNameTextView).text = cityWeather.city.name
        findViewById<TextView>(R.id.secondWeatherTextView).text = cityWeather.list[index].temp.max.toString()
    }

    private fun thirdUpdateUi(cityWeather: CityWeather, index: Int) {
        findViewById<TextView>(R.id.thirdDayTextView).text = "Vendredi"
        findViewById<TextView>(R.id.thirdCityNameTextView).text = cityWeather.city.name
        findViewById<TextView>(R.id.thirdWeatherTextView).text = cityWeather.list[index].temp.max.toString()
    }

    private fun observeResult() {
        citiesWeatherLiveData.observe(this@MainActivity, Observer {

            val dayOfWeekNumber = getDayOfWeekNumber()

            if (it[0] != null && it[1] != null && it[2] != null) {

                it[0]!!.list = it[0]!!.list.subList(weekDayCount-dayOfWeekNumber, it[0]!!.list.count())
                it[1]!!.list = it[1]!!.list.subList(weekDayCount-dayOfWeekNumber, it[1]!!.list.count())
                it[2]!!.list = it[2]!!.list.subList(weekDayCount-dayOfWeekNumber, it[2]!!.list.count())

                displayWeathers(it)
            }
        })
    }

    private fun displayWeathers(cityWeatherList: MutableList<CityWeather?>) {
        Log.i("MyTag", "=========> BEFORE LOOP")
        var cityWeather = getBiggerWeather(cityWeatherList, 0)
        for (i in 0..6) {
            if (cityWeatherList.count() > 0)
                cityWeather = getBiggerWeather(cityWeatherList, i)

            if (i == 0) {
                //Lundi
                firstUpdateUI(cityWeather, i)
                Log.i("MyTag", "Lundi")
                logWeatherCity(cityWeather, i)
                cityWeatherList.removeAll { element -> element!!.city.id == cityWeather.city.id }
            } else if (i == 2) {
                //Mercredi
                secondUpdateUI(cityWeather, i)
                Log.i("MyTag", "Mercredi")
                logWeatherCity(cityWeather, i)
                cityWeatherList.removeAll { element -> element!!.city.id == cityWeather.city.id }
            } else if (i == 4) {
                //Vendredi
                thirdUpdateUi(cityWeather, i)
                Log.i("MyTag", "Vendredi")
                logWeatherCity(cityWeather, i)
                cityWeatherList.removeAll { element -> element!!.city.id == cityWeather.city.id }
            }
        }
    }

    private fun logWeatherCity(cityWeather: CityWeather, i: Int) {
        Log.i("MyTag", "${cityWeather.city.name} temp max ==> ${cityWeather.list[i].temp.max} || city id ==> ${cityWeather.city.id}")
        Log.i("MyTag", "...................................................")
        Log.i("MyTag", "...................................................")
    }
}