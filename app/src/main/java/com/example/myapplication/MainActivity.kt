package com.example.myapplication

import android.Manifest
import android.app.DownloadManager.Request
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONArray
import org.json.JSONObject

import com.squareup.picasso.Picasso
import java.time.LocalDate

const val LONG_DAY_SIZE = 16
const val SHORT_DAY_SIZE = 15
const val API_KEY = "fb1327bc346e4b2288c183543230203"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var plauncher: ActivityResultLauncher<String>
    private lateinit var hoursAdapter: WeatherAdapter
    private lateinit var daysAdapter: WeatherAdapter

    private lateinit var fLocationClient: FusedLocationProviderClient

    private var currentDay = CurrentDayInfo()
    private var  daysList = mutableListOf<WeatherInfo>()
    private var hoursList = mutableListOf<WeatherInfo>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()
        fLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        binding.locationButton.setOnClickListener {
            checkLockation()
        }

        binding.searchButton.setOnClickListener {
            DialogManager.searchByNameDialog(this, object: DialogManager.Listener{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(name: String?) {
                    if (name != null) {
                        requestData(name)
                    }
                }
            })
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        checkLockation()

    }

    override fun onStart() {
        super.onStart()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkLockation(){
        if (isLocationEnabled()){
            getLocation()
        }
        else{
            DialogManager.locationSettingsDialog(this, object: DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }
    private fun isLocationEnabled(): Boolean{
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocation(){
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token).
        addOnCompleteListener {
            requestData("${it.result.latitude}, ${it.result.longitude}")
        }

    }


    private fun mainInit() = with(binding){
        dayTextView.text = currentDay.currentTime
        locationTextView.text = "${currentDay.country}, ${currentDay.name}"
        timeTextView.text = currentDay.currentHour
        tempTextView.text = "${currentDay.currentTemp}°C "
        weatherTypeTextView.text = currentDay.currentCondition
        Picasso.get().load(currentDay.currentIconUrl).into(iconImageView)
    }

    private fun RcViewInit() = with(binding){
        rvHours.layoutManager = LinearLayoutManager(applicationContext,  LinearLayoutManager.HORIZONTAL, false)
        rvDays.layoutManager = LinearLayoutManager(applicationContext,  LinearLayoutManager.HORIZONTAL, false)
        hoursAdapter = WeatherAdapter()
        rvHours.adapter = hoursAdapter
        hoursAdapter.submitList(hoursList)

        daysAdapter = WeatherAdapter()
        rvDays.adapter = daysAdapter
        daysAdapter.submitList(daysList)
    }

    private fun permissionListener(){
        plauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){}
    }

    private fun checkPermission(){
        if(!isPermitionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            plauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        //Log.d("MyLog", "Permission checked")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestData(city: String){
        val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=7&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(com.android.volley.Request.Method.GET, url,
            {response->
                val jsonObj = JSONObject(response)
                parseCurrentData(jsonObj)
                parseHours(jsonObj)
                parseDays(jsonObj)
                RcViewInit()

            },
            {
            }
        )
        queue.add(stringRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseCurrentData(jsonObj: JSONObject){
        val date = jsonObj.getJSONObject("location").getString("localtime")
        currentDay.currentTime = date.slice(0 .. 9)
        if (date.length == LONG_DAY_SIZE) currentDay.currentHour = date.slice(11 .. 15)
        else currentDay.currentHour = date.slice(11 .. 14)

        currentDay.name = jsonObj.getJSONObject("location").getString("name")
        currentDay.region = jsonObj.getJSONObject("location").getString("region")
        currentDay.country = jsonObj.getJSONObject("location").getString("country")
        currentDay.currentTemp = jsonObj.getJSONObject("current").getString("temp_c")
        currentDay.currentCondition = jsonObj.getJSONObject("current").getJSONObject("condition").getString("text")
        currentDay.currentIconUrl = "https:" + jsonObj.getJSONObject("current").getJSONObject("condition").getString("icon")

        mainInit()
    }

    private fun parseHours(jsonObj: JSONObject) {
        var hours = mutableListOf<WeatherInfo>()
        val forecastday: JSONArray = jsonObj.getJSONObject("forecast").getJSONArray("forecastday")
        val firstDayHours = forecastday.getJSONObject(0).getJSONArray("hour")

        for (index in 0 .. 23){
            val time = firstDayHours.getJSONObject(index).getString("time").slice(11 .. 15)
            val temp = firstDayHours.getJSONObject(index).getString("temp_c")
            val condition = firstDayHours.getJSONObject(index).getJSONObject("condition").getString("text")
            val iconUrl = "https:" + firstDayHours.getJSONObject(index).getJSONObject("condition").getString("icon")
            hours.add(WeatherInfo(time=time, currentTemp=temp, condition=condition, iconUrl=iconUrl))

        }
        hoursList = hours
    }

    private fun parseDays(jsonObj: JSONObject) {
        val  days = mutableListOf<WeatherInfo>()
        val forecastday: JSONArray = jsonObj.getJSONObject("forecast").getJSONArray("forecastday")
        for (index in 0 ..6){
            val date = forecastday.getJSONObject(index).getString("date")
            val maxTemp = forecastday.getJSONObject(index).getJSONObject("day").getString("maxtemp_c")
            val minTemp = forecastday.getJSONObject(index).getJSONObject("day").getString("mintemp_c")
            val condition = forecastday.getJSONObject(index).getJSONObject("day").getJSONObject("condition").getString("text")
            val iconUrl = "https:" + forecastday.getJSONObject(index).getJSONObject("day").getJSONObject("condition").getString("icon")
            val day = WeatherInfo(time=date, condition=condition, maxTemp=maxTemp, minTemp = minTemp, iconUrl = iconUrl, isDay = true)
            days.add(day)
        }
        daysList = days
    }
}