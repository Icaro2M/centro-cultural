package com.example.centrocultural

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

class ObraEditFragment : Fragment(),AutorAddAdapter.OnItemClickListener {

    companion object{

        fun newInstance(obra:Obra):ObraEditFragment{
            val fragmento = ObraEditFragment()
            val args = Bundle()

            args.putSerializable("chave", obra)
            fragmento.arguments = args
            return fragmento
        }

    }
    var img_delete = false
    var img_edit = false
    private lateinit var last_nome:String
    private lateinit var last_ano:String
    private lateinit var last_autor:String
    private lateinit var last_descricao:String

    private lateinit var bt_bg_delpic:TextView
    private lateinit var bt_ic_delpic:ImageButton
    private lateinit var obra_imagem:ImageView
    var imagemAlt:Boolean = false


    private lateinit var obraInfo:Serializable
    private lateinit var obra:Obra



    private lateinit var back_button: ImageButton

    private  lateinit var adapter: AutorAddAdapter

    private var savePos:Int = 0

    private lateinit var obras:MutableList<Obra>
    private lateinit var autores:MutableList<Autor>

    private lateinit var txt_nome: TextView
    private lateinit var txt_ano: TextView
    private lateinit var txt_autor: TextView
    private lateinit var txt_descricao: TextView

    private lateinit var aut_option:View

    private lateinit var btn_add: Button

    private lateinit var scroll: ScrollView

    private lateinit var down_arrow: ImageButton
    private lateinit var up_arrow: ImageButton

    private lateinit var autoresRecycler: RecyclerView
    private lateinit var novoAutor: Button

    private lateinit var pop_up: Dialog

