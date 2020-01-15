package com.example.meumapa.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.meumapa.ui.constantes.PATH_CODE_CAMERA
import com.example.meumapa.ui.constantes.TITLE_FORMULARIO
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_formulario_salva_local.*
import java.io.File

class FormularioSalvaLocalActivity : AppCompatActivity() {

    lateinit var campo_observacao: TextInputLayout

    private lateinit var option: Spinner
    lateinit var caminhoFoto: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_salva_local)

        setSupportActionBar(toolbar_formulario)
        supportActionBar?.title = TITLE_FORMULARIO
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        option = activity_formulario_spinner
        campo_observacao = activity_formulario_observacao

        val options = arrayOf(
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
                val bitmap: Bitmap = BitmapFactory.decodeFile(caminhoFoto)
                val bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 480, 480, true)
                imagem_local.setImageBitmap(bitmapReduzido)
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
                Log.e("Spinner", options[position])
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
                Toast.makeText(this, "Clique salvar", Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


}
