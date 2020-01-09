package com.example.meumapa

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = LocationServices.getFusedLocationProviderClient(this)


    }


    override fun onResume() {
        super.onResume()
        val codError =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when (codError) {
            ConnectionResult.SERVICE_MISSING or ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED or ConnectionResult.SERVICE_DISABLED -> {
                Log.e(
                    "Teste", "show Dialog"
                )
                GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, codError, 0, DialogInterface.OnCancelListener {
                        finish()
                    }).show()
            }
            ConnectionResult.SUCCESS -> Log.e("Teste", "Service Up- to - date")
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), LocationPermissionRequestCode
            )
        }
        client.lastLocation
            .addOnSuccessListener { sucess ->
                sucess?.let {
                    Log.e("Teste", "${it.latitude},     ${it.longitude}")
                }
            }
            .addOnFailureListener {

            }

        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.interval = 15 * 1000
        locationRequest.fastestInterval = 5 * 1000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
            Log.e("Teste", it.locationSettingsStates.isNetworkLocationPresent.toString())
            }
            .addOnFailureListener {e ->
                if (e is ResolvableApiException){
                 val resolvable  : ResolvableApiException = e
                    resolvable.startResolutionForResult(this, 10)
                }
            }

        val locationCallBack : LocationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult == null){
                    Log.e("Teste2", "local is null")
                }else{
                    for ( location in locationResult.locations){
                        Log.e("Teste2", "${location.latitude}")
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Log.e("Teste", "LocationAvailability ${locationAvailability?.isLocationAvailable}")
            }
        }

        client.requestLocationUpdates(locationRequest, locationCallBack, null)
    }

    private class AddressResultReceiver(handler: Handler?) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (resultData == null)return

            val addressOutPut = resultData.getString(RESULT_DATA_KEY)
            if (addressOutPut != null){



            }
            super.onReceiveResult(resultCode, resultData)
        }
    }
}
