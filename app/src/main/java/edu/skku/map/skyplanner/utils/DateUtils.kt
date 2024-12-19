package edu.skku.map.skyplanner.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    // 비행 시간 계산
    fun calculateFlightTime(departure: String, arrival: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

        val departureDate = dateFormat.parse(departure)
        val arrivalDate = dateFormat.parse(arrival)
        val durationMillis = arrivalDate.time - departureDate.time
        return durationMillis
    }
    // 비행 시간 계산
    fun parseFlightTime(durationMillis: Long): String {
        return try {
            val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60

            "${hours}시간 ${minutes}분"
        } catch (e: Exception) {
            "시간 계산 오류"
        }
    }

    // 가격 포맷 (1,000,000원 형태)
    fun formatCurrencyKorean(price: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
        return numberFormat.format(price) + "원"
    }
    fun formatCurrency(price: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
        return numberFormat.format(price)
    }

    // 날짜 차이 계산 (일 단위)
    fun calculateDayDifference(startDateString: String, endDateString: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        return try {
            val startDate = dateFormat.parse(startDateString)
            val endDate = dateFormat.parse(endDateString)
            val diffMillis = endDate.time - startDate.time
            TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
        } catch (e: Exception) {
            -1
        }
    }

    // 24시간 형식을 12시간 형식으로 변환
    fun formatTimeTo12Hour(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        val outputFormat = SimpleDateFormat("a h:mm", Locale.KOREA)

        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            "시간 오류"
        }
    }

    fun formatTimeKorean(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)

        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            "시간 오류"
        }
    }
    fun formatTimeKoreanDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            "시간 오류"
        }
    }


}
