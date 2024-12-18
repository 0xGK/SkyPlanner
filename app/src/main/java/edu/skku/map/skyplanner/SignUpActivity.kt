package edu.skku.map.skyplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

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
            val id = signUpId.text.toString().trim()
            val name = signUpName.text.toString().trim()
            val password = signUpPassword.text.toString().trim()
            val pinNumber = signUpPinNumber.text.toString().trim()

            // 입력값 검증
            if (id.isEmpty() || name.isEmpty() || password.isEmpty() || pinNumber.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pinNumber.length<6) {
                Toast.makeText(this, "PIN 번호는 6자리입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // JSON 요청 생성
            val signUpRequest = mapOf(
                "user_id" to id,
                "name" to name,
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
                        Toast.makeText(this@SignUpActivity, "회원가입 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val responseBody = response.body?.string()
                        runOnUiThread {
                            Toast.makeText(this@SignUpActivity, "성공적으로 회원가입 하셨습니다!", Toast.LENGTH_SHORT).show()
                            Log.d("SignUpResponse", responseBody ?: "Empty response")

                            // 회원가입 성공 시 LoginActivity로 이동
                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish() // SignUpActivity 종료
                        }
                    }
                }
            })
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