package com.example.meumapa.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.meumapa.FetchAddressService
import com.example.meumapa.R
import com.example.meumapa.ui.constantes.*
import com.example.meumapa.ui.extension.debounce
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_customizado_marker_salvar.view.*
import java.io.IOException
import java.text.DecimalFormat
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private var client: FusedLocationProviderClient? = null
    private lateinit var resultReceiver: AddressResultReceiver
    var mMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private var listapolyline: MutableList<LatLng> = arrayListOf()
    private lateinit var _dialog: AlertDialog
    private var distance = 0.0
    private val minimSize = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configuraToolbar()

        search_mapa.debounce { buscaEnderecoCampo(it) }
    }


    fun buscaEnderecoCampo(endereco: String) {
        if ((!endereco.isEmpty()) and (endereco.length >= minimSize)) {
            var listaEndereco: MutableList<Address> = arrayListOf()
            val geocoder = Geocoder(this)
            try {
                listaEndereco = geocoder.getFromLocationName(endereco, 1)

            } catch (e: IOException) {
                Toast.makeText(this, "$e", Toast.LENGTH_LONG).show()
            }

            if (listaEndereco.isEmpty()) return

            val address: Address = listaEndereco[0]
            Log.e("buscaEndereco", "$address")
            val localizacao = LatLng(address.latitude, address.longitude)

            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(localizacao))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacao, 15.0f))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCALE_PERMISSION_REQUEST_CODE -> {
                Log.e("mMap", "RequestCode $requestCode  e $LOCALE_PERMISSION_REQUEST_CODE")
                if ((grantResults.isEmpty()) or (grantResults[0]
                            != PackageManager.PERMISSION_GRANTED)
                ) {
                    finish()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.e("mMap", "$mMap")

        if (mMap != null) {
            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (permission == PackageManager.PERMISSION_GRANTED) {
                mMap?.isMyLocationEnabled = true
            }
        }

        mMap!!.setMinZoomPreference(6.0f)
        mMap!!.setMaxZoomPreference(20.0f)
        mMap!!.setOnMapClickListener(this)
        mMap!!.setOnMapLongClickListener(this)
        mMap!!.setOnMarkerClickListener(this)
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
    }


// aqui habilitava o click para calcular distancia entre dois pontos

//    @SuppressLint("SetTextI18n")
//    private fun imagemViewRotas() {
//        activity_maps_rota.setOnClickListener {
//            drawRoute()
//            drawRoute()
//            for (i in 0..listapolyline.size) {
//                if (i < listapolyline.size - 1) {
//                    distance += distance(listapolyline[i], listapolyline[i + 1])
//                }
//            }
//            val comUmaCasa = ".#"
//            val decimalFormat = DecimalFormat(comUmaCasa)
//            val format: String = decimalFormat.format(distance)
//            text_metros.visibility = View.VISIBLE
//            text_metros.text = "$format m"
//        }
//    }

    private fun lixeiraLimpaMapa() {
        activity_maps_recycler_bin.setOnClickListener {
            mMap!!.clear()
            listapolyline.clear()
            text_metros.visibility = View.GONE
            activity_maps_recycler_bin.visibility = View.GONE
            activity_maps_rota.visibility = View.GONE
            text_metros.text = 0.0.toString()
            distance = 0.0
            polyline = null
        }
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCALE_PERMISSION_REQUEST_CODE
            )
        } else {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

//            imagemViewRotas()   // aqui atualiza o componente visivel
            lixeiraLimpaMapa()

            client = LocationServices.getFusedLocationProviderClient(this)
            resultReceiver = AddressResultReceiver(null)
        }

        val codError =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when (codError) {
            ConnectionResult.SERVICE_MISSING or ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED or ConnectionResult.SERVICE_DISABLED -> {
                GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, codError, 0, DialogInterface.OnCancelListener {
                        finish()
                    }).show()
            }
        }
        Log.e("Client", "$client")
        client?.lastLocation
            ?.addOnSuccessListener { sucess ->
                sucess?.let {

                    val origem = LatLng(it.latitude, it.longitude)
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(origem, 14.0f))
                }
            }
            ?.addOnFailureListener {
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
        client?.requestLocationUpdates(locationRequest, locationCallBack, null)
    }

    override fun onMapClick(position: LatLng) {
        mMap!!.addMarker(MarkerOptions().position(position))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        listapolyline.add(position)
        if (listapolyline.size > 0) {
            activity_maps_recycler_bin.visibility = View.VISIBLE
        }
        if (listapolyline.size >= 2) {
            activity_maps_rota.visibility = View.VISIBLE
            text_metros.visibility = View.VISIBLE
        }
        drawRoute()
        drawRoute()
        for (i in 0..listapolyline.size) {
            if (i < listapolyline.size - 1) {
                distance += distance(listapolyline[i], listapolyline[i + 1])
            }
        }
        val comUmaCasa = ".#"
        val decimalFormat = DecimalFormat(comUmaCasa)
        val format: String = decimalFormat.format(distance)
        text_metros.text = "$format m"
    }

    override fun onMapLongClick(position: LatLng) {
        val intent = Intent(this, FormularioSalvaLocalActivity::class.java)
        Log.e("onMArkClick", "$position")
        val objetoJson = Gson()
        val objetoTransferir = objetoJson.toJson(position)
        intent.putExtra(PATH_CODE, objetoTransferir)
        startActivity(intent)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
return true
    }

    private fun drawRoute() {
        val poly: PolylineOptions
        if (polyline == null) {
            poly = PolylineOptions()

            for (tam in 0..listapolyline.size) {
                poly.add(listapolyline[0])
            }

            poly.color(Color.BLUE)
            polyline = mMap!!.addPolyline(poly)
        } else {
            polyline!!.points = listapolyline
//            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(listapolyline[0]))
        }
    }

    private fun distance(StartP: LatLng, EndP: LatLng): Double {
        val lat1: Double = StartP.latitude
        val lat2: Double = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                dLon / 2
            ) * sin(dLon / 2)
        val c = asin(sqrt(a))
        return 6366000 * c
    }

    private fun configuraToolbar() {
        setSupportActionBar(toolbar_mapa)
        supportActionBar?.title = TITLE_MAPA
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_informativo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_botao_informativo -> {
                Toast.makeText(this, "Click informativo", Toast.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
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
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }

}
