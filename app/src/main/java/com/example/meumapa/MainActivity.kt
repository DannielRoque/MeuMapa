package com.example.meumapa

import android.os.Bundle
import android.util.Log
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val codError =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when(codError){
            ConnectionResult.SERVICE_MISSING -> Log.e("Teste", "show Dialog")
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> Log.e("Teste", "show Dialog")
            ConnectionResult.SERVICE_DISABLED -> Log.e("Teste", "show Dialog")
            ConnectionResult.SUCCESS -> Log.e("Teste", "Service Up- in-date")
        }
    }
}
