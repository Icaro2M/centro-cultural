package com.example.centrocultural

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class AdmSearchAutores : Fragment(), AutorAdapter.OnItemClickListener {

    interface OnSearchListener {
        fun onSearch(query: String)
    }

    private var searchListener: AdmSearchAutores.OnSearchListener? = null

    companion object{
        fun newInstance() = AdmSearchAutores()
    }

    private lateinit var recycler_autores_search: RecyclerView
    private  lateinit var thisActivity: Context
    private lateinit var adapter:AutorAdapter

    private lateinit var autoresArray:MutableList<Autor>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_adm_search_autores, container, false)

        thisActivity = activity as Context
        autoresArray = (requireActivity() as AdmMainActivity).databaseAutores

        recycler_autores_search = view.findViewById(R.id.search_autor_recycler)

        startFragment()








        return view
    }



    private fun arrayFiltrado(query: String):MutableList<Autor>{
        return autoresArray.filter { autor-> autor.nome.contains(query, ignoreCase = true) }.toMutableList()
    }


    fun loadContentString(key:String,context: Context):String{
        var sp: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sp.getString(key,"")?:""
    }

    private fun startFragment(){


        var searchText = loadContentString("saveString",requireContext())


        adapter = AutorAdapter(arrayFiltrado(searchText))
        recycler_autores_search.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(this)
        pausarExecucaoComCallback(300){
            recycler_autores_search.adapter = adapter
        }
    }

    fun search(query: String) {


        adapter.atualizarDados(arrayFiltrado(query))
    }

    override fun onItemClick(autor:Autor) {
        (requireActivity() as AdmMainActivity).openFragmentInformationAutor(autor)
    }
    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }

    }