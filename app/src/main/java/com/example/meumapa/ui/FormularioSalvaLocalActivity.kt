package com.example.meumapa.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.meumapa.R
import com.example.meumapa.ui.constantes.TITLE_FORMULARIO
import kotlinx.android.synthetic.main.activity_formulario_salva_local.*

class FormularioSalvaLocalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_salva_local)

        configuraToolbar()
    }

    private fun configuraToolbar() {
        setSupportActionBar(toolbar_formulario)
        supportActionBar?.title = TITLE_FORMULARIO
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
