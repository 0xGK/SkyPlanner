package edu.skku.map.skyplanner.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "flight.db" // 데이터베이스 이름
        const val DATABASE_VERSION = 1      // 데이터베이스 버전
        const val TAG = "FlightDatabaseHelper" // 로그 태그
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성 쿼리
        val createFlightTableQuery = """
            CREATE TABLE Booking (
                user_id varchar(255) PRIMARY KEY,
                departure_location varchar(255) NOT NULL,
                arrival_location varchar(255) NOT NULL,
                departure_date datetime NOT NULL,
                arrival_date datetime NOT NULL,
                airline varchar(255) NOT NULL,
                price int NOT NULL
            )
        """
        db.execSQL(createFlightTableQuery)
//        val createUserTableQuery = """
//            CREATE TABLE User (
//                user_id varchar(255) PRIMARY KEY,
//                password varchar(255) NOT NULL,
//                name varchar(255) NOT NULL,
//                pin_number varchar(255) NOT NULL
//            )
//        """
//        db.execSQL(createUserTableQuery)
//
//        insertInitialQuery(db)
//
//
//
//        Log.d(TAG, "Database created successfully!") // 데이터베이스 생성 로그
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Flight")
        onCreate(db)
        Log.d(TAG, "Upgraded") // 업그레이드 로그
    }

    fun insertInitialQuery(db: SQLiteDatabase){
        val insertDataQueries = """
INSERT INTO Flight (departure_location, arrival_location, departure_date, arrival_date, airline, price) VALUES
('ICN', 'JFK', '2024-11-01 18:45', '2024-11-02 10:20', '제주항공', 879000),
('ICN', 'JFK', '2024-11-01 13:50', '2024-11-02 04:05', '아시아나항공', 1766000);

        """
        db.execSQL(insertDataQueries)
    }
}
