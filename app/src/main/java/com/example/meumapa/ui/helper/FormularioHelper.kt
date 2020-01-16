package com.example.meumapa.ui.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.example.meumapa.model.Local
import com.example.meumapa.ui.FormularioSalvaLocalActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_formulario_salva_local.*

class FormularioHelper(activity: FormularioSalvaLocalActivity) {

    private val campoImagem: ImageView = activity.imagem_local
    private val campoTelefone: TextInputLayout = activity.activity_formulario_telefone
    private val campoObservacao: TextInputLayout = activity.activity_formulario_observacao
    private val local = Local()

    fun pegaLocal(): Local {
        local.imagem = campoImagem.tag as String?
        local.telefone = campoTelefone.editText?.text.toString()
        local.observacao = campoObservacao.editText?.text.toString()

        return local
    }

    fun carregaImagem(caminhoFoto: String) {
        if (caminhoFoto != null) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(caminhoFoto)
            val bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 480, 400, true)
            campoImagem.setImageBitmap(bitmapReduzido)
            campoImagem.tag = caminhoFoto
        }
    }
}