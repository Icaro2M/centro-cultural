package com.example.centrocultural

import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat


class ObraAddFragment : Fragment(),AutorAddAdapter.OnItemClickListener {

    var qrcodeID:String = ""

    var imagemAlt:Boolean = false

    val db = FirebaseFirestore.getInstance()

    private lateinit var editImgBtn:ImageButton

    private lateinit var back_button:ImageButton

    private  lateinit var adapter: AutorAddAdapter

    private lateinit var bt_bg_delpic:TextView
    private lateinit var bt_ic_delpic:ImageButton

    private var savePos:Int = 0

    private lateinit var obras:MutableList<Obra>
    private lateinit var autores:MutableList<Autor>

    private lateinit var txt_nome:TextView
    private lateinit var txt_ano:TextView
    private lateinit var txt_autor:TextView
    private lateinit var txt_descricao:TextView
    private lateinit var obra_imagem:ImageView

    private lateinit var aut_option:View

    private lateinit var btn_add:Button

    private lateinit var scroll:ScrollView

    private lateinit var down_arrow:ImageButton
    private lateinit var up_arrow:ImageButton

    private lateinit var autoresRecycler:RecyclerView
    private lateinit var novoAutor:Button

    private lateinit var pop_up: Dialog

    private lateinit var nome_error_icon: ImageView
    private lateinit var nome_error_txt: TextView
    private lateinit var autor_error_icon: ImageView
    private lateinit var autor_error_txt: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_obra_add, container, false)

        obras = (requireActivity() as AdmMainActivity).database
        autores = (requireActivity() as AdmMainActivity).databaseAutores


        back_button = view.findViewById(R.id.frame_back_button)

        txt_nome = view.findViewById(R.id.name_txt)
        txt_ano = view.findViewById(R.id.ano_txt)
        txt_autor = view.findViewById(R.id.autor_txt)
        txt_descricao = view.findViewById(R.id.descricao_txt)
        obra_imagem = view.findViewById(R.id.frame_info_image)

        down_arrow = view.findViewById(R.id.down_arrow)
        up_arrow = view.findViewById(R.id.up_arrow)

        btn_add = view.findViewById(R.id.btn_add)

        scroll = view.findViewById(R.id.scroll)

        aut_option = view.findViewById(R.id.aut_option)

        autoresRecycler = view.findViewById(R.id.autores_add_recycler)
        novoAutor = view.findViewById(R.id.btn_novo_autor)

        bt_bg_delpic = view.findViewById(R.id.deletepic_bg)
        bt_ic_delpic = view.findViewById(R.id.deletepic_ic)

        nome_error_txt =  view.findViewById(R.id.nome_error_txt)
        nome_error_icon =  view.findViewById(R.id.nome_error_icon)
        autor_error_txt =  view.findViewById(R.id.autor_error_txt)
        autor_error_icon = view.findViewById(R.id.autor_error_icon)

        editImgBtn = view.findViewById(R.id.edit_button)


        editImgBtn.setOnClickListener(){
            selectImage()
        }
        bt_ic_delpic.setOnClickListener(){
            bt_ic_delpic.visibility = View.GONE
            bt_bg_delpic.visibility = View.GONE
            imagemAlt = false
            obra_imagem.setImageURI(null)
        }


        adapter = AutorAddAdapter((requireActivity() as AdmMainActivity).databaseAutores)
        autoresRecycler.layoutManager = LinearLayoutManager(requireContext())
        adapter.setOnItemClickListener(this)
        autoresRecycler.adapter = adapter




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
            if(imagemAlt || !txt_nome.text.isNullOrEmpty() || !txt_autor.text.isNullOrEmpty() || !txt_ano.text.isNullOrEmpty() || !txt_descricao.text.isNullOrEmpty()){

                pop_up_campos("Ao voltar, todas as informações inseridas serão perdidas. Deseja voltar?")
            }


            else{
                activity?.onBackPressed()
            }

        }


        btn_add.setOnClickListener(){
            onAdd_btnClicked()

        }





        return view
    }

    override fun onItemClick(autor: Autor) {
        Log.e("CREATION","CHEGOU NO CLICK")

        txt_autor.text = autor.nome
        aut_option.visibility = View.GONE

        backtxt()
        
    }



    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
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
    if(imagemAlt){
        return false
    }
        return true
    }





    //==================================================================

    fun campoEmBranco():ArrayList<String>{
        var list:ArrayList<String> = arrayListOf()

        if(txt_nome.text.isNullOrEmpty()){
            list.add("Nome")
        }
        if(txt_ano.text.isNullOrEmpty()){
            list.add("Ano")
        }
        if(txt_autor.text.isNullOrEmpty()){
            list.add("Autor")
        }
        if(txt_descricao.text.isNullOrEmpty()){
            list.add("Descrição")
        }


        return list
    }

    //========================================================

    fun verificarNomeAutor():Boolean{

        var errors:Int = 0
        var obraErrada:Boolean = false
        if(!txt_nome.text.isNullOrEmpty() && nomeExistente()){
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

    fun verificarCampos():Boolean{
        val listaCampo:ArrayList<String> = campoEmBranco()

        if(listaCampo.size!=0){

            var str:String = ""
            if(listaCampo.size>1){
                str="Os campos de ${listaCampo[0]}"
                for(i in 1..listaCampo.size-1){
                    if(i==listaCampo.size-1){
                        str+=" e ${listaCampo[i]}"
                    }
                    else{
                        str+=", ${listaCampo[i]}"
                    }
                }
                str+=" estão vazios, deseja adicionar a obra mesmo assim?"

            }else{
                str = "O campo de ${listaCampo[0]} está vazio, deseja adicionar a obra mesmo assim?"
            }
            pop_up_campos(str)
            return false


        }
        return true


    }


    fun pop_up_campos(str:String){
        pop_up.setContentView(R.layout.confirmation_pop_up)
        pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txt_campos = pop_up.findViewById<TextView>(R.id.text_pop_up)
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

                if(verificarCampos()){
                    adicionarObra()

                }


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
        Toast.makeText(requireContext(), "Obra adicionada com sucesso", Toast.LENGTH_SHORT).show()
        db.collection("obras").add(
            hashMapOf(
                "nome" to txt_nome.text.toString(),
                "ano" to txt_ano.text.toString(),
                "autor" to txt_autor.text.toString(),
                "descricao" to txt_descricao.text.toString()
            )
        ).addOnSuccessListener {
            documentReference->
            qrcodeID = documentReference.id
            if(imagemAlt){
                Log.e("CREATION","$imagemAlt")
                Utilidades.addImage("imgObras",qrcodeID,imageUri)
            }
            val oname:String = txt_nome.text.toString()
            pop_up.setContentView(R.layout.qrcode_popup)
            pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val img_popup = pop_up.findViewById<ImageView>(R.id.qrcode_image)
            val share = pop_up.findViewById<ImageButton>(R.id.share)
            val download = pop_up.findViewById<ImageButton>(R.id.download)
            val cancel = pop_up.findViewById<ImageButton>(R.id.cancel)
            val qrcodebitmap = Utilidades.generateQRCode(qrcodeID,400,400)
            img_popup.setImageBitmap(qrcodebitmap)

            pop_up.show()
            cancel.setOnClickListener(){
                pop_up.dismiss()
            }
            download.setOnClickListener(){
                downloadQRCode(qrcodebitmap,oname)
            }
            share.setOnClickListener(){
                shareQRCode(qrcodebitmap,oname)
            }
            Log.d(TAG, "Documento adicionado com ID: ${qrcodeID}")

        }.addOnFailureListener { e ->
            Log.w(TAG, "Erro ao adicionar documento", e)
        }
        if(!txt_autor.text.isNullOrEmpty()){
            Utilidades.addObraAoAutor((requireActivity() as AdmMainActivity).idDoAutor(txt_autor.text.toString()),txt_nome.text.toString())
        }

        (requireActivity() as AdmMainActivity).atualizarBanco()




        activity?.onBackPressed()
    }


    fun selectImage(){
        val intent = Intent()
        intent.type ="*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        requireActivity().startActivityForResult(intent,100)

    }
    lateinit var imageUri:Uri
    fun receberDados(uri: Uri){



        obra_imagem.setImageURI(uri)
        imageUri = uri
        imagemAlt = true
        bt_ic_delpic.visibility = View.VISIBLE
        bt_bg_delpic.visibility = View.VISIBLE

    }


    fun downloadQRCode(qrCodeBitmap: Bitmap?, name:String) {
        val rname = name.replace(' ','_')
        qrCodeBitmap?.let { bitmap ->
            val filename = "${rname}_qrcode.png"
            val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(filepath, filename)

            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                MediaScannerConnection.scanFile(requireContext(), arrayOf(file.toString()), null, null)

            } catch (e: IOException) {
                e.printStackTrace()

            }
        }
    }

    fun shareQRCode(qrCodeBitmap: Bitmap?, name: String) {
        val rname = name.replace(' ','_')
        val intent = Intent()
        intent.action = Intent.ACTION_SEND

        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver,qrCodeBitmap,"${rname}_qrcode",null)

        val uri = Uri.parse(path)
        intent.putExtra(Intent.EXTRA_STREAM,uri)
        intent.type="images/*"
        startActivity(Intent.createChooser(intent,"Compartilhar com: "))
    }











}

