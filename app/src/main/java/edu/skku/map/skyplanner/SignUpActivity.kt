package edu.skku.map.skyplanner

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.skku.map.skyplanner.database.DatabaseHelper

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        val backBtn = findViewById<Button>(R.id.back_btn)
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)
        val signUpId = findViewById<EditText>(R.id.sign_up_id)
        val signUpName = findViewById<EditText>(R.id.sign_up_name)
        val signUpPassword = findViewById<EditText>(R.id.sign_up_password)
        val signUpPinNumber = findViewById<EditText>(R.id.sign_up_pin_number)

        signUpBtn.setOnClickListener {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase

            val id = signUpId.text.toString().trim()
            val name = signUpName.text.toString().trim()
            val password = signUpPassword.text.toString().trim()
            val pinNumber = signUpPinNumber.text.toString().trim()

            // 입력값 검증
            if (id.isEmpty() || name.isEmpty() || password.isEmpty() || pinNumber.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 확인
            val cursor = db.rawQuery("SELECT * FROM User WHERE user_id = ?", arrayOf(id))
            if (cursor.moveToFirst()) {
                Toast.makeText(this, "이미 존재하는 ID입니다.", Toast.LENGTH_SHORT).show()
                cursor.close()
                db.close()
                return@setOnClickListener
            }
            cursor.close()

            // 중복되지 않으면 삽입
            val contentValues = ContentValues().apply {
                put("user_id", id)
                put("name", name)
                put("password", password)
                put("pin_number", pinNumber)
            }

            val result = db.insert("User", null, contentValues)
            if (result != -1L) {
                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                finish() // 현재 액티비티 종료
            } else {
                Toast.makeText(this, "회원가입 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }

            db.close()
        }



        backBtn.setOnClickListener{
            finish()
        }

    }
    override fun onResume() {
        super.onResume()
        val signUpId = findViewById<EditText>(R.id.sign_up_id)
        val signUpName = findViewById<EditText>(R.id.sign_up_name)
        val signUpPassword = findViewById<EditText>(R.id.sign_up_password)
        val signUpPinNumber = findViewById<EditText>(R.id.sign_up_pin_number)
        signUpId.text.clear()
        signUpName.text.clear()
        signUpPassword.text.clear()
        signUpPinNumber.text.clear()
    }
}