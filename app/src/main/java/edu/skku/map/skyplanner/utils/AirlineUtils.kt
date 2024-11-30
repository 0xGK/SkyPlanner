package edu.skku.map.skyplanner.utils

import edu.skku.map.skyplanner.R

object AirlineUtils {

    // 항공사 이름에 따른 로고 리소스를 반환
    fun getAirlineLogoResource(airlineName: String): Int {
        return when (airlineName) {
            "대한항공" -> R.drawable.korean_air_logo
            "진에어항공" -> R.drawable.jin_air_logo
            "티웨이항공" -> R.drawable.tway_air_logo
            "제주항공" -> R.drawable.jeju_air_logo
            "아시아나항공" -> R.drawable.asiana_air_logo
            else -> R.drawable.default_air_logo // 기본 로고 (없을 경우 대체)
        }
    }
}
