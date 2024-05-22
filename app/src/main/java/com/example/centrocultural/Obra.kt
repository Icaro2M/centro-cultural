package com.example.centrocultural

import android.graphics.Bitmap
import java.io.Serializable

data class Obra(
    val nome:String,
    val autor:String,
    val imagem: Bitmap?,
    val qrCode:String,
    val ano:String,
    val descricao:String
) : Serializable

class ObraBuilder{
    var nome:String = "Sem informação"
    var autor:String = "Sem informação"
    var imagem:Bitmap?= null
    var qrCode:String = "codigo"
    var ano:String = "Sem informação"
    var descricao:String = "Sem informação"

    fun build() : Obra = Obra(nome,autor,imagem,qrCode,ano,descricao)
}

fun obra(block: ObraBuilder.()->Unit):Obra = ObraBuilder().apply(block).build()

fun obrasFake() = mutableListOf(
    obra{
        nome = "Monalisa"
        autor = "Leonardo Da vince"
        ano = "1770"
        descricao = "asndkjsandkjn knsakdnkjn sadjnj nsajn sajndsajndkdsanaksdjkan ksajndksandksandka"
    }
)