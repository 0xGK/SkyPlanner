package edu.skku.map.skyplanner

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_ARRIVAL_DATE
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_ARRIVAL_LOCATION
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_DEPARTURE_DATE
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_DEPARTURE_LOCATION
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_ROUND_TRIP_OPTION
import java.util.Calendar

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val airportNames = mapOf(
            "ICN" to "인천국제공항",
            "SYD" to "시드니공항",
            "JFK" to "뉴욕존에프케네디공항"
        )

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val departureCursor = db.rawQuery("SELECT DISTINCT departure_location FROM Flight ORDER BY departure_location", null)
        val departureAirports = mutableListOf<String>()
        while (departureCursor.moveToNext()) {
            val location = departureCursor.getString(departureCursor.getColumnIndexOrThrow("departure_location"))
            val airportName = airportNames[location] ?: "Unknown Airport"
            departureAirports.add("$location, $airportName")
        }
        val arrivalCursor = db.rawQuery("SELECT DISTINCT arrival_location FROM Flight ORDER BY arrival_location", null)
        val arrivalAirports = mutableListOf<String>()
        while (arrivalCursor.moveToNext()) {
            val location = arrivalCursor.getString(arrivalCursor.getColumnIndexOrThrow("arrival_location"))
            val airportName = airportNames[location] ?: "Unknown Airport"
            arrivalAirports.add("$location, $airportName")
        }

        departureCursor.close()
        arrivalCursor.close()
        db.close()

        val btnRoundTrip = findViewById<Button>(R.id.btnRoundTrip)
        val btnOneWay = findViewById<Button>(R.id.btnOneWay)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val editTextDepartureLocation = findViewById<AutoCompleteTextView>(R.id.editTextDepartureLocation)
        val editTextArrivalLocation = findViewById<AutoCompleteTextView>(R.id.editTextArrivalLocation)
        val editTextDepartureDate  = findViewById<EditText>(R.id.editTextDepartureDate)
        val editTextArrivalDate = findViewById<EditText>(R.id.editTextArrivalDate)
        val departureGroup = findViewById<LinearLayout>(R.id.departureGroup)
        val arrivalGroup = findViewById<LinearLayout>(R.id.arrivalGroup)
        val userName = findViewById<TextView>(R.id.user_name)
        userName.text = intent.getStringExtra(MainActivity.EXT_USER_NAME)
        setDatePicker(editTextDepartureDate)
        setDatePicker(editTextArrivalDate)
        var roundTripOption = true


        setupAutoCompleteTextView(editTextDepartureLocation, departureAirports)
        setupAutoCompleteTextView(editTextArrivalLocation, arrivalAirports)


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
        }
        btnSearch.setOnClickListener{
            val intent = Intent(this, FlightActivity::class.java).apply{
                putExtra(EXT_DEPARTURE_LOCATION, editTextDepartureLocation.text.toString().trim())
                putExtra(EXT_ARRIVAL_LOCATION, editTextArrivalLocation.text.toString().trim())
                putExtra(EXT_DEPARTURE_DATE, editTextDepartureDate.text.toString())
                putExtra(EXT_ARRIVAL_DATE, editTextArrivalDate.text.toString())
                putExtra(EXT_ROUND_TRIP_OPTION, roundTripOption)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    fun setDatePicker(editText: EditText) {
        // 오늘 날짜 기본값 설정
        val calendar = Calendar.getInstance()

        editText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                editText.context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "$selectedYear-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}"
                    editText.setText(date)
                },
                year,
                month,
                day
            )

            // DatePickerDialog 표시
            datePickerDialog.show()
        }
    }

    fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapter)

        // 항상 드롭다운이 표시되도록 설정
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
            }
        }

        // 입력값에 따라 필터링
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}