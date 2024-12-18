package edu.skku.map.skyplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OneWayFlight(
    val flightId: Int,
    val departureDate: String,
    val arrivalDate: String,
    val airlineName: String,
    val ticketPrice: Int,
    val flightTime: Long,
    val departureAirport: String,
    val arrivalAirport: String,
    ): Parcelable {
}