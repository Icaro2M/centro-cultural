package com.example.centrocultural

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class userSeachObras : Fragment() ,ObraAdapter.OnItemClickListener{

    interface OnSearchListener {
        fun onSearch(query: String)
    }

    private var searchListener: OnSearchListener? = null

    companion object{
        fun newInstance() = userSeachObras()
    }

    private lateinit var recycler_obras_search: RecyclerView
    private  lateinit var thisActivity:Context
    private lateinit var adapter:ObraAdapter


    private lateinit var obrasArray:MutableList<Obra>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_seach_obras, container, false)




        thisActivity = activity as Context
        obrasArray = (requireActivity() as MainActivity).database
        Log.e("CREATION",obrasArray.toString())



        recycler_obras_search = view.findViewById(R.id.search_frame_recycler)


        startFragment()




        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val resources = context.resources

    }

    override fun onItemClick(obra: Obra) {

        (requireActivity() as MainActivity).openFragmentInformation(obra)
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


        adapter = ObraAdapter(arrayFiltrado(searchText))
        recycler_obras_search.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(this)
        recycler_obras_search.adapter = adapter
    }



}