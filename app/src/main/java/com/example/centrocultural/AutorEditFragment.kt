package com.example.centrocultural

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable


class AutorEditFragment : Fragment() {

    companion object{

        fun newInstance(autor: Autor):AutorEditFragment{
            val fragmento = AutorEditFragment()
            val args = Bundle()

            args.putSerializable("chave2", autor)
            fragmento.arguments = args
            return fragmento
        }

    }
    var del_img = false
    var imgEdited = false
    var imagemAlt:Boolean = false
    private lateinit var editImgBtn:ImageButton
    private lateinit var bt_bg_delpic:TextView
    private lateinit var bt_ic_delpic:ImageButton
    private lateinit var obra_imagem:ImageView

    private lateinit var last_nome:String
    private lateinit var last_nascimento:String
    private lateinit var last_falescimento:String
    private lateinit var last_nacionalidade:String
    private lateinit var last_descricao:String

    private lateinit var autorInfo:Serializable
    private lateinit var autor:Autor

    private lateinit var nome_txt:TextView
    private lateinit var nascimento_txt:TextView
    private lateinit var falescimento_txt:TextView
    private lateinit var nacionalidade_txt:TextView
    private lateinit var descricao_txt:TextView

    private lateinit var btn_add:Button

    private lateinit var pop_up:Dialog

    private lateinit var back_btn:ImageButton

    private lateinit var autores:MutableList<Autor>

