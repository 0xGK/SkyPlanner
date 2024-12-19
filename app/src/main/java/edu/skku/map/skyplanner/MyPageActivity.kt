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
import edu.skku.map.skyplanner.adapter.ReservationListAdapter
import edu.skku.map.skyplanner.model.OneWayFlight
import edu.skku.map.skyplanner.utils.DateUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
class MyPageActivity : AppCompatActivity() {
    val itemsOneWay = ArrayList<OneWayFlight>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        val btnBack = findViewById<Button>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        // 초기 데이터 로드
        loadMileageAndReservations()
    }

    override fun onResume() {
        super.onResume()

        // Resume 시 데이터 갱신
        loadMileageAndReservations()
    }

    private fun loadMileageAndReservations() {
        val mileage = findViewById<TextView>(R.id.mileage)
        val listView = findViewById<ListView>(R.id.listViewFlight)
        val textResultCnt = findViewById<TextView>(R.id.textResultCnt)
        val userName = findViewById<TextView>(R.id.user_name)

        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "null")
        userName.text =sharedPref.getString("userName", "null")

        if (userId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val gson = Gson()

        // Mileage 조회 요청
        val mileageRequest = mapOf("user_id" to userId)
        val mileageRequestBody = gson.toJson(mileageRequest).toRequestBody("application/json; charset=utf-8".toMediaType())
        val mileageApi = "https://uhtcnsqync.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerInfo"

        val mileageHttpRequest = Request.Builder()
            .url(mileageApi)
            .post(mileageRequestBody)
            .build()

        client.newCall(mileageHttpRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MyPageActivity, "Mileage 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@MyPageActivity, "Mileage 조회 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    val responseBody = response.body?.string()
                    val responseJson = gson.fromJson(responseBody, Map::class.java)
                    val userMileage = (responseJson["mileage"] as? Number)?.toInt() ?: 0

                    Log.d("MileageResponse", "Response Body: $responseBody")

                    runOnUiThread {
                        mileage.text = DateUtils.formatCurrency(userMileage)
                    }
                }
            }
        })

        // 항공편 예약 조회 요청
        val reservationApi = "https://xscy9x9o3d.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerFlight"
        val reservationRequest = mapOf("user_id" to userId, "is_reservation" to true)
        val reservationRequestBody = gson.toJson(reservationRequest).toRequestBody("application/json; charset=utf-8".toMediaType())

        val reservationHttpRequest = Request.Builder()
            .url(reservationApi)
            .post(reservationRequestBody)
            .build()

        client.newCall(reservationHttpRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MyPageActivity, "항공편 검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@MyPageActivity, "항공편 검색 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val responseBody = response.body?.string()
                    Log.d("MyPageActivity", "Response Body: $responseBody")

                    val responseJson = gson.fromJson(responseBody, Map::class.java)
                    val count = responseJson["count"] as? Double ?: 0.0
                    val flights = responseJson["flights"] as? List<Map<String, Any>> ?: emptyList()

                    runOnUiThread {
                        textResultCnt.text = "결과 ${count.toInt()} 개"
                        itemsOneWay.clear()

                        itemsOneWay.addAll(
                            flights.map { flight ->
                                val departureDate = flight["departure_date"] as String
                                val arrivalDate = flight["arrival_date"] as String
                                val airlineName = flight["airline"] as String
                                val ticketPrice = (flight["price"] as Double).toInt()
                                val flightTime = DateUtils.calculateFlightTime(departureDate, arrivalDate)
                                val flightId = (flight["id"] as Double).toInt()
                                val departureLocation = flight["departure_location"] as String
                                val arrivalLocation = flight["arrival_location"] as String
                                OneWayFlight(
                                    departureDate = departureDate,
                                    arrivalDate = arrivalDate,
                                    airlineName = airlineName,
                                    ticketPrice = ticketPrice,
                                    flightTime = flightTime,
                                    departureAirport = departureLocation,
                                    arrivalAirport = arrivalLocation,
                                    flightId = flightId
                                )
                            }
                        )

                        itemsOneWay.sortBy { it.departureDate }
                        val adapter = ReservationListAdapter(itemsOneWay, applicationContext)
                        listView.adapter = adapter
                    }
                }
            }



        }


        )

        listView.setOnItemClickListener { _, _, position, _ ->
            // 클릭된 아이템 데이터 가져오기
            val selectedItem = itemsOneWay[position]

            // 새로운 Activity로 이동
            val intent = Intent(this, ReservationDetailActivity::class.java).apply {
                putExtra(EXT_FLIGHT_DETAIL, selectedItem)

            }
            startActivity(intent)
        }
    }
}
