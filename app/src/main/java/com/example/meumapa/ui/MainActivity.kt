package com.example.meumapa.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.CaseMap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.ContactsContract
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.meumapa.FetchAddressService
import com.example.meumapa.R
import com.example.meumapa.dao.LocalDAO
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
import java.io.IOException
import java.lang.Double.parseDouble
import java.text.DecimalFormat
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private var client: FusedLocationProviderClient? = null
    private lateinit var resultReceiver: AddressResultReceiver
    var mMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private var listapolyline: MutableList<LatLng> = arrayListOf()
    private var distance = 0.0
    private val minimSize = 3
    private val dao = LocalDAO(this)
    var listaMarker: MutableList<LatLng> = arrayListOf()
    private lateinit var marker : Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configuraToolbar()

        search_mapa.debounce { buscaEnderecoCampo(it) }


        var lat: Double
        var lon: Double
        var locationLatLong: LatLng
        val listaLocal = dao.buscaLocal()
        if (listaLocal.isNotEmpty()) {
            for (myLista in listaLocal) {

                if ((myLista.latitude!!.isNotEmpty()) or (myLista.longitude!!.isNotEmpty())) {
                    lat = parseDouble(myLista.latitude!!)
                    lon = parseDouble(myLista.longitude!!)
                    Log.e("listaMarker2", "$lat $lon")
                    locationLatLong = LatLng(lat, lon)
                    listaMarker.add(locationLatLong)
                }

                val builder = LatLngBounds.builder()
                for (markers in listaMarker) {
                    builder.include(markers)
                }

                val bounds: LatLngBounds? = builder.build()
                if (bounds != null) {
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))
                }
            }
        }
    }

    private fun buscaEnderecoCampo(endereco: String) {
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
        mMap!!.uiSettings.isMyLocationButtonEnabled = true

        for (mi_mark in listaMarker) {
            val options = MarkerOptions()
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
            mMap!!.addMarker(options.position(mi_mark))
        }

//        customAddMarker(LatLng(-22.20525287785512, -49.661143496632576), "Marcador 1 alterado", "O Marcador 1 foi reposicionado" )
//        customAddMarker(LatLng(-22.221672952934018, -49.66127760708332), "Marcador 2 alterado", "O Marcador 2 foi reposicionado" )
//
        mMap!!.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter{

            override fun getInfoContents(marker: Marker): View {
             val tv : TextView  = TextView(this@MainActivity)
                tv.text = Html.fromHtml("<b><font color =\"#ff0000\"> ${marker.title} : </font></b>  ${marker.snippet}")

                return tv

            }

            override fun getInfoWindow(marker: Marker): View? {
                val ll = LinearLayout(this@MainActivity)
                ll.setPadding(20,20,20,20)
                ll.setBackgroundColor(Color.YELLOW)


                val tv : TextView  = TextView(this@MainActivity)
                tv.text = Html.fromHtml("<b><font color =\"#fff\"> ${marker.title} : </font></b>  ${marker.snippet}")
                ll.addView(tv)

                val btn = Button(this@MainActivity)
                btn.text = "Botao"
                btn.setBackgroundColor(Color.LTGRAY)
                btn.setOnClickListener {
                    Toast.makeText(this@MainActivity, "Botao", Toast.LENGTH_LONG).show()
                }
                ll.addView(btn)

                return ll
            }

        })

//        mMap!!.setOnCameraIdleListener {
//            Log.e("marker", "setCameraMove")
//            val lat = mMap!!.cameraPosition.target.latitude
//            val lng = mMap!!.cameraPosition.target.longitude
//            customAddMarker(LatLng(lat, lng), "Marcador 1 alterado", "O Marcador 1 foi reposicionado" )
//        }
//
//        mMap!!.setOnMapClickListener {
//            Log.e("marker", "setOnMapClick")
//            if (marker != null){
//                marker.remove()
//            }
//            customAddMarker(LatLng(it.latitude, it.longitude), "Marcador 2 alterado", "O Marcador 2 foi reposicionado" )
//        }
//
        mMap!!.setOnMarkerClickListener { marker ->
            Log.e("marker", "3 MarcadorClicado ${marker.title}")
            false
        }
//
//        mMap!!.setOnInfoWindowClickListener { marker ->
//                Log.e("marker", "4 Window ${marker.title}")
//        }
    }


        private fun customAddMarker(latLng: LatLng, title: String, snippet : String){
        val options = MarkerOptions()
        options.position(latLng).title(title).snippet(snippet).draggable(true)

            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))

        marker = mMap!!.addMarker(options)

    }


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
        val options = MarkerOptions()
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.square))
        mMap!!.addMarker(options.position(position))
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


    private fun drawRoute() {
        val poly: PolylineOptions
        if (polyline == null) {
            poly = PolylineOptions()

            for (tam in 0..listapolyline.size) {
                poly.add(listapolyline[0])
            }

            poly.color(Color.GRAY)
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
                showDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(this)
        val meuVIew = layoutInflater.inflate(R.layout.dialog_informativo, null)

        dialog.setPositiveButton("Eu Entendi") { dialog, which ->
            dialog.cancel()
            dialog.dismiss()
        }
        dialog.setView(meuVIew)
        dialog.show()
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
