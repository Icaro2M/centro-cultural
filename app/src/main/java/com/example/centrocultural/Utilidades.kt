package com.example.centrocultural

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Utilidades {
    val db = FirebaseFirestore.getInstance()
    val sr = FirebaseStorage.getInstance()


    fun removerElemento(colecao:String,elemento:String){
        db.collection(colecao).document(elemento).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Documento deletado com sucesso!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao deletar documento", e)
            }

        deleteImage(colecao,elemento)
    }


    fun removerObradoAutor(id:String,obra:String){

        db.collection("autores").document(id).update(
            "obras",FieldValue.arrayRemove(obra)
        ).addOnSuccessListener {
            Log.d(TAG, "Elemento removido do array com sucesso!")
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao remover elemento do array", e)
            }

    }

    fun addObraAoAutor(id:String,obra:String){
        db.collection("autores").document(id).update(
            "obras",FieldValue.arrayUnion(obra)
        ).addOnSuccessListener {
            Log.d(TAG, "Elemento adicionado ao array com sucesso!")
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao adicionar elemento ao array", e)
            }
    }



    fun alterarDado(colecao: String,id: String,updates:HashMap<String,Any>){
        db.collection(colecao).document(id).update(updates).addOnSuccessListener {
            Log.d(TAG, "Documento atualizado com sucesso!")
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao atualizar documento", e)
            }
    }

    fun alterarArray(autor:String,nome_anterior:String,nome_atual:String){
        db.collection("autores").document(autor).get().addOnSuccessListener {
        ds->
            val obra_array = ds.get("obras") as ArrayList<String>?
            if(obra_array!=null && obra_array.isNotEmpty()){
                for(i in 0..obra_array.size-1){
                    if(obra_array[i]==nome_anterior){
                        obra_array[i] = nome_atual
                        break
                    }
                }

                db.collection("autores").document(autor).update("obras",obra_array).addOnSuccessListener {
                    Log.d(TAG, "Elemento do array modificado com sucesso!")
                }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao modificar elemento do array", e)
                    }

            }


        }.addOnFailureListener { e ->
            Log.w(TAG, "Erro ao obter documento", e)
        }
    }

    fun alterarAutorObras(obras:MutableList<Obra>,autor_anterior:String,autor_novo:String){
        for(i in obras){
            if(i.autor==autor_anterior){
                alterarDado("obras",i.qrCode, hashMapOf("autor" to autor_novo))
            }
        }
    }



    suspend  fun getImage(colecao:String,img:String): Bitmap? {
        return suspendCoroutine { continuation ->
            val localFile = File.createTempFile("tempImage", "jpeg")
            sr.reference.child("$colecao/$img").getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                continuation.resume(bitmap)
            }.addOnFailureListener {
                Log.e("CREATION", "Erro ao carregar imagem")
                continuation.resume(null)
            }
        }
    }

    fun addImage(colecao: String,id:String,uri: Uri){
        var file = Uri.fromFile(File("$colecao/$id"))
        sr.getReference().child("$colecao/${file.lastPathSegment}").putFile(uri).addOnSuccessListener {

        }.addOnFailureListener{

        }

    }




    fun deleteImage(colecao: String,id: String){
        sr.getReference().child(colecao).child(id).delete().addOnSuccessListener {

        }.addOnFailureListener {

        }
    }




    fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, width, height
        )
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitmap
    }



}