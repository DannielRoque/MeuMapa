package com.example.meumapa.ui.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.Spinner
import com.example.meumapa.model.Local
import com.example.meumapa.ui.FormularioSalvaLocalActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_formulario_salva_local.*

class FormularioHelper(private val activity: FormularioSalvaLocalActivity) {

    private val campo_imagem: ImageView = activity.imagem_local
    private val campo_descricao: TextInputLayout = activity.activity_formulario_descricao
    private val campo_telefone: TextInputLayout = activity.activity_formulario_telefone
    private val campo_observacao: TextInputLayout = activity.activity_formulario_observacao
    private lateinit var campo_categoria: Spinner
    private val local = Local()

    fun pegaLocal(): Local {
        local.imagem = campo_imagem.tag as String?
        local.descricao = campo_descricao.editText?.text.toString()
        local.telefone = campo_telefone.editText?.text.toString()
        local.observacao = campo_observacao.editText?.text.toString()
        local.descricao = campo_descricao.editText?.text.toString()
        return local
    }

    fun carregaImagem(caminhoFoto: String) {
        if (caminhoFoto != null) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(caminhoFoto)
            val bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 480, 480, true)
            campo_imagem.setImageBitmap(bitmapReduzido)
            campo_imagem.tag = caminhoFoto
        }
    }
}