    private lateinit var nome_error_icon: ImageView
    private lateinit var nome_error_txt: TextView
    private lateinit var autor_error_icon: ImageView
    private lateinit var autor_error_txt: TextView
    private lateinit var editImgBtn:ImageButton



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_obra_edit, container, false)

        arguments?.let {
            Log.e("CREATION","item recuperado 3")
            obraInfo = it.getSerializable("chave") ?: throw IllegalArgumentException("Obra não encontrada")
        }
        obra = obraInfo as Obra


        obras = (requireActivity() as AdmMainActivity).database
        autores = (requireActivity() as AdmMainActivity).databaseAutores


        back_button = view.findViewById(R.id.frame_back_button)

        txt_nome = view.findViewById(R.id.name_txt)
        txt_ano = view.findViewById(R.id.ano_txt)
        txt_autor = view.findViewById(R.id.autor_txt)
        txt_descricao = view.findViewById(R.id.descricao_txt)





        down_arrow = view.findViewById(R.id.down_arrow)
        up_arrow = view.findViewById(R.id.up_arrow)

        btn_add = view.findViewById(R.id.btn_add)

        scroll = view.findViewById(R.id.scroll)

        aut_option = view.findViewById(R.id.aut_option)

        autoresRecycler = view.findViewById(R.id.autores_add_recycler)
        novoAutor = view.findViewById(R.id.btn_novo_autor)

        nome_error_txt =  view.findViewById(R.id.nome_error_txt)
        nome_error_icon =  view.findViewById(R.id.nome_error_icon)
        autor_error_txt =  view.findViewById(R.id.autor_error_txt)
        autor_error_icon = view.findViewById(R.id.autor_error_icon)
        obra_imagem = view.findViewById(R.id.frame_info_image)

        bt_bg_delpic = view.findViewById(R.id.deletepic_bg)
        bt_ic_delpic = view.findViewById(R.id.deletepic_ic)

        editImgBtn = view.findViewById(R.id.edit_button)


        adapter = AutorAddAdapter((requireActivity() as AdmMainActivity).databaseAutores)
        autoresRecycler.layoutManager = LinearLayoutManager(requireContext())
        adapter.setOnItemClickListener(this)
        autoresRecycler.adapter = adapter


        editImgBtn.setOnClickListener(){
            selectImage()
        }
        bt_ic_delpic.setOnClickListener(){
            bt_ic_delpic.visibility = View.GONE
            bt_bg_delpic.visibility = View.GONE
            img_delete = true


            imagemAlt = false
            obra_imagem.setImageURI(null)
            obra_imagem.setImageBitmap(null)
        }




        novoAutor.setOnClickListener(){
            (requireActivity() as AdmMainActivity).replaceFragment(AutorAddFragment())
        }

        txt_autor.setOnTouchListener{view, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->{

                    down_arrow.visibility = View.GONE





                    up_arrow.visibility = View.VISIBLE




                    view.onTouchEvent(event)



                    txtboxSet()



                    true
                }
                else -> false
            }

        }


        txt_autor.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                onSearch(s.toString())

            }

            override fun afterTextChanged(s: Editable?) {



            }

        })




        down_arrow.setOnClickListener(){
            txtboxSet()
        }
        up_arrow.setOnClickListener(){
            down_arrow.visibility = View.VISIBLE
            up_arrow.visibility = View.GONE
            aut_option.visibility = View.GONE
            backtxt()
        }

        pop_up = Dialog(requireContext())

        back_button.setOnClickListener(){


            if(changed()){
                pop_up_salvar("Tem certeza de que deseja descartar as alterações?")

            }

            else{
                activity?.onBackPressed()
            }

        }


        btn_add.setOnClickListener(){

            if(!changed()){
                activity?.onBackPressed()
            }

            else{
                onAdd_btnClicked()


            }







        }

        startFragment()



        return view
    }

    override fun onItemClick(autor: Autor) {


        txt_autor.text = autor.nome
        aut_option.visibility = View.GONE

        backtxt()


    }





    fun txtboxSet(){
        Log.e("CREATION","chegou textbox")
        savePos = scroll.scrollY
        val layoutParams = btn_add.layoutParams as ViewGroup.MarginLayoutParams
        val novaMargem = resources.getDimensionPixelSize(R.dimen.nova_margem) // substitua "nova_margem_bottom" pelo valor desejado em dimens.xml
        layoutParams.bottomMargin = novaMargem
        btn_add.layoutParams = layoutParams

        scroll.post(Runnable { scroll.scrollTo(0, scroll.getBottom()) })

        aut_option.visibility = View.VISIBLE
    }
    fun backtxt(){
        val layoutParams = btn_add.layoutParams as ViewGroup.MarginLayoutParams
        val novaMargem = resources.getDimensionPixelSize(R.dimen.margem_padrao) // substitua "nova_margem_bottom" pelo valor desejado em dimens.xml
        layoutParams.bottomMargin = novaMargem
        btn_add.layoutParams = layoutParams

        scroll.post(Runnable { scroll.scrollTo(0, savePos) })

        esconderTeclado(txt_autor)
    }
    fun esconderTeclado(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun arrayFiltrado(query: String):MutableList<Autor>{
        return autores.filter { autor-> autor.nome.contains(query, ignoreCase = true) }.toMutableList()
    }

    private fun onSearch(query: String){
        adapter.atualizarDados(arrayFiltrado(query))
    }
    private fun changed():Boolean{
        if(img_edit){
            return true
        }

        if(txt_nome.text.toString() != last_nome){
            return true
        }
        if(txt_ano.text.toString() != last_ano){
            return true
        }
        if(txt_autor.text.toString() != last_autor){
            return true
        }
        if(txt_descricao.text.toString() != last_descricao){
            return true
        }

        return false
    }


    private fun startFragment(){

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = Utilidades.getImage("imgObras", obra.qrCode)
            bitmap?.let {
                if(!it.equals(null)){
                    imagemAlt = true
                    bt_ic_delpic.visibility = View.VISIBLE
                    bt_bg_delpic.visibility = View.VISIBLE
                }
                obra_imagem.setImageBitmap(it) }
        }

        txt_nome.text = obra.nome
        txt_ano.text = obra.ano
        txt_autor.text = obra.autor
        txt_descricao.text = obra.descricao


        last_nome = obra.nome
        last_ano = obra.ano
        last_autor = obra.autor
        last_descricao = obra.descricao
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun nomeExistente():Boolean{
        for(obra in obras){
            if(obra.nome==txt_nome.text.toString()){
                return true
            }
        }
        return false
    }

    fun autorNaoCadastrado():Boolean{
        for(autor in autores){
            if(autor.nome==txt_autor.text.toString()){
                return true
            }
        }


        return false
    }

    fun fotoNaoInserina():Boolean{
    if(imagemAlt || !img_delete){
        return false
    }
        return true
    }








    //========================================================

    fun verificarNomeAutor():Boolean{

        var errors:Int = 0
        var obraErrada:Boolean = false
        if(txt_nome.text.toString()!=last_nome && !txt_nome.text.isNullOrEmpty() &&  nomeExistente()){
            errors++
            obraErrada = true
            txt_nome.setBackgroundResource(R.drawable.textbox_background_red)
            nome_error_txt.visibility = View.VISIBLE
            nome_error_icon.visibility = View.VISIBLE
        }
        if(!txt_autor.text.isNullOrEmpty() &&  !autorNaoCadastrado()){
            errors++
            txt_autor.setBackgroundResource(R.drawable.textbox_background_red)
            autor_error_txt.visibility = View.VISIBLE
            autor_error_icon.visibility = View.VISIBLE

        }

        when(errors){

            0-> return true

            1->{

                if(obraErrada){
                    pop_up_error("A obra com nome ${txt_nome.text} já foi adicionada, escolha outro nome para adicionar a obra.")

                }else{
                    pop_up_error("O autor com nome ${txt_autor.text} não foi cadastrado, cadastre o autor para adicionar a obra.")

                }

            }

            2-> pop_up_error("A obra com nome ${txt_nome.text} já foi adicionada e o autor com nome ${txt_autor.text} não foi cadastrado.")

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

        if(fotoNaoInserina()){

            pop_up_error("A imagem é um campo obrigatório, preencha o campo de imagem para cadastar a obra.")
        }else{
            if(verificarNomeAutor()){


                pop_up_salvar("Tem certeza de que deseja salvar as alterações da obra?")



            }


        }
    }

    fun resetText(){
        txt_nome.setBackgroundResource(R.drawable.textbox_background)
        txt_autor.setBackgroundResource(R.drawable.textbox_background)

        nome_error_txt.visibility = View.GONE
        nome_error_icon.visibility = View.GONE
        autor_error_txt.visibility = View.GONE
        autor_error_icon.visibility = View.GONE
    }


    //////////////////////////////////////////////////////////////////////////////////



    fun adicionarObra(){
        Toast.makeText(requireContext(), "Obra editada com sucesso", Toast.LENGTH_SHORT).show()
        var updates = hashMapOf<String,Any>(
            "nome" to txt_nome.text.toString(),
            "ano" to txt_ano.text.toString(),
            "autor" to txt_autor.text.toString(),
            "descricao" to txt_descricao.text.toString()

        )
        Utilidades.alterarDado("obras",obra.qrCode,updates)
        val id_autor = (requireActivity() as AdmMainActivity).idDoAutor(txt_autor.text.toString())
        if(txt_autor.text.toString()!=last_autor){

            Utilidades.addObraAoAutor(id_autor,txt_nome.text.toString())

            Utilidades.removerObradoAutor(id_autor,txt_nome.text.toString())



        }else{
            if(txt_nome.text.toString()!=last_nome && !txt_autor.text.isNullOrEmpty()){
                Utilidades.alterarArray(id_autor,last_nome,txt_nome.text.toString())
            }
        }
        if(img_edit){
            Utilidades.addImage("imgObras",obra.qrCode,imageUri)
        }

        (requireActivity() as AdmMainActivity).atualizarBanco()









        activity?.onBackPressed()
    }

    fun selectImage(){
        val intent = Intent()
        intent.type ="*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        requireActivity().startActivityForResult(intent,101)

    }
    lateinit var imageUri: Uri
    fun receberDados(uri: Uri){


        img_delete = false
        obra_imagem.setImageURI(uri)
        imageUri = uri
        imagemAlt = true
        bt_ic_delpic.visibility = View.VISIBLE
        bt_bg_delpic.visibility = View.VISIBLE
        img_edit = true

    }


}