package com.example.centrocultural

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class AdmHomeFragment : Fragment(),AdmObraAdapter.OnItemClickListener  {

    private lateinit var recycler_obras_home: RecyclerView

    private lateinit var user_dots_button: Button

    private lateinit var ask_option_popup: Dialog

    private  lateinit var adapter: AdmObraAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_adm_home, container, false)

        var obras:MutableList<Obra> = (requireActivity() as AdmMainActivity).database
        Log.e("CREATION",obras.toString())
        recycler_obras_home = view.findViewById(R.id.adm_recycle_home)
        adapter = AdmObraAdapter(obras)

        recycler_obras_home.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(this)
        pausarExecucaoComCallback(300){
            recycler_obras_home.adapter = adapter
        }


        user_dots_button = view.findViewById(R.id.ab_dots_button)
        
        ask_option_popup = Dialog(requireContext())

        user_dots_button.setOnClickListener(){
            ask_option_popup.setContentView(R.layout.ask_out_option_pop_up)
            val layoutParams = WindowManager.LayoutParams().apply {
                copyFrom(ask_option_popup.window?.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                gravity = Gravity.TOP or Gravity.END

                x = 22
                y = 130
            }
            ask_option_popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            ask_option_popup.window?.attributes = layoutParams

            ask_option_popup.show()
            var pop_up_ask_button = ask_option_popup.findViewById<Button>(R.id.pop_up_ask_out_button)
            Log.e("CREATION", "criei o btn 1")
            pop_up_ask_button.setOnClickListener(){
                Log.e("CREATION", "entrei na fun do pop_up 2")
                resetDialogLayoutParams(ask_option_popup)
                Log.e("CREATION", "alterei os parametros 3")
                ask_option_popup.setContentView(R.layout.confirmation_pop_up)
                ask_option_popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                ask_option_popup.show()
                Log.e("CREATION", "mostrei o pop up 4")

                var sim_btn = ask_option_popup.findViewById<Button>(R.id.confirm_button)
                var nao_btn = ask_option_popup.findViewById<Button>(R.id.deny_button)

                var pop_up_key_button_cancel = ask_option_popup.findViewById<ImageButton>(R.id.adm_pop_up_button_cancel)
                Log.e("CREATION", "criei os botoes 5")
                pop_up_key_button_cancel.setOnClickListener(){
                    ask_option_popup.dismiss()
                }
                nao_btn.setOnClickListener(){
                    ask_option_popup.dismiss()
                }
                sim_btn.setOnClickListener(){
                    requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("adm",false).apply()
                    val intent = Intent(requireContext(),MainActivity::class.java)
                    startActivity(intent)
                }

            }
        }





        return view
    }

    override fun onItemClick(obra: Obra) {
        (requireActivity() as AdmMainActivity).openFragmentInformation(obra)
    }

    override fun onBtn_editClick(obra: Obra) {
        Log.e("CREATION","edit")
        (requireActivity() as AdmMainActivity).openFragmentEdit(obra)
    }

    override fun onBtn_removeClick(obra: Obra) {
        resetDialogLayoutParams(ask_option_popup)
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
            pausarExecucaoComCallback(300){
                adapter.atualizarDados((requireActivity() as AdmMainActivity).database)
            }





        }
        nao_btn.setOnClickListener(){
            ask_option_popup.dismiss()
        }
        cancel_btn.setOnClickListener(){
            ask_option_popup.dismiss()
        }
    }




    fun resetDialogLayoutParams(dialog: Dialog) {
        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window?.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER // ou qualquer outra gravidade padrão que você deseja
            x = 0
            y = 0
        }
        dialog.window?.attributes = layoutParams
    }


    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }







}