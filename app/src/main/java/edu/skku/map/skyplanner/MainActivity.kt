package edu.skku.map.skyplanner

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    companion object{
        const val EXT_DEPARTURE_LOCATION = "extra_key_departure_location"
        const val EXT_ARRIVAL_LOCATION = "extra_key_arrival_location"
        const val EXT_DEPARTURE_DATE = "extra_key_departure_date"
        const val EXT_ARRIVAL_DATE = "extra_key_arrival_date"
        const val EXT_ROUND_TRIP_OPTION = "extra_key_round_trip_option"
        const val EXT_FLIGHT_DETAIL = "extra_key_flight_detail"
        const val EXT_USER_NAME = "extra_key_user_name"
    }
    val airportNames = mapOf(
        "ICN" to "인천국제공항",
        "SYD" to "시드니공항",
        "JFK" to "뉴욕존에프케네디공항"
    )
    data class LocationsResponse(
        val locations: List<String>
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // SharedPreferences에서 로그인 상태 확인
        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val userName = sharedPref.getString("userName", "")

        if (!isLoggedIn) {
            // 로그인이 안 되어 있으면 LoginActivity로 이동
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 로그인된 사용자 이름 출력
            Toast.makeText(this, "환영합니다, $userName!", Toast.LENGTH_SHORT).show()
        }

        setContentView(R.layout.activity_main)

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        val btnRoundTrip = findViewById<Button>(R.id.btnRoundTrip)
        val btnOneWay = findViewById<Button>(R.id.btnOneWay)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val editTextDepartureLocation = findViewById<AutoCompleteTextView>(R.id.editTextDepartureLocation)
        val editTextArrivalLocation = findViewById<AutoCompleteTextView>(R.id.editTextArrivalLocation)
        val editTextDepartureDate  = findViewById<EditText>(R.id.editTextDepartureDate)
        val editTextArrivalDate = findViewById<EditText>(R.id.editTextArrivalDate)
        val departureGroup = findViewById<LinearLayout>(R.id.departureGroup)
        val arrivalGroup = findViewById<LinearLayout>(R.id.arrivalGroup)
        val userNameText = findViewById<TextView>(R.id.user_name)
        userNameText.text = sharedPref.getString("userName", "")
        var roundTripOption = true
        var departureDate: Calendar? = null
        var arrivalDate: Calendar? = null

        setDatePicker(editTextDepartureDate) { selectedDate ->
            departureDate = selectedDate

            // 출발 날짜가 도착 날짜보다 이후라면 도착 날짜 초기화
            if (arrivalDate != null && departureDate!!.after(arrivalDate)) {
                editTextArrivalDate.text.clear()
                Toast.makeText(this, "출발 날짜는 도착 날짜보다 이전이어야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        setDatePicker(editTextArrivalDate) { selectedDate ->
            arrivalDate = selectedDate

            // 도착 날짜가 출발 날짜보다 이전이라면 도착 날짜 초기화
            if (departureDate != null && (arrivalDate!!.before(departureDate) || arrivalDate == departureDate)) {
                editTextArrivalDate.text.clear()
                Toast.makeText(this, "도착 날짜는 출발 날짜보다 이후이어야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        val host = "https://uhtcnsqync.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerInfo"
        val request = Request.Builder()
            .url(host)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("SignUpError", e.message ?: "Unknown error")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        return
                    }

                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val gson = Gson()
                            val locationsResponse = gson.fromJson(responseBody, LocationsResponse::class.java)
                            val airports = locationsResponse.locations

                            for(airport in airports){
                                airport+", "+ airportNames[airport]
                            }
                            val airportDisplayList = airports.map { airport ->
                                val airportName = airportNames[airport] ?: "Unknown Airport"
                                "$airport, $airportName"
                            }


                            runOnUiThread {
                                setupAutoCompleteTextView(editTextDepartureLocation, airportDisplayList)
                                setupAutoCompleteTextView(editTextArrivalLocation, airportDisplayList)
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Log.e("GsonError", e.message ?: "Error parsing JSON")
                            }
                        }
                    }
                }
            }
        })

        btnRoundTrip.setOnClickListener {
            roundTripOption = true

            // 출발일과 도착일 그룹 보이기
            departureGroup.visibility = View.VISIBLE
            arrivalGroup.visibility = View.VISIBLE
        }

        btnOneWay.setOnClickListener {
            roundTripOption = false

            // 출발일은 보이고 도착일은 숨기기
            departureGroup.visibility = View.VISIBLE
            arrivalGroup.visibility = View.GONE
            editTextArrivalDate.text.clear()
        }
        btnSearch.setOnClickListener{
            val departureLocation = editTextDepartureLocation.text.toString().trim()
            val arrivalLocation = editTextArrivalLocation.text.toString().trim()
            val departureDate = editTextDepartureDate.text.toString()
            val arrivalDate = editTextArrivalDate.text.toString()
            val missingFields = if (roundTripOption) {
                listOf(departureLocation, arrivalLocation, departureDate, arrivalDate)
            } else {
                listOf(departureLocation, arrivalLocation, departureDate)
            }

            if (missingFields.any { it.isEmpty() }) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (departureLocation == arrivalLocation) {
                Toast.makeText(this, "출발지와 도착지가 같을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, FlightActivity::class.java).apply{
                putExtra(EXT_DEPARTURE_LOCATION, departureLocation)
                putExtra(EXT_ARRIVAL_LOCATION, arrivalLocation)
                putExtra(EXT_DEPARTURE_DATE, departureDate)
                putExtra(EXT_ARRIVAL_DATE, arrivalDate)
                putExtra(EXT_ROUND_TRIP_OPTION, roundTripOption)
            }
            startActivity(intent)
        }
        btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear() // 로그인 상태 초기화
            editor.apply()

            // LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    fun setDatePicker(editText: EditText, onDateSelected: (Calendar) -> Unit) {
        // 기본 날짜로 오늘 날짜 설정
        val calendar = Calendar.getInstance()

        editText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                editText.context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // 선택한 날짜를 Calendar 객체에 저장
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    val date = "$selectedYear-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}"
                    editText.setText(date)

                    // 날짜 선택 후 추가 로직 실행
                    onDateSelected(calendar)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }
    }


    fun validateLocations(departure: String?, arrival: String?): Boolean {
        return departure != arrival
    }

    fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapter)

        // 항상 드롭다운이 표시되도록 설정
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.text.clear()
            autoCompleteTextView.postDelayed({
                autoCompleteTextView.showDropDown()
            }, 100) // 100ms 지연
        }
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.performClick()
            }
        }

        // 입력값에 따라 필터링
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {
                val editTextDepartureLocation = findViewById<AutoCompleteTextView>(R.id.editTextDepartureLocation)
                val editTextArrivalLocation = findViewById<AutoCompleteTextView>(R.id.editTextArrivalLocation)
                val departure = editTextDepartureLocation.text.toString().trim()
                val arrival = editTextArrivalLocation.text.toString().trim()

                if (!validateLocations(departure, arrival) && !arrival.isEmpty() && !departure.isEmpty()) {
                    autoCompleteTextView.error = "출발지와 목적지가 같을 수 없습니다."
                } else {
                    autoCompleteTextView.error = null
                }
            }
        })
    }
}