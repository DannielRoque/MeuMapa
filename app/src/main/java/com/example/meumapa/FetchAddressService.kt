package com.example.meumapa

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import com.example.meumapa.ui.constantes.*
import java.io.IOException
import java.util.*

class FetchAddressService : IntentService("fetchAddressService") {

    lateinit var receiver : ResultReceiver

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

            val geocoder = Geocoder(this, Locale.getDefault())
            val location: Location = intent.getParcelableExtra(LOCATION_DATA_EXTRA)
            receiver = intent.getParcelableExtra(RECEIVER)

            var  addresses : List<Address> = arrayListOf()
            try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }catch (e : IOException){
                Log.e("Teste", "Serviço indisponível $e")
            }catch (i : IllegalArgumentException){
                Log.e("Teste", "latitude ou longitude inválida $i")
            }
            if ((addresses.equals(null)) or (addresses.isEmpty())){
                Log.e("Teste", "nenhum enderço encontrado")
                deliveryResultToReceiver(FAILURE_RESULT, "Nenhum endereco encontrado")
            }else{
                val address : Address = addresses[0]
                val addressF : MutableList<String> = arrayListOf()

                for ( i in 0..address.maxAddressLineIndex){
                    addressF.add(address.getAddressLine(i))
                }
                deliveryResultToReceiver(SUCESS_RESULT, TextUtils.join("|", addressF))
            }

    }

    private fun deliveryResultToReceiver(resultCode : Int, message : String){
        val bundle = Bundle()
        bundle.putString(RESULT_DATA_KEY, message)
        receiver.send(resultCode, bundle)
    }
}