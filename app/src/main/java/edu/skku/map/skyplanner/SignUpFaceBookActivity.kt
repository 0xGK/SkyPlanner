package edu.skku.map.skyplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class SignUpFaceBookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_face_book)
        val signUpId = findViewById<TextView>(R.id.sign_up_id)
        val signUpName = findViewById<TextView>(R.id.sign_up_name)
        val signUpPinNumber = findViewById<EditText>(R.id.sign_up_pin_number)
        val backBtn = findViewById<Button>(R.id.back_btn)
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)
        val userId = intent.getStringExtra(MainActivity.EXT_USER_ID)
        signUpId.text = userId
        val userName = intent.getStringExtra(MainActivity.EXT_USER_NAME)
        signUpName.text = userName
        val password = intent.getStringExtra(MainActivity.EXT_USER_PASSWORD)


        signUpBtn.setOnClickListener {
            val pinNumber = signUpPinNumber.text.toString()
            if (pinNumber.length<6) {
                Toast.makeText(this, "PIN 번호는 6자리입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // JSON 요청 생성
            val signUpRequest = mapOf(
                "user_id" to userId,
                "name" to userName,
                "password" to password,
                "pin_number" to pinNumber,
                "action" to "signup"
            )
            val gson = Gson()
            val jsonBody = gson.toJson(signUpRequest)

            val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            // Lambda API URL

            val host = "https://1rzijajbg5.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerUser"
            val request = Request.Builder()
                .url(host)
                .post(body)
                .build()

            // OkHttp로 요청 전송
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SignUpFaceBookActivity, "회원가입 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("SignUpError", e.message ?: "Unknown error")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            runOnUiThread {
                                val errorMessage = when (response.code) {
                                    409 -> "이미 존재하는 사용자 ID입니다."
                                    else -> "회원가입 실패: ${response.message}"
                                }
                                Toast.makeText(this@SignUpFaceBookActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val responseBody = response.body?.string()
                        runOnUiThread {
                            Toast.makeText(this@SignUpFaceBookActivity, "성공적으로 회원가입 하셨습니다!", Toast.LENGTH_SHORT).show()
                            Log.d("SignUpResponse", responseBody ?: "Empty response")

                            val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putBoolean("isLoggedIn", true)
                            editor.putString("userName", userName)
                            editor.putString("userId", userId)
                            editor.putString("pinNumber", pinNumber)
                            editor.apply()

                            // MainActivity로 이동
                            val intent = Intent(this@SignUpFaceBookActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish() // LoginActivity 종료
                        }
                    }
                }
            })
    }

    backBtn.setOnClickListener{
        finish()
    }
    }
}