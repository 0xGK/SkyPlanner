package edu.skku.map.skyplanner

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import java.security.MessageDigest
import java.util.Arrays

class MainActivity : AppCompatActivity() {
    companion object{
        const val EXT_DEPARTURE_LOCATION = "extra_key_departure_location"
        const val EXT_ARRIVAL_LOCATION = "extra_key_arrival_location"
        const val EXT_DEPARTURE_DATE = "extra_key_departure_date"
        const val EXT_ARRIVAL_DATE = "extra_key_arrival_date"
        const val EXT_ROUND_TRIP_OPTION = "extra_key_round_trip_option"
        const val EXT_FLIGHT_DETAIL = "extra_key_flight_detail"
    }
    private lateinit var callbackManager: CallbackManager
    private lateinit var loginManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(applicationContext)

        callbackManager = CallbackManager.Factory.create()
        loginManager = LoginManager.getInstance()

        val loginBtn = findViewById<LoginButton>(R.id.login_button)
        val textView = findViewById<TextView>(R.id.textView)
        loginBtn.setOnClickListener{

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
                    val graphRequest = GraphRequest.newMeRequest(result?.accessToken) { f_object, response ->
                        textView.text = "onSuccess: token: ${result?.accessToken!!.token} \n\n userObject: ${f_object}"
                        Log.d("onSuccess", "onSuccess: token: ${result?.accessToken!!.token} \n\n userObject: ${f_object.toString()} \n\n ${response.toString()}")
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,birthday")
                    graphRequest.parameters = parameters
                    graphRequest.executeAsync()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}