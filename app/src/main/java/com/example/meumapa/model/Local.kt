package com.example.meumapa.model

import java.io.Serializable

open class Local : Serializable{
    var id: Long = 0
    var imagem: String? = null
    var descricao: String? = null
    var telefone: String? = null
    var observacao: String? = null
    var nota: Int? = null
}
