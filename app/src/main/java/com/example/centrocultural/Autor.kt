package com.example.centrocultural

import android.graphics.Bitmap
import java.io.Serializable

data class Autor(
    val nome:String,
    val nascimento:String,
    val falescimento:String,
    val imagem: Bitmap?,
    val nacionalidade:String,
    val descricao:String,
    val id:String,
    var obras:ArrayList<String> = arrayListOf()
) :  Serializable


class AutorBuilder{
    var nome:String = "Sem informação"
    var nascimento:String = "Sem informação"
    var imagem:Bitmap? = null
    var falescimento:String = "sem informação"
    var nacionalidade:String = "Sem informação"
    var descricao:String = "Sem informação"
    var id:String = ""


    fun build() : Autor = Autor(nome,nascimento,falescimento,imagem,nacionalidade,descricao,id)
}

fun autor(block: AutorBuilder.()->Unit):Autor = AutorBuilder().apply(block).build()

fun autoresFake() = mutableListOf(
    autor{
        nome = "Leonardo Da Vince"
        nascimento = "1730"

    }
)