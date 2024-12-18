package edu.skku.map.skyplanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.skku.map.skyplanner.model.OneWayFlight
import edu.skku.map.skyplanner.R
import edu.skku.map.skyplanner.utils.AirlineUtils
import edu.skku.map.skyplanner.utils.DateUtils
class ReservationListAdapter(val data:ArrayList<OneWayFlight>, val context: Context):BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val generatedView = inflater.inflate(R.layout.item_reservation, null)

        val airlineName = generatedView.findViewById<TextView>(R.id.airlineName)
        val airlineLogo = generatedView.findViewById<ImageView>(R.id.airlineLogo)
        val flightDepartureDate = generatedView.findViewById<TextView>(R.id.flightDepartureDate)
        val flightArrivalDate = generatedView.findViewById<TextView>(R.id.flightArrivalDate)
        val flightTime = generatedView.findViewById<TextView>(R.id.flightTime)
        val startAirport = generatedView.findViewById<TextView>(R.id.startAirport)
        val endAirport = generatedView.findViewById<TextView>(R.id.endAirport)
        val startLocation = generatedView.findViewById<TextView>(R.id.startLocation)
        val endLocation = generatedView.findViewById<TextView>(R.id.endLocation)
        startAirport.text = data[p0].departureAirport
        endAirport.text = data[p0].arrivalAirport
        startLocation.text = AirlineUtils.getAirportNames(data[p0].departureAirport)
        endLocation.text = AirlineUtils.getAirportNames(data[p0].arrivalAirport)
        val ticketPrice = generatedView.findViewById<TextView>(R.id.ticketPrice)
//        val flightDiffDate = generatedView.findViewById<TextView>(R.id.flightDiffDate)

        val departureDate = data[p0].departureDate
        val arrivalDate = data[p0].arrivalDate
        val diffDate = DateUtils.calculateDayDifference(departureDate, arrivalDate)
        airlineName.text = data[p0].airlineName
        flightDepartureDate.text = DateUtils.formatTimeKorean(departureDate)
        flightArrivalDate.text = DateUtils.formatTimeKorean(arrivalDate)
        ticketPrice.text = DateUtils.formatCurrencyKorean(data[p0].ticketPrice)
        flightTime.text = DateUtils.parseFlightTime(data[p0].flightTime)
//        flightDiffDate.text = if (diffDate > 0) "+$diffDate" else ""

        val logoResource = AirlineUtils.getAirlineLogoResource(data[p0].airlineName)
        airlineLogo.setImageResource(logoResource)

        return generatedView
    }



}