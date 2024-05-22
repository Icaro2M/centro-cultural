package com.example.centrocultural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutorAdapter(var autores:MutableList<Autor>):RecyclerView.Adapter<AutorAdapter.AutorViewHolder>(){

    fun atualizarDados(novaLista: MutableList<Autor>) {
        autores = novaLista
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(autor:Autor)

    }

    private var listener: AutorAdapter.OnItemClickListener? = null

    // Método público para configurar o ouvinte
    fun setOnItemClickListener(listener: AutorAdapter.OnItemClickListener) {
        this.listener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_autor_recycler_item,parent,false)
        return AutorViewHolder(view)
    }

    override fun getItemCount(): Int = autores.size

    override fun onBindViewHolder(holder: AutorViewHolder, position: Int) {
        holder.bind(autores[position])
        holder.itemView.setOnClickListener {
            listener?.onItemClick(autores[position])
        }
    }



    inner class AutorViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun bind(autor: Autor){
            var autor_nome = itemView.findViewById<TextView>(R.id.user_item_autor_name)
            var autor_image = itemView.findViewById<ImageView>(R.id.user_item_autor_image)
            autor_nome.text = autor.nome
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap = Utilidades.getImage("imgAutores", autor.id)
                bitmap?.let { autor_image.setImageBitmap(it) }
            }
        }
    }
}