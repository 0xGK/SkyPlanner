package edu.skku.map.skyplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_FLIGHT_DETAIL
import edu.skku.map.skyplanner.adapter.OneWayFlightAdapter
import edu.skku.map.skyplanner.adapter.RoundTripFlightAdapter
import edu.skku.map.skyplanner.model.OneWayFlight
import edu.skku.map.skyplanner.model.RoundTripFlight
import edu.skku.map.skyplanner.utils.DateUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class FlightActivity : AppCompatActivity() {
    val itemsOneWay = ArrayList<OneWayFlight>()
    val itemsRoundTrip = ArrayList<RoundTripFlight>()
    private val host = "https://xscy9x9o3d.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerFlight"
    data class OneWayRequest(
        val departure_location: String,
        val arrival_location: String,
        val departure_date: String,
        val round_trip: Boolean = false,
        val is_reservation: Boolean = false
    )
    data class RoundTripRequest(
        val departure_location: String,
        val arrival_location: String,
        val departure_date: String,
        val arrival_date: String,
        val round_trip: Boolean = true,
        val is_reservation: Boolean = false
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight)
        val listView = findViewById<ListView>(R.id.listViewFlight)
        val btnBack = findViewById<Button>(R.id.btn_back)
        val textAirport = findViewById<TextView>(R.id.textAirport)
        val textResultCnt = findViewById<TextView>(R.id.textResultCnt)
        val textDepartureDate = findViewById<TextView>(R.id.textDepartureDate)
        val btnSortPrice = findViewById<Button>(R.id.btnSortPrice)
        val btnSortDepartureDate = findViewById<Button>(R.id.btnSortDepartureDate)
//        val btnSortArrivalDate = findViewById<Button>(R.id.btnSortArrivalDate)
        val btnSortTime = findViewById<Button>(R.id.btnSortTime)

        textDepartureDate.text = intent.getStringExtra(MainActivity.EXT_DEPARTURE_DATE)
        val roundTripOption = intent.getBooleanExtra(MainActivity.EXT_ROUND_TRIP_OPTION, true)

        val departureLocationString = intent.getStringExtra(MainActivity.EXT_DEPARTURE_LOCATION)
        val departureParts = departureLocationString?.split(", ") ?: listOf("Unknown", "Unknown")
        val departureLocation = departureParts[0]
        val depatureAirport = departureParts[1]

        val arrivalLocationString = intent.getStringExtra(MainActivity.EXT_ARRIVAL_LOCATION)
        val arrivalParts = arrivalLocationString?.split(", ") ?: listOf("Unknown", "Unknown")
        val arrivalLocation = arrivalParts[0]
        val arrivalAirport = arrivalParts[1]



        val departureDate = intent.getStringExtra(MainActivity.EXT_DEPARTURE_DATE)
        val arrivalDate = intent.getStringExtra(MainActivity.EXT_ARRIVAL_DATE)

        textAirport.text = "$depatureAirport($departureLocation) - $arrivalAirport($arrivalLocation)"


        if(roundTripOption){
            textDepartureDate.text = intent.getStringExtra(MainActivity.EXT_DEPARTURE_DATE)+" - "+intent.getStringExtra(MainActivity.EXT_ARRIVAL_DATE)
            val roundTripRequest = RoundTripRequest(
                departure_location = departureLocation,
                arrival_location = arrivalLocation,
                departure_date = departureDate.toString(),
                arrival_date = arrivalDate.toString()
            )

            val gson = Gson()
            val jsonBody = gson.toJson(roundTripRequest)

            Log.d("FlightActivity", "Request JSON: $jsonBody") // 요청 JSON 로그 추가

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(host)
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("FlightActivity", "Request failed: ${e.message}") // 실패 로그
                    runOnUiThread {
                        Toast.makeText(this@FlightActivity, "왕복 항공편 검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        Log.d("FlightActivity", "Response Code: ${response.code}") // 응답 코드 로그
                        if (!response.isSuccessful) {
                            Log.e("FlightActivity", "Response failed: ${response.message}") // 실패 응답 로그
                            runOnUiThread {
                                Toast.makeText(this@FlightActivity, "왕복 항공편 검색 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val responseBody = response.body?.string()
                        Log.d("FlightActivity", "Response Body: $responseBody") // 응답 본문 로그

                        try {
                            val responseJson = gson.fromJson(responseBody, Map::class.java)
                            Log.d("FlightActivity", "Parsed Response JSON: $responseJson") // 파싱된 JSON 로그

                            val count = responseJson["count"] as? Double ?: 0.0
                            val flights = responseJson["flights"] as? List<Map<String, Any>> ?: emptyList()

                            runOnUiThread {
                                textResultCnt.text = "결과 ${count.toInt()} 개"
                                itemsRoundTrip.clear()

                                flights.forEach { flight ->
                                    try {
                                        val departure = flight["departure"] as Map<String, Any>
                                        val returnFlight = flight["return"] as Map<String, Any>

                                        val roundTripFlight = RoundTripFlight(
                                            departureDate = departure["departure_date"] as String,
                                            arrivalDate = departure["arrival_date"] as String,
                                            airlineName = departure["airline"] as String,
                                            ticketPrice = (departure["price"] as Double).toInt(),
                                            flightTime = DateUtils.calculateFlightTime(
                                                departure["departure_date"] as String,
                                                departure["arrival_date"] as String
                                            ),
                                            flightId = (departure["id"] as Double).toInt(),
                                            departureLocation = departureLocation,
                                            arrivalLocation = arrivalLocation,
                                            returnDepartureDate = returnFlight["departure_date"] as String,
                                            returnArrivalDate = returnFlight["arrival_date"] as String,
                                            returnAirlineName = returnFlight["airline"] as String,
                                            returnTicketPrice = (returnFlight["price"] as Double).toInt(),
                                            returnFlightTime = DateUtils.calculateFlightTime(
                                                returnFlight["departure_date"] as String,
                                                returnFlight["arrival_date"] as String
                                            ),
                                            returnFlightId = (returnFlight["id"] as Double).toInt(),
                                        )

                                        itemsRoundTrip.add(roundTripFlight)
                                        Log.d("FlightActivity", "Mapped RoundTripFlight: $roundTripFlight") // 매핑된 객체 로그
                                    } catch (e: Exception) {
                                        Log.e("FlightActivity", "Error mapping flight data: ${e.message}", e) // 매핑 오류 로그
                                    }
                                }

                                itemsRoundTrip.sortBy { it.ticketPrice + it.returnTicketPrice }
                                updateListView(roundTripOption, listView)
                            }
                        } catch (e: Exception) {
                            Log.e("FlightActivity", "Error parsing response: ${e.message}", e) // JSON 파싱 오류 로그
                            runOnUiThread {
                                Toast.makeText(this@FlightActivity, "데이터 처리 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
//      one way
        else{

            val oneWayRequest = OneWayRequest(
                departure_location = departureLocation,
                arrival_location = arrivalLocation,
                departure_date = departureDate.toString()
            )
            val gson = Gson()
            val jsonBody = gson.toJson(oneWayRequest)

            Log.d("FlightActivity", "Request JSON: $jsonBody") // 요청 JSON 로그 추가

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(host)
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("FlightActivity", "Request failed: ${e.message}") // 실패 로그 추가
                    runOnUiThread {
                        Toast.makeText(this@FlightActivity, "항공편 검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        Log.d("FlightActivity", "Response Code: ${response.code}") // 응답 코드 로그 추가

                        if (!response.isSuccessful) {
                            Log.e("FlightActivity", "Response failed: ${response.message}") // 실패 응답 로그 추가
                            runOnUiThread {
                                Toast.makeText(this@FlightActivity, "항공편 검색 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val responseBody = response.body?.string()
                        Log.d("FlightActivity", "Response Body: $responseBody") // 응답 본문 로그 추가

                        val responseJson = gson.fromJson(responseBody, Map::class.java)
                        val count = responseJson["count"] as? Double ?: 0.0
                        val flights = responseJson["flights"] as? List<Map<String, Any>> ?: emptyList()

                        runOnUiThread {
                            textResultCnt.text = "결과 ${count.toInt()} 개"
                            itemsOneWay.clear()

                            try {
                                itemsOneWay.addAll(
                                    flights.map { flight ->
                                        val departureDate = flight["departure_date"] as String
                                        val arrivalDate = flight["arrival_date"] as String
                                        val airlineName = flight["airline"] as String
                                        val ticketPrice = (flight["price"] as Double).toInt() // Double -> Int 변환
                                        val flightTime = DateUtils.calculateFlightTime(departureDate, arrivalDate)
                                        val flightId = (flight["id"] as Double).toInt()
                                        OneWayFlight(
                                            departureDate = departureDate,
                                            arrivalDate = arrivalDate,
                                            airlineName = airlineName,
                                            ticketPrice = ticketPrice,
                                            flightTime = flightTime,
                                            departureAirport = departureLocation,
                                            arrivalAirport = arrivalLocation,
                                            flightId = flightId,
                                        )
                                    }
                                )

                                if (roundTripOption) {
                                    itemsRoundTrip.sortBy { it.ticketPrice + it.returnTicketPrice }
                                } else {
                                    itemsOneWay.sortBy { it.ticketPrice }
                                }

                                updateListView(roundTripOption, listView)
                            } catch (e: Exception) {
                                Toast.makeText(this@FlightActivity, "데이터 처리 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("FlightActivity", "Error processing flight data", e)
                            }
                        }

                    }
                }
            })
        }


        btnBack.setOnClickListener{
            finish()
        }
        btnSortPrice.setOnClickListener{
            if(roundTripOption){
                itemsRoundTrip.sortBy{it.ticketPrice+it.returnTicketPrice}
            }else{
                itemsOneWay.sortBy{it.ticketPrice}
            }
            updateListView(roundTripOption, listView);
        }
        btnSortDepartureDate.setOnClickListener{
            if(roundTripOption){
                itemsRoundTrip.sortBy{it.departureDate}
            }else{
                itemsOneWay.sortBy{it.departureDate}
            }
            updateListView(roundTripOption, listView);
        }
        btnSortTime.setOnClickListener{
            if(roundTripOption){
                itemsRoundTrip.sortBy{it.flightTime+it.returnFlightTime}
            }else{
                itemsOneWay.sortBy{it.flightTime}
            }
            updateListView(roundTripOption, listView);
        }


    }


    private fun updateListView(roundTripOption: Boolean, listView: ListView) {

        if(roundTripOption){
            val adapter = RoundTripFlightAdapter(itemsRoundTrip, applicationContext)
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                // 클릭된 아이템 데이터 가져오기
                val selectedItem = itemsRoundTrip[position]

                // 새로운 Activity로 이동
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(EXT_FLIGHT_DETAIL, selectedItem)

                }
                startActivity(intent)
            }

        } else {
            val adapter = OneWayFlightAdapter(itemsOneWay, applicationContext)
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                // 클릭된 아이템 데이터 가져오기
                val selectedItem = itemsOneWay[position]

                // 새로운 Activity로 이동
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(EXT_FLIGHT_DETAIL, selectedItem) // 데이터 전달
                }
                startActivity(intent)
            }
        }
    }

}