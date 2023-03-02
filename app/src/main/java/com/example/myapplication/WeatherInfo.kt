package com.example.myapplication


data class WeatherInfo(val time:String = "",
                       val condition: String = "",
                       val currentTemp:String = "",
                       val maxTemp: String = "",
                       val minTemp: String = "",
                       val iconUrl: String,
                       val isDay:Boolean = false
)

data class CurrentDayInfo(
    var name: String = "",
    var region: String= "",
    var country: String= "",
    var currentTime: String= "",
    var currentHour: String= "",
    var currentTemp: String= "",
    var currentCondition: String= "",
    var currentIconUrl: String= "",
)