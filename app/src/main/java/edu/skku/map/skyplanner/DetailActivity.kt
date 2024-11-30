package edu.skku.map.skyplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.skku.map.skyplanner.model.RoundTripFlight

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        @Suppress("DEPRECATION")
        val selectedItem: RoundTripFlight? = intent.getParcelableExtra(MainActivity.EXT_FLIGHT_DETAIL)

        if (selectedItem != null) {
            println("Flight Detail: $selectedItem")
        }
    }
}