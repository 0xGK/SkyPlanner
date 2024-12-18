package edu.skku.map.skyplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReservationList(
    val flightId: Int,
    val departureDate: String,
    val arrivalDate: String,
    val airlineName: String,
    val ticketPrice: Int,
    val flightTime: Long,
    val departureLocation: String,
    val arrivalLocation: String,

) : Parcelable{
}