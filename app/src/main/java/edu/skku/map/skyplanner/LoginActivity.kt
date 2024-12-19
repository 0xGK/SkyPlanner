package edu.skku.map.skyplanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.Arrays

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var loginManager: LoginManager

    data class FacebookUser(
        val id: String,
        val name: String,
        val email: String
    )
    data class LoginRequest(
        val user_id: String,
        val password: String,
        val action: String = "login" // 기본값 설정
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FacebookSdk.sdkInitialize(applicationContext)

        callbackManager = CallbackManager.Factory.create()
        loginManager = LoginManager.getInstance()

        val fbLoginBtn = findViewById<LoginButton>(R.id.fb_login_btn)
        val fbLoginLayout = findViewById<LinearLayout>(R.id.fb_login_layout)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)
        val userId = findViewById<EditText>(R.id.user_id)
        val userPassword = findViewById<EditText>(R.id.user_password)
        fbLoginBtn.isClickable = false


        val host = " https://1rzijajbg5.execute-api.ap-northeast-2.amazonaws.com/default/skyPlannerUser"
        loginBtn.setOnClickListener {
            val client = OkHttpClient()
            val gson = Gson()

            val userId = userId.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "ID와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // JSON 요청 생성
            val loginRequest = LoginRequest(user_id = userId, password = password)
            val jsonBody = gson.toJson(loginRequest)

            val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(host)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "로그인 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginError", e.message ?: "Unknown error")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "로그인 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }
                        // Lambda 응답 파싱
                        val responseBody = response.body?.string()
                        val responseJson = gson.fromJson(responseBody, Map::class.java)

                        val userName = responseJson["name"] as? String ?: "Unknown"
                        val pinNumber = responseJson["pin_number"] as? String ?: "Unknown"

                        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("userName", userName)
                        editor.putString("userId", userId)
                        editor.putString("pinNumber", pinNumber)
                        editor.apply()

                        // MainActivity로 이동
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // LoginActivity 종료
                    }
                }
            })


        }

        signUpBtn.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java).apply{
            }
            startActivity(intent)
        }

        // Facebook Callback 설정 - onCreate에서 한 번만 설정
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, "Facebook Login Error Occurred...", Toast.LENGTH_SHORT).show()
                Log.d("onError", "error : ${error.message}")
            }

            override fun onSuccess(result: LoginResult) {
                val graphRequest = GraphRequest.newMeRequest(result.accessToken) { fObject, response ->
                    if (response?.error != null) {
                        Log.e("GraphRequestError", response.error.toString())
                        return@newMeRequest
                    }

                    val gson = Gson()
                    // JSON 데이터를 파싱하여 Facebook 사용자 정보 객체 생성
                    val userInfo = gson.fromJson(fObject.toString(), FacebookUser::class.java)

                    Log.d("onSuccess", "User Info: ${userInfo.name}, ${userInfo.email}")

                    val userName = userInfo.name
                    val client = OkHttpClient()
//                    val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
//                    val editor = sharedPref.edit()
//                    editor.putBoolean("isLoggedIn", true)
//                    editor.putString("userName", userName)
//                    editor.putString("userId", userId)
//                    editor.apply()

                    // MainActivity로 이동
//                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                    startActivity(intent)
//                    finish() // LoginActivity 종료
                    val loginRequest = LoginRequest(user_id = userInfo.email, password = userInfo.id)
                    val jsonBody = gson.toJson(loginRequest)

                    val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

                    val request = Request.Builder()
                        .url(host)
                        .post(body)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "로그인 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("LoginError", e.message ?: "Unknown error")
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if (!response.isSuccessful) {
                                    runOnUiThread {
                                        Toast.makeText(this@LoginActivity, "최초 로그인 시, 핀 번호 설정이 필요합니다", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@LoginActivity, SignUpFaceBookActivity::class.java).apply{
                                            putExtra(MainActivity.EXT_USER_NAME, userName)
                                            putExtra(MainActivity.EXT_USER_ID, userInfo.email)
                                            putExtra(MainActivity.EXT_USER_PASSWORD, userInfo.id)
                                        }
                                        startActivity(intent)

                                    }
                                    return
                                }
                                // Lambda 응답 파싱
                                val responseBody = response.body?.string()
                                val responseJson = gson.fromJson(responseBody, Map::class.java)

                                val userName = responseJson["name"] as? String ?: "Unknown"
                                val pinNumber = responseJson["pin_number"] as? String ?: "Unknown"

                                val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.putString("userName", userName)
                                editor.putString("userId", userInfo.email)
                                editor.putString("pinNumber", pinNumber)
                                editor.apply()

                                // MainActivity로 이동
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish() // LoginActivity 종료
                            }
                        }
                    })
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                graphRequest.parameters = parameters
                graphRequest.executeAsync()
            }
        })

        // Facebook 로그인 버튼 클릭 이벤트
        fbLoginBtn.setOnClickListener {
            loginManager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        }
        fbLoginLayout.setOnClickListener {
            fbLoginBtn.performClick()
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = findViewById<EditText>(R.id.user_id)
        val userPassword = findViewById<EditText>(R.id.user_password)
        userId.text.clear()
        userPassword.text.clear()
        LoginManager.getInstance().logOut() // Facebook 로그아웃
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }



}