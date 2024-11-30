package edu.skku.map.skyplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.skku.map.skyplanner.MainActivity.Companion.EXT_FLIGHT_DETAIL
import edu.skku.map.skyplanner.adapter.OneWayFlightAdapter
import edu.skku.map.skyplanner.adapter.RoundTripFlightAdapter
import edu.skku.map.skyplanner.database.DatabaseHelper
import edu.skku.map.skyplanner.model.OneWayFlight
import edu.skku.map.skyplanner.model.RoundTripFlight
import edu.skku.map.skyplanner.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale

class FlightActivity : AppCompatActivity() {
    val itemsOneWay = ArrayList<OneWayFlight>()
    val itemsRoundTrip = ArrayList<RoundTripFlight>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight)

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val listView = findViewById<ListView>(R.id.listViewFlight)


        val btnBack = findViewById<Button>(R.id.btn_back)
        val textAirport = findViewById<TextView>(R.id.textAirport)
        val textResultCnt = findViewById<TextView>(R.id.textResultCnt)
        val textDepartureDate = findViewById<TextView>(R.id.textDepartureDate)
        val btnSortPrice = findViewById<Button>(R.id.btnSortPrice)
        val btnSortDepartureDate = findViewById<Button>(R.id.btnSortDepartureDate)
//        val btnSortArrivalDate = findViewById<Button>(R.id.btnSortArrivalDate)
        val btnSortTime = findViewById<Button>(R.id.btnSortTime)


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
            textDepartureDate.text = formatDateToKoreanStyle(intent.getStringExtra(MainActivity.EXT_DEPARTURE_DATE).toString())+" - " +
                    formatDateToKoreanStyle(intent.getStringExtra(MainActivity.EXT_ARRIVAL_DATE).toString())

            val departureCursor = db.rawQuery(
                "SELECT * FROM Flight WHERE departure_location = ? AND arrival_location = ? AND date(departure_date) = ?",
                arrayOf(departureLocation, arrivalLocation, departureDate)
            )

            val returnCursor = db.rawQuery(
                "SELECT * FROM Flight WHERE departure_location = ? AND arrival_location = ? AND date(departure_date) = ?",
                arrayOf(arrivalLocation, departureLocation, arrivalDate)
            )

            var cnt = 0
            val departureFlights = mutableListOf<RoundTripFlight>()
            while (departureCursor.moveToNext()) {
                val departureDate = departureCursor.getString(departureCursor.getColumnIndexOrThrow("departure_date"))
                val arrivalDate = departureCursor.getString(departureCursor.getColumnIndexOrThrow("arrival_date"))
                val airline = departureCursor.getString(departureCursor.getColumnIndexOrThrow("airline"))
                val price = departureCursor.getInt(departureCursor.getColumnIndexOrThrow("price"))
                val flightTime = DateUtils.calculateFlightTime(departureDate, arrivalDate)

                departureFlights.add(
                    RoundTripFlight(
                        departureDate, arrivalDate, airline, price, flightTime,
                        "", "", "", 0, 0
                    )
                )
            }

            // 도착편과 출발편의 모든 경우의 수 조합
            while (returnCursor.moveToNext()) {
                val returnDepartureDate = returnCursor.getString(returnCursor.getColumnIndexOrThrow("departure_date"))
                val returnArrivalDate = returnCursor.getString(returnCursor.getColumnIndexOrThrow("arrival_date"))
                val returnAirline = returnCursor.getString(returnCursor.getColumnIndexOrThrow("airline"))
                val returnPrice = returnCursor.getInt(returnCursor.getColumnIndexOrThrow("price"))
                val returnFlightTime = DateUtils.calculateFlightTime(returnDepartureDate, returnArrivalDate)

                // 출발편과 반환편의 모든 조합 생성
                for (departureFlight in departureFlights) {
                    itemsRoundTrip.add(
                        RoundTripFlight(
                            departureFlight.departureDate,
                            departureFlight.arrivalDate,
                            departureFlight.airlineName,
                            departureFlight.ticketPrice,
                            departureFlight.flightTime,

                            returnDepartureDate,
                            returnArrivalDate,
                            returnAirline,
                            returnPrice,
                            returnFlightTime
                        )
                    )
                    cnt++
                }
            }

            departureCursor.close()
            returnCursor.close()
            textResultCnt.text="결과 $cnt 개"

            if(roundTripOption){
                itemsRoundTrip.sortBy{it.ticketPrice+it.returnTicketPrice}
            }else{
                itemsOneWay.sortBy{it.ticketPrice}
            }
            updateListView(roundTripOption, listView);
        }
//      one way
        else{
            textDepartureDate.text = formatDateToKoreanStyle(intent.getStringExtra(MainActivity.EXT_DEPARTURE_DATE).toString())
            val cursor = db.rawQuery(
                "SELECT * FROM Flight WHERE departure_location = ? AND arrival_location = ? AND date(departure_date) = ?",
                arrayOf(departureLocation, arrivalLocation, departureDate)
            )
            var cnt=0;
            while (cursor.moveToNext()) {
                val departureDate = cursor.getString(cursor.getColumnIndexOrThrow("departure_date"))
                val arrivalDate = cursor.getString(cursor.getColumnIndexOrThrow("arrival_date"))
                val airline = cursor.getString(cursor.getColumnIndexOrThrow("airline"))
                val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
                val flightTime = DateUtils.calculateFlightTime(departureDate, arrivalDate)
                // Flight 객체 추가
                itemsOneWay.add(
                    OneWayFlight(
                        departureDate,
                        arrivalDate,
                        airline,
                        price,
                        flightTime
                    )
                )
                cnt+=1
            }
            cursor.close()
            textResultCnt.text="결과 $cnt 개"

            if(roundTripOption){
                itemsRoundTrip.sortBy{it.ticketPrice+it.returnTicketPrice}
            }else{
                itemsOneWay.sortBy{it.ticketPrice}
            }
            updateListView(roundTripOption, listView);
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

    private fun formatDateToKoreanStyle(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA) // 입력 형식
        val outputFormat = SimpleDateFormat("MM월 dd일", Locale.KOREA)  // 출력 형식

        return try {
            val date = inputFormat.parse(dateString) // 문자열을 Date로 변환
            outputFormat.format(date)               // 변환된 날짜 반환
        } catch (e: Exception) {
            "날짜 오류" // 파싱 실패 시 기본값 반환
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
                    putExtra("selected_item", selectedItem) // 데이터 전달
                }
                startActivity(intent)
            }
        }
    }

}