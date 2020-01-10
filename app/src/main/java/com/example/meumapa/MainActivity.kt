package com.example.meumapa

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var client: FusedLocationProviderClient
    lateinit var resultReceiver: AddressResultReceiver
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        client = LocationServices.getFusedLocationProviderClient(this)
        resultReceiver = AddressResultReceiver(null)



//        search_texto_maps.debounce {
//            buscaEndereco(it)
//        }
    }


    fun EditText.debounce(delegate: (text: String) -> Unit) {
        val timeDebounce: Long = 800
        val minSize: Long = 3
        var textTyped = ""
        val runnable = Runnable {
            delegate(textTyped)
        }

        val handler = Handler()

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(runnable)
                textTyped = text.toString()
                if (text != null && text.length >= minSize) {
                    handler.postDelayed(runnable, timeDebounce)
                }
            }
        })
    }

    fun EditText.onChangeText(delegate: (text: String) -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(
                textTyped: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                delegate(textTyped.toString())
            }
        })
    }




    fun buscaEndereco(endereco: String) {
        var listaEndereco : MutableList<Address> = arrayListOf()
        val geocoder : Geocoder = Geocoder(this)
        try {
        listaEndereco = geocoder.getFromLocationName(endereco, 1)


        }catch (e : IOException){
            Toast.makeText(this, "$e", Toast.LENGTH_LONG).show()
        }

        if (listaEndereco.isEmpty()) return

        val address : Address = listaEndereco[0]
        val localizacao : LatLng =  LatLng(address.latitude, address.longitude)

//        mMap.addMarker(MarkerOptions().position(localizacao).title("Marker"))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(localizacao))

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(20.0f)
        // Add a marker in Sydney and move the camera

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

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
                ), LOCALE_PERMISSION_REQUEST_CODE
            )
        }
        client.lastLocation
            .addOnSuccessListener { sucess ->
                sucess?.let {
                    Log.e("Teste", "${it.latitude},     ${it.longitude}")


                    val origem = LatLng(it.latitude, it.longitude)
//                    mMap.addMarker(MarkerOptions().position(origem).title("Estou aqui"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origem, 14.0f))
                }
            }
            .addOnFailureListener {

            }

        val locationRequest: LocationRequest = LocationRequest.create()
//        locationRequest.interval = 15 * 1000
//        locationRequest.fastestInterval = 5 * 1000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(
            builder.build()
        )
            .addOnSuccessListener {
                Log.e("Teste", it.locationSettingsStates.isNetworkLocationPresent.toString())
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    val resolvable: ResolvableApiException = e
                    resolvable.startResolutionForResult(this, 10)
                }
            }

        val locationCallBack: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult == null) {
                    Log.e("Teste2", "local is null")
                }

                for (location in locationResult!!.locations) {
                    Log.e("Teste2", "${location.latitude}")

                    if (!Geocoder.isPresent()) {
                        return
                    }

//                        startIntenteService(location)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Log.e("Teste", "LocationAvailability ${locationAvailability?.isLocationAvailable}")
            }
        }

        client.requestLocationUpdates(locationRequest, locationCallBack, null)
    }

    fun startIntenteService(location: Location) {
        val intent = Intent(this, FetchAddressService::class.java)
        intent.putExtra(RECEIVER, resultReceiver)
        intent.putExtra(LOCATION_DATA_EXTRA, location)
        startService(intent)

    }

    inner class AddressResultReceiver(handler: Handler?) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (resultData == null) return

            val addressOutPut = resultData.getString(RESULT_DATA_KEY)
            if (addressOutPut != null) {

                runOnUiThread {
                    Toast.makeText(this@MainActivity, addressOutPut, Toast.LENGTH_LONG).show()
                    Log.e("Teste", "addressOutPut $addressOutPut")
                    this@MainActivity.textView.text = addressOutPut

                }

            }
            super.onReceiveResult(resultCode, resultData)
        }
    }
}
