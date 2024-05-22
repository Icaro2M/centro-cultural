package com.example.centrocultural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdmObraAdapter(var obras:MutableList<Obra>) : RecyclerView.Adapter<AdmObraAdapter.ObraViewHolder>(){




    fun atualizarDados(novaLista: MutableList<Obra>) {
        obras = novaLista
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(obra:Obra)
        fun onBtn_editClick(obra: Obra)
        fun onBtn_removeClick(obra: Obra)
    }

    // Variável de membro para armazenar o ouvinte
    private var listener: OnItemClickListener? = null


    // Método público para configurar o ouvinte
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adm_recycler_item,parent,false)
        return ObraViewHolder(view)
    }



    override fun getItemCount():Int =  obras.size

    override fun onBindViewHolder(holder: ObraViewHolder, position: Int) {
        holder.bind(obras[position])
        val btn_edit = holder.itemView.findViewById<ImageButton>(R.id.edit_button)
        val btn_remove = holder.itemView.findViewById<ImageButton>(R.id.remove_button)

        btn_edit.setOnClickListener(){
            listener?.onBtn_editClick(obras[position])
        }
        btn_remove.setOnClickListener(){
            listener?.onBtn_removeClick(obras[position])
        }
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