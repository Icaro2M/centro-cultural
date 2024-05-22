package com.example.centrocultural

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class UserHomeFragment : Fragment(), ObraAdapter.OnItemClickListener  {

    private lateinit var recycler_obras_home: RecyclerView

    private lateinit var user_dots_button: Button

    private lateinit var ask_option_popup:Dialog

    private  lateinit var adapter: ObraAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_user_home, container, false)



        recycler_obras_home = view.findViewById(R.id.user_recycle_home)
        adapter = ObraAdapter((requireActivity() as MainActivity).database)
        recycler_obras_home.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(this)
        recycler_obras_home.adapter = adapter

        user_dots_button = view.findViewById(R.id.ab_dots_button)
        ask_option_popup = Dialog(requireContext())


        user_dots_button.setOnClickListener(){

            onUser_dots_buttonPressed()



            var pop_up_ask_button = ask_option_popup.findViewById<Button>(R.id.pop_up_ask_button)
            pop_up_ask_button.setOnClickListener(){
                resetDialogLayoutParams(ask_option_popup)
                onPop_up_ask_buttonPressed()
                var pop_up_button_continuar = ask_option_popup.findViewById<Button>(R.id.pop_up_button_continuar)
                var pop_up_button_cancel = ask_option_popup.findViewById<ImageButton>(R.id.pop_up_button_cancel)
                pop_up_button_cancel.setOnClickListener(){
                    ask_option_popup.dismiss()
                }
                pop_up_button_continuar.setOnClickListener(){
                    onPop_up_button_continuarPressed()


                    var pop_up_key_button_cancel = ask_option_popup.findViewById<ImageButton>(R.id.key_pop_up_button_cancel)
                    var pop_up_button_enviar = ask_option_popup.findViewById<Button>(R.id.key_pop_up_button_enviar)
                    var pop_up_key_text = ask_option_popup.findViewById<TextView>(R.id.key_pop_up_text)
                    var pop_up_chave_errada_text = ask_option_popup.findViewById<TextView>(R.id.pop_up_chave_incorreta_text)
                    var pop_up_chave_errada_icon = ask_option_popup.findViewById<ImageView>(R.id.pop_up_chave_incorreta_icon)



                    pop_up_key_button_cancel.setOnClickListener(){
                        ask_option_popup.dismiss()
                    }
                    pop_up_button_enviar.setOnClickListener(){

                        var chave_de_acesso = pop_up_key_text.text
                        var chave:String = "Unifor123"

                        if(chave_de_acesso.toString()==chave){

                            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("adm",true).apply()

                            val intent = Intent(requireContext(),AdmMainActivity::class.java)
                            startActivity(intent)


                        }else{
                            pop_up_key_text.text = ""

                            pop_up_chave_errada_text.visibility = View.VISIBLE
                            pop_up_chave_errada_icon.visibility = View.VISIBLE
                            pop_up_key_text.setBackgroundResource(R.drawable.pop_up_key_text_red)
                        }
                    }
                }
            }


        }


        return view
    }


    private fun onUser_dots_buttonPressed(){
        ask_option_popup.setContentView(R.layout.ask_option_pop_up)

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
    }

    private fun onPop_up_ask_buttonPressed(){
        ask_option_popup.setContentView(R.layout.information_pop_up)
        ask_option_popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        ask_option_popup.show()
    }
    private fun onPop_up_button_continuarPressed(){
        ask_option_popup.setContentView(R.layout.key_pop_up)
        ask_option_popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        ask_option_popup.show()
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

    override fun onItemClick(obra: Obra) {

        (requireActivity() as MainActivity).openFragmentInformation(obra)
    }

}