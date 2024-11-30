package edu.skku.map.skyplanner

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OneWayFlight(
    val departureDate: String,
    val arrivalDate: String,
    val airlineName: String,
    val ticketPrice: Int,
    val flightTime: Long,

    ): Parcelable {
}