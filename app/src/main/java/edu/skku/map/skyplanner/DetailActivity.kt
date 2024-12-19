package edu.skku.map.skyplanner

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.skku.map.skyplanner.model.OneWayFlight
import edu.skku.map.skyplanner.model.RoundTripFlight
import edu.skku.map.skyplanner.utils.AirlineUtils
import edu.skku.map.skyplanner.utils.DateUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val ticketPrice = findViewById<TextView>(R.id.totalPrice)
        @Suppress("DEPRECATION")
        val selectedItem: Parcelable? = intent.getParcelableExtra(MainActivity.EXT_FLIGHT_DETAIL)
        var isRound = false
        when (selectedItem) {
            is RoundTripFlight -> {
                // RoundTripFlight로 처리
                handleRoundTripFlight(selectedItem, ticketPrice)
                isRound = true
            }
            is OneWayFlight -> {
                // OneWayFlight로 처리
                handleOneWayFlight(selectedItem, ticketPrice)
            }
            else -> {
                // 알 수 없는 타입
                Log.e("DetailActivity", "Unknown data type received: ${selectedItem?.javaClass?.name}")
            }
        }

        val btnReservation = findViewById<Button>(R.id.btnReservation)
        btnReservation.setOnClickListener {
            showPinInputDialog {
                val client = OkHttpClient()
                val gson = Gson()
                val host = "https://0bkdikbvuj.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerReservation"

                val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                val userId = sharedPref.getString("userId", null)

                if (userId == null) {
                    Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    return@showPinInputDialog
                }

                val flightId = when (selectedItem) {
                    is RoundTripFlight -> selectedItem.flightId
                    is OneWayFlight -> selectedItem.flightId
                    else -> null
                }
                val returnFlightId = when (selectedItem) {
                    is RoundTripFlight -> selectedItem.returnFlightId
                    else -> null
                }

                if (flightId == null) {
                    Toast.makeText(this, "유효한 항공편이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    return@showPinInputDialog
                }

                // 예약 API 호출 함수
                fun callReservationApi(userId: String, flightId: Int, onComplete: () -> Unit) {
                    val reservationRequest = mapOf(
                        "user_id" to userId,
                        "flight_id" to flightId
                    )
                    val jsonBody = gson.toJson(reservationRequest)
                    val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

                    val request = Request.Builder()
                        .url(host)
                        .post(body)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@DetailActivity, "예약 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("ReservationError", e.message ?: "Unknown error")
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if (response.code == 400) {
                                    runOnUiThread {
                                        Toast.makeText(this@DetailActivity, "중복된 예약입니다.", Toast.LENGTH_SHORT).show()
                                    }
                                    return
                                }
                                if (!response.isSuccessful) {
                                    runOnUiThread {
                                        Toast.makeText(this@DetailActivity, "예약 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    return
                                }

                                onComplete()
                            }
                        }
                    })
                }

                // 첫 번째 예약 호출
                callReservationApi(userId, flightId) {
                    if (returnFlightId != null) {
                        // 두 번째 예약 호출
                        callReservationApi(userId, returnFlightId) {
                            // 모든 예약 완료 후 화면 전환
                            navigateToNextActivities()
                        }
                    } else {
                        // 한 번의 예약만 완료된 경우 화면 전환
                        navigateToNextActivities()
                    }
                }
            }
        }

        // 화면 전환 함수




        val btnBack = findViewById<Button>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }
    private fun navigateToNextActivities() {
        val intentToMain = Intent(this, MainActivity::class.java)
        intentToMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // MainActivity 위의 모든 Activity 제거
        startActivity(intentToMain)

        val intentToMyPage = Intent(this, MyPageActivity::class.java)
        startActivity(intentToMyPage)

        finish()
    }
    private fun handleRoundTripFlight(flight: RoundTripFlight, ticketPrice: TextView) {
        Log.d("DetailActivity", "RoundTripFlight: ${flight.flightTime}")
        val itemDetail1 = findViewById<View>(R.id.itemDetail1)
        val itemDetail2 = findViewById<View>(R.id.itemDetail2)
        itemDetail2.visibility = View.VISIBLE

        val startAirport1 = itemDetail1.findViewById<TextView>(R.id.startAirport)
        val endAirport1 = itemDetail1.findViewById<TextView>(R.id.endAirport)
        val startLocation1 = itemDetail1.findViewById<TextView>(R.id.startLocation)
        val endLocation1 = itemDetail1.findViewById<TextView>(R.id.endLocation)
        val startDate1 = itemDetail1.findViewById<TextView>(R.id.flightDepartureTime)
        val endDate1 = itemDetail1.findViewById<TextView>(R.id.flightArrivalTime)
        val duration1 = itemDetail1.findViewById<TextView>(R.id.flightTime)
        val airlineLogo1 = itemDetail1.findViewById<ImageView>(R.id.airlineLogo)
        val airlineName1 = itemDetail1.findViewById<TextView>(R.id.airlineName)

        val startAirport2 = itemDetail2.findViewById<TextView>(R.id.startAirport)
        val endAirport2 = itemDetail2.findViewById<TextView>(R.id.endAirport)
        val startLocation2 = itemDetail2.findViewById<TextView>(R.id.startLocation)
        val endLocation2 = itemDetail2.findViewById<TextView>(R.id.endLocation)
        val startDate2 = itemDetail2.findViewById<TextView>(R.id.flightDepartureTime)
        val endDate2 = itemDetail2.findViewById<TextView>(R.id.flightArrivalTime)
        val duration2 = itemDetail2.findViewById<TextView>(R.id.flightTime)
        val airlineLogo2 = itemDetail2.findViewById<ImageView>(R.id.airlineLogo)
        val airlineName2 = itemDetail2.findViewById<TextView>(R.id.airlineName)

        val logoResource1 = AirlineUtils.getAirlineLogoResource(flight.airlineName)
        airlineLogo1.setImageResource(logoResource1)

        val logoResource2 = AirlineUtils.getAirlineLogoResource(flight.returnAirlineName)
        airlineLogo2.setImageResource(logoResource2)

        airlineName1.text = flight.airlineName
        startAirport1.text = flight.departureLocation
        endAirport1.text = flight.arrivalLocation
        startLocation1.text = AirlineUtils.getAirportNames(flight.departureLocation)
        endLocation1.text = AirlineUtils.getAirportNames(flight.arrivalLocation)
        startDate1.text = DateUtils.formatTimeKorean(flight.departureDate)
        endDate1.text = DateUtils.formatTimeKorean(flight.arrivalDate)
        duration1.text = DateUtils.parseFlightTime(flight.flightTime)
        val ticketPrice1 = itemDetail1.findViewById<TextView>(R.id.ticketPrice)
        ticketPrice1.text = DateUtils.formatCurrencyKorean(flight.ticketPrice)
        airlineName2.text = flight.returnAirlineName
        startAirport2.text = flight.arrivalLocation
        endAirport2.text = flight.departureLocation
        startLocation2.text = AirlineUtils.getAirportNames(flight.arrivalLocation)
        endLocation2.text = AirlineUtils.getAirportNames(flight.departureLocation)
        startDate2.text = DateUtils.formatTimeKorean(flight.returnDepartureDate)
        endDate2.text = DateUtils.formatTimeKorean(flight.returnArrivalDate)
        duration2.text = DateUtils.parseFlightTime(flight.returnFlightTime)
        val ticketPrice2 = itemDetail2.findViewById<TextView>(R.id.ticketPrice)
        ticketPrice2.text = DateUtils.formatCurrencyKorean(flight.returnTicketPrice)
        ticketPrice.text = DateUtils.formatCurrencyKorean(flight.ticketPrice+flight.returnTicketPrice)
    }

    private fun handleOneWayFlight(flight: OneWayFlight, ticketPrice: TextView) {
//        Log.d("DetailActivity", "OneWayFlight: ${flight.flightId}")
        val itemDetail2 = findViewById<View>(R.id.itemDetail2)
        itemDetail2.visibility = View.GONE

        val itemDetail1 = findViewById<View>(R.id.itemDetail1)
        val startAirport1 = itemDetail1.findViewById<TextView>(R.id.startAirport)
        val endAirport1 = itemDetail1.findViewById<TextView>(R.id.endAirport)
        val startLocation1 = itemDetail1.findViewById<TextView>(R.id.startLocation)
        val endLocation1 = itemDetail1.findViewById<TextView>(R.id.endLocation)
        val startDate1 = itemDetail1.findViewById<TextView>(R.id.flightDepartureTime)
        val endDate1 = itemDetail1.findViewById<TextView>(R.id.flightArrivalTime)
        val duration1 = itemDetail1.findViewById<TextView>(R.id.flightTime)
        val airlineName1 = itemDetail1.findViewById<TextView>(R.id.airlineName)
        val airlineLogo1 = itemDetail1.findViewById<ImageView>(R.id.airlineLogo)
        val ticketPrice1 = itemDetail1.findViewById<TextView>(R.id.ticketPrice)

        val logoResource = AirlineUtils.getAirlineLogoResource(flight.airlineName)
        airlineLogo1.setImageResource(logoResource)

        airlineName1.text = flight.airlineName
        startAirport1.text = flight.departureAirport
        endAirport1.text = flight.arrivalAirport
        startLocation1.text = AirlineUtils.getAirportNames(flight.departureAirport)
        endLocation1.text = AirlineUtils.getAirportNames(flight.arrivalAirport)
        startDate1.text = DateUtils.formatTimeKorean(flight.departureDate)
        endDate1.text = DateUtils.formatTimeKorean(flight.arrivalDate)
        duration1.text = DateUtils.parseFlightTime(flight.flightTime)
        ticketPrice1.text =DateUtils.formatCurrencyKorean(flight.ticketPrice)
        ticketPrice.text = DateUtils.formatCurrencyKorean(flight.ticketPrice)
    }

    private fun showPinInputDialog(onSuccess: () -> Unit) {
        // Custom Layout Inflate
        val dialogView = layoutInflater.inflate(R.layout.pin_input_dialog, null)

        val pinInput = dialogView.findViewById<EditText>(R.id.pin_input)

        // AlertDialog 생성
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("결제") { _, _ ->
                val enteredPin = pinInput.text.toString()
                if (enteredPin.length == 6) {
                    val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                    val userPinNumber = sharedPref.getString("pinNumber", "")
                    if (userPinNumber == enteredPin) {
                        onSuccess() // 콜백 호출
                    } else {
                        Toast.makeText(this, "PIN 번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "6자리 PIN 번호를 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

}