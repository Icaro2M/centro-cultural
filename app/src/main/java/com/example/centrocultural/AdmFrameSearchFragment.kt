package com.example.centrocultural

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.Locale

class AdmFrameSearchFragment : Fragment(), AdmSearchObras.OnSearchListener, AdmSearchAutores.OnSearchListener {



    private lateinit var sb_frame_btn : Button
    private lateinit var sb_person_btn : Button
    private lateinit var sb_mic_btn : ImageButton
    private lateinit var sb_cancel_btn : ImageButton

    private lateinit var sb_frame_icon : ImageView
    private lateinit var sb_person_icon : ImageView

    private lateinit var sb_frame_line : TextView
    private lateinit var sb_person_line : TextView

    private lateinit var sb_search_textbox : TextView

    private lateinit var estado:String





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_adm_frame_search, container, false)





        estado = "obras"

        sb_frame_btn = view.findViewById(R.id.sb_frame_button)
        sb_person_btn = view.findViewById(R.id.sb_person_button)
        sb_mic_btn = view.findViewById(R.id.sb_mic_button)
        sb_cancel_btn = view.findViewById(R.id.sb_cancel_button)

        sb_frame_icon = view.findViewById(R.id.sb_frame_icon)
        sb_person_icon = view.findViewById(R.id.sb_person_icon)

        sb_frame_line = view.findViewById(R.id.sb_frame_line)
        sb_person_line = view.findViewById(R.id.sb_person_line)

        sb_search_textbox = view.findViewById(R.id.sb_textbox_search)


        sb_frame_btn.setOnClickListener(){
            onSb_frame_btnClicked()
        }
        sb_person_btn.setOnClickListener(){
            onSb_person_btnClicked()
        }


        sb_mic_btn.setOnClickListener(){

            askSpeechInputs()

        }



        sb_cancel_btn.setOnClickListener(){
            sb_search_textbox.text = ""
        }
        sb_search_textbox.text = arguments?.getString("textbunle")?:""

        sb_search_textbox.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                onSearch(s.toString())

            }

            override fun afterTextChanged(s: Editable?) {
                if(s.isNullOrEmpty()){
                    sb_mic_btn.visibility = View.VISIBLE
                    sb_cancel_btn.visibility = View.INVISIBLE

                }else{
                    sb_mic_btn.visibility = View.INVISIBLE
                    sb_cancel_btn.visibility = View.VISIBLE

                }
                saveContentString("saveString",s.toString(),requireContext())

            }

        })

        startFragment()

        return view;
    }






    private fun onSb_frame_btnClicked(){
        sb_frame_btn.setTextColor(Color.parseColor("#0069b5"))
        sb_frame_icon.background.setTint(ContextCompat.getColor(requireContext(), R.color.selectedItem))

        sb_person_btn.setTextColor(Color.parseColor("#8c8c8c"))
        sb_person_icon.background.setTint(ContextCompat.getColor(requireContext(), R.color.unselectedItem))

        sb_frame_line.visibility = View.VISIBLE
        sb_person_line.visibility = View.INVISIBLE

        saveContentBoolean("saveBoolean",true,requireContext())
        estado = "obras"
        Log.e("CREATION",estado)
        replaceFragment(AdmSearchObras(),"obras")



    }

    private fun onSb_person_btnClicked(){
        sb_person_btn.setTextColor(Color.parseColor("#0069b5"))
        sb_person_icon.background.setTint(ContextCompat.getColor(requireContext(), R.color.selectedItem))

        sb_frame_btn.setTextColor(Color.parseColor("#8c8c8c"))
        sb_frame_icon.background.setTint(ContextCompat.getColor(requireContext(), R.color.unselectedItem))



        sb_person_line.visibility = View.VISIBLE
        sb_frame_line.visibility = View.INVISIBLE

        saveContentBoolean("saveBoolean",false,requireContext())
        estado = "autores"
        replaceFragment(AdmSearchAutores(),"autores")


    }
    fun loadContentBoolean(key:String,context: Context):Boolean{
        var sp: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sp.getBoolean(key,true)
    }
    fun loadContentString(key:String,context: Context):String{
        var sp: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sp.getString(key,"")?:""
    }

    private fun saveContentBoolean(key:String, value: Boolean,context: Context){
        var sp: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = sp.edit()
        editor.putBoolean(key,value)
        editor.apply()
    }
    private fun saveContentString(key:String, value: String,context: Context){
        var sp: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = sp.edit()
        editor.putString(key,value)
        editor.apply()
    }


    private fun replaceFragment(fragment: Fragment,fragment_tag:String){
        val fm = childFragmentManager
        val ft = fm.beginTransaction()

        ft.replace(R.id.user_search_frame_layout,fragment,fragment_tag)
        ft.commit()
    }



    private fun startFragment(){
        var searchSelected = loadContentBoolean("saveBoolean",requireContext())
        var searchText = loadContentString("saveString",requireContext())
        sb_search_textbox.text = searchText

        if(searchSelected){
            onSb_frame_btnClicked()

        }
        else{
            onSb_person_btnClicked()

        }


    }

    override fun onSearch(query: String) {
        Log.e("Creation","pesquisando")

        if(estado=="obras"){

            val fragmento = childFragmentManager.findFragmentByTag(estado) as? AdmSearchObras
            Log.e("Creation",fragmento.toString())
            fragmento?.search(query)
        }else{
            val fragmento = childFragmentManager.findFragmentByTag(estado) as? AdmSearchAutores

            fragmento?.search(query)
        }


    }


    private fun askSpeechInputs(){
        if(!SpeechRecognizer.isRecognitionAvailable(requireContext())){
            Toast.makeText(requireContext(), "reconhecimento de voz indisponível    ", Toast.LENGTH_SHORT).show()
        } else{
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Fale alguma coisa")



            requireActivity().startActivityForResult(i,(requireActivity() as AdmMainActivity).RQ_SPEECH_REC)
        }
    }
    fun atualizarTexto(str:String){
        sb_search_textbox.text = str
    }


}