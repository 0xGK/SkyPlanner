package edu.skku.map.skyplanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import okhttp3.*
import okio.IOException
import java.util.Arrays

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
    private lateinit var callbackManager: CallbackManager
    private lateinit var loginManager: LoginManager

    data class FacebookUser(
        val id: String,
        val name: String,
        val email: String
    )
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_gray)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(applicationContext)

        callbackManager = CallbackManager.Factory.create()
        loginManager = LoginManager.getInstance()

        val fbLoginBtn = findViewById<LoginButton>(R.id.fb_login_btn)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)
        val userId = findViewById<EditText>(R.id.user_id)
        val userPassword = findViewById<EditText>(R.id.user_password)

        loginBtn.setOnClickListener {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase
            val id = userId.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cursor = db.rawQuery(
                "SELECT * FROM User WHERE user_id = ? AND password = ?",
                arrayOf(id, password)
            )

            if (cursor.moveToFirst()) {
                val userName = cursor.getString(cursor.getColumnIndexOrThrow("name"))

                val intent = Intent(this@MainActivity, SearchActivity::class.java).apply {
                    putExtra(EXT_USER_NAME, userName)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }

            cursor.close()
            db.close()
        }

        signUpBtn.setOnClickListener{
            val intent = Intent(this@MainActivity, SignUpActivity::class.java).apply{
            }
            startActivity(intent)
        }

        fbLoginBtn.setOnClickListener{
            loginManager.logInWithReadPermissions(this,Arrays.asList("public_profile", "email"))
            loginManager.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Toast.makeText(this@MainActivity, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@MainActivity, "Facebook Login Error Occurred...", Toast.LENGTH_SHORT).show()
                    Log.d("onError", "error : ${error.toString()}")
                }

                override fun onSuccess(result: LoginResult) {
                    val graphRequest = GraphRequest.newMeRequest(result.accessToken) { f_object, response ->
                        if (response?.error != null) {
                            Log.e("GraphRequestError", response.error.toString())
                            return@newMeRequest
                        }

                        // GSON 객체 초기화
                        val gson = com.google.gson.Gson()

                        // JSON 데이터를 파싱하여 사용자 정보 객체 생성
                        val userInfo = gson.fromJson(f_object.toString(), FacebookUser::class.java)

                        // 로그 확인
                        Log.d("onSuccess", "User Info: ${userInfo.name}, ${userInfo.email}")

                        // Intent로 데이터 전달
                        val intent = Intent(this@MainActivity, SearchActivity::class.java).apply{
                            putExtra(EXT_USER_NAME, userInfo.name)

                        }
                        startActivity(intent)
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,birthday")
                    graphRequest.parameters = parameters
                    graphRequest.executeAsync()


                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = findViewById<EditText>(R.id.user_id)
        val userPassword = findViewById<EditText>(R.id.user_password)
        userId.text.clear()
        userPassword.text.clear()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


   
}