    private lateinit var nome_error_icon: ImageView
    private lateinit var nome_error_txt: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_autor_edit, container, false)

        arguments?.let {

            autorInfo = it.getSerializable("chave2") ?: throw IllegalArgumentException("Autor não encontrado")
        }
        autor = autorInfo as Autor

        autores = (requireActivity() as AdmMainActivity).databaseAutores

        nome_txt = view.findViewById(R.id.name_txt)
        nascimento_txt = view.findViewById(R.id.ano_txt)
        falescimento_txt = view.findViewById(R.id.falescimento_txt)
        nacionalidade_txt = view.findViewById(R.id.nacionalidade_txt)
        descricao_txt = view.findViewById(R.id.descricao_txt)

        btn_add = view.findViewById(R.id.btn_add)

        back_btn = view.findViewById(R.id.frame_back_button)

        pop_up = Dialog(requireContext())
        nome_error_txt =  view.findViewById(R.id.nome_error_txt)
        nome_error_icon =  view.findViewById(R.id.nome_error_icon)

        obra_imagem = view.findViewById(R.id.autor_info_image)
        editImgBtn = view.findViewById(R.id.edit_button)
        bt_bg_delpic = view.findViewById(R.id.deletepic_bg)
        bt_ic_delpic = view.findViewById(R.id.deletepic_ic)

        editImgBtn.setOnClickListener(){
            selectImage()
        }
        bt_ic_delpic.setOnClickListener(){
            bt_ic_delpic.visibility = View.GONE
            bt_bg_delpic.visibility = View.GONE

            imagemAlt = false
            obra_imagem.setImageURI(null)
            obra_imagem.setImageBitmap(null)
            del_img = true
        }



        back_btn.setOnClickListener(){


            //

            if(changed()){
                pop_up_salvar("Tem certeza de que deseja descartar as alterações?")

            }
            else{
                activity?.onBackPressed()
            }
        }

        btn_add.setOnClickListener(){

                onAdd_btnClicked()

            }






        startFragment()



        return view
    }


    private fun changed():Boolean{
        if(imgEdited){
            return true
        }

        if(nome_txt.text.toString()!=last_nome){
            return true
        }
        if(nascimento_txt.text.toString()!=last_nascimento){
            return true
        }
        if(falescimento_txt.text.toString()!=last_falescimento){
            return true
        }
        if(nacionalidade_txt.text.toString()!=last_nascimento){
            return true
        }
        if(descricao_txt.text.toString()!=last_descricao){
            return true
        }


        return false
    }

    private fun startFragment(){
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = Utilidades.getImage("imgAutores", autor.id)
            bitmap?.let {
                if(!it.equals(null)){
                    imagemAlt = true
                    bt_ic_delpic.visibility = View.VISIBLE
                    bt_bg_delpic.visibility = View.VISIBLE
                }
                obra_imagem.setImageBitmap(it) }
        }
        nome_txt.text = autor.nome
        nascimento_txt.text = autor.nascimento
        falescimento_txt.text = autor.falescimento
        nacionalidade_txt.text = autor.nacionalidade
        descricao_txt.text = autor.descricao

        last_nome= autor.nome
        last_nascimento= autor.nascimento
        last_falescimento= autor.falescimento
        last_nacionalidade= autor.nacionalidade
        last_descricao = autor.descricao


    }


    ////////////////////////////////////////////////



    fun nomeExistente():Boolean{
        for(obra in autores){
            if(obra.nome==nome_txt.text.toString()){
                return true
            }
        }
        return false
    }




    //==================================================================



    fun verificarNomeAutor():Boolean{


        if(nome_txt.text.toString()!=last_nome && nomeExistente()){
            nome_txt.setBackgroundResource(R.drawable.textbox_background_red)
            nome_error_txt.visibility = View.VISIBLE
            nome_error_icon.visibility = View.VISIBLE
            pop_up_error("O autor com o nome ${nome_txt} já está cadastrado, escolha outro nome para adicionar o autor")
        }else{
            if(nome_txt.text.isNullOrEmpty()){
                pop_up_error("O campo de Nome é um campo obrigatório, preencha o campo para cadastrar o autor.")
            }else{
                return true
            }
        }

        return false

    }
    ///===============================================

    fun pop_up_error(str:String){
        pop_up.setContentView(R.layout.add_error_pop_up)
        pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txt_error = pop_up.findViewById<TextView>(R.id.text_pop_up)
        val btn_cancel = pop_up.findViewById<ImageButton>(R.id.adm_pop_up_button_cancel)
        val btn_ok = pop_up.findViewById<AppCompatButton>(R.id.confirm_button)
        txt_error.text = str
        pop_up.show()
        btn_cancel.setOnClickListener(){
            pop_up.dismiss()
        }
        btn_ok.setOnClickListener(){
            pop_up.dismiss()
        }


    }



    ///////////////////////////////////////////////////////////////////////




    fun pop_up_salvar(str:String){
        pop_up.setContentView(R.layout.adm_delete_pop_up)
        pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txt_campos = pop_up.findViewById<TextView>(R.id.text_popup)
        val btn_cancel = pop_up.findViewById<ImageButton>(R.id.adm_pop_up_button_cancel)
        val btn_nao = pop_up.findViewById<AppCompatButton>(R.id.deny_button)
        val btn_sim = pop_up.findViewById<AppCompatButton>(R.id.confirm_button)
        txt_campos.text = str
        pop_up.show()
        btn_cancel.setOnClickListener(){

            pop_up.dismiss()
        }
        btn_nao.setOnClickListener(){

            pop_up.dismiss()
        }
        btn_sim.setOnClickListener(){
            adicionarObra()
            pop_up.dismiss()


        }


    }
    /////////////////////////////////////////////////////////////////////////////////

    fun onAdd_btnClicked(){
        resetText()


        if(verificarNomeAutor()){


             pop_up_salvar("Tem certeza de que deseja salvar as alterações do autor?")



        }



    }

    fun resetText(){
        nome_txt.setBackgroundResource(R.drawable.textbox_background)


        nome_error_txt.visibility = View.GONE
        nome_error_icon.visibility = View.GONE

    }


    //////////////////////////////////////////////////////////////////////////////////



    fun adicionarObra(){
        Toast.makeText(requireContext(), "Autor editado com sucesso", Toast.LENGTH_SHORT).show()
        var updates = hashMapOf<String,Any>(
            "nome" to nome_txt.text.toString(),
            "nascimento" to nascimento_txt.text.toString(),
            "falescimento" to falescimento_txt.text.toString(),
            "nacionalidade" to nacionalidade_txt.text.toString(),
            "descricao" to descricao_txt.text.toString()
        )
        Utilidades.alterarDado("autores",autor.id,updates)
        if(nome_txt.text.toString()!=last_nome){
            Utilidades.alterarAutorObras((requireActivity() as AdmMainActivity).database,last_nome,nome_txt.text.toString())
        }
        if(imgEdited){
            Utilidades.addImage("imgAutores",autor.id,imageUri)
        }
        if(del_img){
            Utilidades.deleteImage("imgAutores",autor.id)
        }
        (requireActivity() as AdmMainActivity).atualizarBanco()

        pausarExecucaoComCallback(300){
            activity?.onBackPressed()
        }

    }

    fun selectImage(){
        val intent = Intent()
        intent.type ="*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        requireActivity().startActivityForResult(intent,98)

    }
    lateinit var imageUri: Uri
    fun receberDados(uri: Uri){



        obra_imagem.setImageURI(uri)
        imageUri = uri
        imagemAlt = true
        bt_ic_delpic.visibility = View.VISIBLE
        bt_bg_delpic.visibility = View.VISIBLE
        imgEdited = true
        del_img = false

    }

    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }


}