package com.example.meumapa.model

import java.io.Serializable


class Local(
    var id: Long? = null,
    var imagem: String? = "",
    var telefone: String? = "",
    var observacao: String? = "",
    var latitude: String? = "",
    var longitude: String? = ""
) : Serializable

