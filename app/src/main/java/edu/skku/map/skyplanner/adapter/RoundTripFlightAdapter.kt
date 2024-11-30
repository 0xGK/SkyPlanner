package edu.skku.map.skyplanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.skku.map.skyplanner.R
import edu.skku.map.skyplanner.model.RoundTripFlight
import edu.skku.map.skyplanner.utils.AirlineUtils
import edu.skku.map.skyplanner.utils.DateUtils

class RoundTripFlightAdapter(val data:ArrayList<RoundTripFlight>, val context: Context):BaseAdapter() {
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
        val generatedView = inflater.inflate(R.layout.item_round_trip_flight, null)

        val airlineName = generatedView.findViewById<TextView>(R.id.airlineName)
        val ticketPrice = generatedView.findViewById<TextView>(R.id.ticketPrice)
        val totalFlightTime = generatedView.findViewById<TextView>(R.id.totalFlightTime)

        val flightItem1 = generatedView.findViewById<View>(R.id.flightItem1)
        val airlineLogo = flightItem1.findViewById<ImageView>(R.id.airlineLogo)
        val flightDepartureDate = flightItem1.findViewById<TextView>(R.id.flightDepartureDate)
        val flightArrivalDate = flightItem1.findViewById<TextView>(R.id.flightArrivalDate)
        val flightTime = flightItem1.findViewById<TextView>(R.id.flightTime)
        val flightDiffDate = flightItem1.findViewById<TextView>(R.id.flightDiffDate)


        val flightItem2 = generatedView.findViewById<View>(R.id.flightItem2)
        val returnAirlineLogo = flightItem2.findViewById<ImageView>(R.id.airlineLogo)
        val returnFlightDepartureDate = flightItem2.findViewById<TextView>(R.id.flightDepartureDate)
        val returnFlightArrivalDate = flightItem2.findViewById<TextView>(R.id.flightArrivalDate)
        val returnFlightTime = flightItem2.findViewById<TextView>(R.id.flightTime)
        val returnFlightDiffDate = flightItem2.findViewById<TextView>(R.id.flightDiffDate)


        airlineName.text = if (data[p0].airlineName == data[p0].returnAirlineName) {
            data[p0].airlineName
        } else {
            "${data[p0].airlineName} & ${data[p0].returnAirlineName}"
        }
        ticketPrice.text = DateUtils.formatCurrency(data[p0].ticketPrice+data[p0].returnTicketPrice)
        totalFlightTime.text = "총" + DateUtils.parseFlightTime(data[p0].flightTime+data[p0].returnFlightTime)

        val departureDate = data[p0].departureDate
        val arrivalDate = data[p0].arrivalDate
        val diffDate = DateUtils.calculateDayDifference(departureDate, arrivalDate)
        val logoResource = AirlineUtils.getAirlineLogoResource(data[p0].airlineName)
        airlineLogo.setImageResource(logoResource)
        flightDepartureDate.text = DateUtils.formatTimeTo12Hour(departureDate)
        flightArrivalDate.text = DateUtils.formatTimeTo12Hour(arrivalDate)
        flightTime.text = DateUtils.parseFlightTime(data[p0].flightTime)
        flightDiffDate.text = if (diffDate > 0) "+$diffDate" else ""

        val returnDepartureDate = data[p0].returnDepartureDate
        val returnArrivalDate = data[p0].returnArrivalDate
        val returnDiffDate = DateUtils.calculateDayDifference(returnDepartureDate, returnArrivalDate)
        val returnLogoResource = AirlineUtils.getAirlineLogoResource(data[p0].returnAirlineName)
        returnAirlineLogo.setImageResource(returnLogoResource)
        returnFlightDepartureDate.text = DateUtils.formatTimeTo12Hour(returnDepartureDate)
        returnFlightArrivalDate.text = DateUtils.formatTimeTo12Hour(returnArrivalDate)
        returnFlightTime.text = DateUtils.parseFlightTime(data[p0].returnFlightTime)
        returnFlightDiffDate.text = if (returnDiffDate > 0) "+$returnDiffDate" else ""


        return generatedView
    }

}