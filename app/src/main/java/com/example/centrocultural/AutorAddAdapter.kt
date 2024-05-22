package com.example.centrocultural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AutorAddAdapter(var autores:MutableList<Autor>):RecyclerView.Adapter<AutorAddAdapter.AutorAddViewHolder>(){

    fun atualizarDados(novaLista: MutableList<Autor>) {
        autores = novaLista
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(autor:Autor)

    }

    private var listener: AutorAddAdapter.OnItemClickListener? = null

    // Método público para configurar o ouvinte
    fun setOnItemClickListener(listener: AutorAddAdapter.OnItemClickListener) {
        this.listener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutorAddViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.autores_add_item,parent,false)
        return AutorAddViewHolder(view)
    }

    override fun getItemCount(): Int = autores.size

    override fun onBindViewHolder(holder: AutorAddViewHolder, position: Int) {
        holder.bind(autores[position])
        holder.itemView.setOnClickListener {
            listener?.onItemClick(autores[position])
        }
    }



    inner class AutorAddViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun bind(autor: Autor){
            var autor_nome = itemView.findViewById<TextView>(R.id.autor_name)
            autor_nome.text = autor.nome
        }
    }
}