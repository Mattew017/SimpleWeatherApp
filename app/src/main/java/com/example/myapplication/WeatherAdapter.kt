package com.example.myapplication

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.RvElementBinding
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.LocalTime


class WeatherAdapter: ListAdapter<WeatherInfo, WeatherAdapter.Holder>(Comparator()) {
    class Holder(view: View): RecyclerView.ViewHolder(view){
        private val binding = RvElementBinding.bind(view)
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: WeatherInfo) = with(binding){


            rvConditionTextView.text = item.condition
            if (item.isDay){
                var date = LocalDate.parse(item.time)
                rvDateTextView.text = "${date.dayOfWeek}, ${date.dayOfMonth}"
                rvTempTextView.text = "${item.minTemp}/${item.maxTemp}°C"
            }
            else{
                rvDateTextView.text = item.time
                rvTempTextView.text = "${item.currentTemp}°C"
            }
            Picasso.get().load(item.iconUrl).into(rvIconImageView)


        }
    }

    class Comparator: DiffUtil.ItemCallback<WeatherInfo>(){
        override fun areItemsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_element, parent, false)
        return Holder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }
}