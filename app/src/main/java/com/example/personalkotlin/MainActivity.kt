package com.example.personalkotlin

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.wifi.WifiManager
import android.net.wifi.rtt.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(this, "OnRequestPermissionsResultCallback", Toast.LENGTH_LONG).show()
        var isGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED}
        setGranted(isGranted)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var allowedField: TextView;
    private var latitude : Double = 0.0;
    private var longtitude: Double = 0.0;
    private var permissionGranted: Boolean = false;
    private var accessPointList = listOf(object : {
        val floor = "L8"
    })

    private fun setGranted(isGranted: Boolean) {
        permissionGranted = isGranted
        Toast.makeText(this, "setGranted: $isGranted", Toast.LENGTH_LONG).show()
        if (isGranted) {
            allowedField.text = "Granted";
        } else {
            allowedField.text = "Not Granted";
        }
    }

    private fun getLocation() : Unit {
        fusedLocationClient.lastLocation.addOnSuccessListener {
                location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longtitude = location.longitude
            }

            Toast.makeText(this, "Latitude: $latitude, Longtitude: $longtitude", Toast.LENGTH_LONG).show()
        }
    }
    private fun checkPermissionGranted() : Boolean{
//      Check permission and set text "Granted" Or not
        setGranted(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        return permissionGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT);
        setContentView(R.layout.activity_main);
//      https://developer.android.com/reference/kotlin/androi
//      d/widget/TextView
        var mainField: TextView = findViewById(R.id.main_text_view)
        var secondaryField: TextView = findViewById(R.id.secondary_text_view)
        allowedField = findViewById(R.id.allowed_text_view);
        var permissionButton: Button = findViewById(R.id.get_permission_button);
        var getLocationButton: Button = findViewById(R.id.get_location_button);
        var permissionsRequired: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        mainField.text = if(isSupported) "RTT Supported" else "RTT Not Supported"

        checkPermissionGranted()

//        Wifi RTT
        if (isSupported) {
            var wifiRttManager = getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager;

            val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
            val filter = IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)
            val myReceiver = object: BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (wifiRttManager.isAvailable) {
                        secondaryField.text = "RTT Available"
                    } else {
                        secondaryField.text = "RTT Not Available"
                    }
                }
            }
            registerReceiver(myReceiver, filter)

            if (wifiRttManager.isAvailable && permissionGranted) {
                var results = wifiManager.scanResults.filter { it.is80211mcResponder }
                println(results)
                val req: RangingRequest = RangingRequest.Builder().run {
                    addAccessPoints(results)
                    build()
                }

                Toast.makeText(this, "Checking Permission Granted", Toast.LENGTH_LONG).show()
                if (checkPermissionGranted()) {
                    try {
                        var that = this

                    wifiRttManager.startRanging(req, application.mainExecutor, object: RangingResultCallback() {
                        override fun onRangingFailure(code: Int) {
                            println(code)
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onRangingResults(results: MutableList<RangingResult>) {
//                            println(results)
                            results.forEach {

                            }
                            Toast.makeText(that, results.toString(), Toast.LENGTH_LONG).show()
                        }
                    })
                    } catch (e: SecurityException) {
                        Toast.makeText(this, "SecurityException", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

        //        FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        Get location permission
        permissionButton.setOnClickListener { ActivityCompat.requestPermissions(this, permissionsRequired, 1) }

//      Get Last Location
        getLocationButton.setOnClickListener {
            getLocation()
        }
    }
}
