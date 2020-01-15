package com.example.meumapa.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.meumapa.R
import com.example.meumapa.ui.constantes.TITLE_FORMULARIO
import kotlinx.android.synthetic.main.activity_formulario_salva_local.*

class FormularioSalvaLocalActivity : AppCompatActivity() {

    lateinit var option : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_salva_local)

        configuraToolbar()

        option = activity_formulario_spinner

        val options = arrayOf("Restaurante & Lanchonete", "Bar & Boteco", "Petiscaria",
            "Hamburgueria", "Pizzaria", "Supermercado", "Doceria", "Parque & Lazer", "Manicure & Pedicure", "Salão de Beleza",
            "Barbearia", "Roupas & Sapatos", "Mecânico & Borracharia", "Hotel & Pousada", "Casa", "Outros")

        option.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)

        option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

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

    private fun configuraToolbar() {
        setSupportActionBar(toolbar_formulario)
        supportActionBar?.title = TITLE_FORMULARIO
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_salvar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_botao_salvar ->{
                Toast.makeText(this, "Clique salvar", Toast.LENGTH_LONG).show()
            }
        }
        return true
    }
}
