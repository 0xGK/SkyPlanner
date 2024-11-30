package edu.skku.map.skyplanner

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoundTripFlight(
    val departureDate: String,
    val arrivalDate: String,
    val airlineName: String,
    val ticketPrice: Int,
    val flightTime: Long,
    val returnDepartureDate: String,
    val returnArrivalDate: String,
    val returnAirlineName: String,
    val returnTicketPrice: Int,
    val returnFlightTime: Long
) : Parcelable
