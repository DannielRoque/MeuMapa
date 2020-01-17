package com.example.meumapa.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.meumapa.BuildConfig
import com.example.meumapa.R
import com.example.meumapa.dao.LocalDAO
import com.example.meumapa.model.Local
import com.example.meumapa.ui.constantes.PATH_CODE
import com.example.meumapa.ui.constantes.PATH_CODE_CAMERA
import com.example.meumapa.ui.constantes.TITLE_FORMULARIO
import com.example.meumapa.ui.helper.FormularioHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_formulario_salva_local.*
import java.io.File

class FormularioSalvaLocalActivity : AppCompatActivity() {

    private var mMap: GoogleMap? = null
    private lateinit var option: Spinner
    lateinit var caminhoFoto: String
    private lateinit var helper: FormularioHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_salva_local)

        setSupportActionBar(toolbar_formulario)
        supportActionBar?.title = TITLE_FORMULARIO
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent?.let { intent ->
            intent.getStringExtra(PATH_CODE)?.let { dados ->
                val latLng: LatLng =
                    Gson().fromJson(dados, object : TypeToken<LatLng>() {}.type)
                txt_latlong.text =
                    ((latLng.latitude.toString()) + "," + (latLng.longitude.toString()))
            }
        }


        option = activity_formulario_spinner
        helper = FormularioHelper(this)

        val options = valoresSpinner()

        option.adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options)

        selecionaItemSpinner(options)


        activity_formulario_botao_foto.setOnClickListener {


            val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            caminhoFoto =
                getExternalFilesDir(null).toString() + "/" + System.currentTimeMillis() + ".jpg"
            val arquivoFoto = File(caminhoFoto)
            intentCamera.putExtra(
                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider", arquivoFoto
                )
            )
            startActivityForResult(intentCamera, PATH_CODE_CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PATH_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                helper.carregaImagem(caminhoFoto)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun selecionaItemSpinner(options: Array<String>) {
        option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.e("Spinner", options[id.toInt()])
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_salvar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_botao_salvar -> {
                val local: Local = helper.pegaLocal()

                if (local.observacao!!.isEmpty()) {
                    activity_formulario_observacao.error = "Campo não pode ser vazio"
                } else {
                    activity_formulario_observacao.error = null
                }

                if((local.telefone!!.isEmpty()) or (local.telefone==null)){
                    activity_formulario_telefone.error = "Campo não pode ser vazio"
                }else{
                    activity_formulario_telefone.error = null
                }

                val dao = LocalDAO(this)
                dao.insereLocal(local)
                finish()
            }
        }
        return true
    }

    private fun valoresSpinner(): Array<String> {
        return arrayOf(
            "Restaurante & Lanchonete",
            "Bar & Boteco",
            "Petiscaria",
            "Hamburgueria",
            "Pizzaria",
            "Supermercado",
            "Doceria",
            "Parque & Lazer",
            "Manicure & Pedicure",
            "Salão de Beleza",
            "Barbearia",
            "Roupas & Sapatos",
            "Mecânico & Borracharia",
            "Hotel & Pousada",
            "Casa",
            "Outros"
        )
    }
}
