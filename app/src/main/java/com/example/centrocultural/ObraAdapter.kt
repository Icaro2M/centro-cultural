package com.example.centrocultural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ObraAdapter(var obras:MutableList<Obra>) : RecyclerView.Adapter<ObraAdapter.ObraViewHolder>(){




    fun atualizarDados(novaLista: MutableList<Obra>) {
        obras = novaLista
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(obra:Obra)
    }

    // Variável de membro para armazenar o ouvinte
    private var listener: OnItemClickListener? = null

    // Método público para configurar o ouvinte
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_recycler_item,parent,false)
        return ObraViewHolder(view)
    }



    override fun getItemCount():Int =  obras.size

    override fun onBindViewHolder(holder: ObraViewHolder, position: Int) {
        holder.bind(obras[position])
        holder.itemView.setOnClickListener {
            listener?.onItemClick(obras[position])
        }

    }



    inner class ObraViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bind(obra: Obra) {
            var obra_nome = itemView.findViewById<TextView>(R.id.user_frame_item_title)
            var obra_image = itemView.findViewById<ImageView>(R.id.user_frame_item_image)
            obra_nome.text = obra.nome
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap = Utilidades.getImage("imgObras", obra.qrCode)
                bitmap?.let { obra_image.setImageBitmap(it) }
            }


        }

    }



}