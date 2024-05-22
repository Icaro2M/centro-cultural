package com.example.centrocultural

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdmSearchObras : Fragment() ,AdmObraAdapter.OnItemClickListener{

    interface OnSearchListener {
        fun onSearch(query: String)
    }

    private var searchListener: OnSearchListener? = null

    companion object{
        fun newInstance() = AdmSearchObras()
    }

    private lateinit var recycler_obras_search: RecyclerView
    private  lateinit var thisActivity: Context
    private lateinit var adapter:AdmObraAdapter

    private lateinit var ask_option_popup: Dialog
    private lateinit var obrasArray:MutableList<Obra>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_adm_search_obras, container, false)




        thisActivity = activity as Context
        obrasArray = (requireActivity() as AdmMainActivity).database
        Log.e("CREATION",obrasArray.toString())


        ask_option_popup = Dialog(requireContext())
        recycler_obras_search = view.findViewById(R.id.search_frame_recycler)


        startFragment()




        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val resources = context.resources

    }

    override fun onItemClick(obra: Obra) {

        (requireActivity() as AdmMainActivity).openFragmentInformation(obra)
    }

    override fun onBtn_editClick(obra: Obra) {
        (requireActivity() as AdmMainActivity).openFragmentEdit(obra)
    }

    override fun onBtn_removeClick(obra: Obra) {

        ask_option_popup.setContentView(R.layout.adm_delete_pop_up)
        var txt_popup = ask_option_popup.findViewById<TextView>(R.id.text_popup)
        txt_popup.text = "Tem certeza de que deseja remover a obra ${obra.nome}?"
        ask_option_popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ask_option_popup.show()

        var sim_btn = ask_option_popup.findViewById<Button>(R.id.confirm_button)
        var nao_btn = ask_option_popup.findViewById<Button>(R.id.deny_button)
        var cancel_btn = ask_option_popup.findViewById<ImageButton>(R.id.adm_pop_up_button_cancel)

        sim_btn.setOnClickListener(){

            Utilidades.removerElemento("obras",obra.qrCode)

            ask_option_popup.dismiss()
            if(!obra.autor.isNullOrEmpty()){
                Utilidades.removerObradoAutor((requireActivity() as AdmMainActivity).idDoAutor(obra.autor),obra.nome)

            }
            (requireActivity() as AdmMainActivity).atualizarBanco()
            adapter.atualizarDados((requireActivity() as AdmMainActivity).database)


        }
        nao_btn.setOnClickListener(){
            ask_option_popup.dismiss()
        }
        cancel_btn.setOnClickListener(){
            ask_option_popup.dismiss()
        }
    }

    fun search(query: String) {
        Log.e("CREATION","cheguei")

        adapter.atualizarDados(arrayFiltrado(query))
    }

    private fun arrayFiltrado(query: String):MutableList<Obra>{
        return obrasArray.filter { obra-> obra.nome.contains(query, ignoreCase = true) }.toMutableList()
    }


    fun loadContentString(key:String,context: Context):String{
        var sp: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sp.getString(key,"")?:""
    }
    private fun startFragment(){


        var searchText = loadContentString("saveString",requireContext())


        adapter = AdmObraAdapter(arrayFiltrado(searchText))
        recycler_obras_search.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(this)
        pausarExecucaoComCallback(300){
            recycler_obras_search.adapter = adapter
        }
    }
    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }



    }