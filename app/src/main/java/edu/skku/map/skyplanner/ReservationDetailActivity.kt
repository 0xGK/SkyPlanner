package edu.skku.map.skyplanner

import android.os.Bundle
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

class ReservationDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_detail)
        @Suppress("DEPRECATION")
        val selectedItem: OneWayFlight? = intent.getParcelableExtra(MainActivity.EXT_FLIGHT_DETAIL)

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

        when (selectedItem) {
            is OneWayFlight -> {
                val logoResource = AirlineUtils.getAirlineLogoResource(selectedItem.airlineName)
                airlineLogo1.setImageResource(logoResource)

                airlineName1.text = selectedItem.airlineName
                startAirport1.text = selectedItem.departureAirport
                endAirport1.text = selectedItem.arrivalAirport
                startLocation1.text = AirlineUtils.getAirportNames(selectedItem.departureAirport)
                endLocation1.text = AirlineUtils.getAirportNames(selectedItem.arrivalAirport)
                startDate1.text = DateUtils.formatTimeKorean(selectedItem.departureDate)
                endDate1.text = DateUtils.formatTimeKorean(selectedItem.arrivalDate)
                duration1.text = DateUtils.parseFlightTime(selectedItem.flightTime)
                ticketPrice1.text =DateUtils.formatCurrencyKorean(selectedItem.ticketPrice)

            }
        }

        val btnCancle = findViewById<Button>(R.id.btnCancle)
        btnCancle.setOnClickListener {
            showPinInputDialog {
                val client = OkHttpClient()
                val gson = Gson()
                val host = "https://ujy2cwi2ke.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerCancelReservation"

                val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                val userId = sharedPref.getString("userId", null)

                if (userId == null) {
                    Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    return@showPinInputDialog
                }

                val flightId = selectedItem?.flightId
                if (flightId == null) {
                    Toast.makeText(this, "유효하지 않은 항공편입니다", Toast.LENGTH_SHORT).show()
                    return@showPinInputDialog
                }

                // JSON 요청 생성
                val cancelRequest = mapOf(
                    "user_id" to userId,
                    "flight_id" to flightId
                )
                val jsonBody = gson.toJson(cancelRequest).toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(host)
                    .post(jsonBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@ReservationDetailActivity, "예약 취소 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                runOnUiThread {
                                    Toast.makeText(this@ReservationDetailActivity, "예약 취소 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                                }
                                return
                            }

                            runOnUiThread {
                                Toast.makeText(this@ReservationDetailActivity, "예약이 성공적으로 취소되었습니다.", Toast.LENGTH_SHORT).show()
                                finish() // 현재 Activity 종료
                            }
                        }
                    }
                })
            }
        }




        val btnBack = findViewById<Button>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }




    private fun showPinInputDialog(onSuccess: () -> Unit) {
        // Custom Layout Inflate
        val dialogView = layoutInflater.inflate(R.layout.pin_input_dialog, null)

        val pinInput = dialogView.findViewById<EditText>(R.id.pin_input)

        // AlertDialog 생성
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("예약 취소") { _, _ ->
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