package com.example.centrocultural

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult


class AdmMainActivity : AppCompatActivity() {

    var RQ_SPEECH_REC = 102
    private var bnv_selected:Boolean = true

    private lateinit var add_popup:Dialog



    private lateinit var user_home_btn : Button
    private lateinit var user_home_line : TextView
    private lateinit var user_home_icon : ImageView
    private lateinit var user_home_txt : TextView

    private lateinit var user_search_btn : Button
    private lateinit var user_search_line : TextView
    private lateinit var user_search_icon : ImageView
    private lateinit var user_search_txt : TextView

    private lateinit var user_add_btn : Button
    private lateinit var user_add_line : TextView
    private lateinit var user_add_icon : ImageView
    private lateinit var user_add_txt : TextView

    private lateinit var qrcode_button: Button
    val db = FirebaseFirestore.getInstance()

    var database:MutableList<Obra> = mutableListOf()
    var databaseAutores:MutableList<Autor> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_adm_main)

        database = mutableListOf()
        databaseAutores = mutableListOf()
        atualizarBanco()





        add_popup = Dialog(this)

        user_home_btn = findViewById(R.id.user_bnv_home_button)
        user_home_line = findViewById(R.id.user_bnv_home_selected_line)
        user_home_icon = findViewById(R.id.user_bnv_home_icon)
        user_home_txt = findViewById(R.id.user_bnv_home_txt)

        user_search_btn = findViewById(R.id.user_bnv_search_button)
        user_search_line = findViewById(R.id.user_bnv_search_selected_line)
        user_search_icon = findViewById(R.id.user_bnv_search_icon)
        user_search_txt = findViewById(R.id.user_bnv_search_txt)

        user_add_btn = findViewById(R.id.user_bnv_add_button)
        user_add_line = findViewById(R.id.user_bnv_add_selected_line)
        user_add_icon = findViewById(R.id.user_bnv_add_icon)
        user_add_txt = findViewById(R.id.user_bnv_add_txt)

        qrcode_button = findViewById(R.id.user_bnv_qrcode_button)


        user_home_btn.setOnClickListener(){
            replaceFragment(AdmHomeFragment())
            onUser_home_btnClicked()

        }
        user_search_btn.setOnClickListener(){
            replaceFragment(AdmFrameSearchFragment())
            onUser_search_btnClicked()
        }
        user_add_btn.setOnClickListener(){
            onUser_add_btnClicked()
        }
        qrcode_button.setOnClickListener(){
            val scanner = IntentIntegrator(this)
            scanner.setBeepEnabled(false)
            scanner.setOrientationLocked(true)
            scanner.setCaptureActivity(CaptureActivityPortrait::class.java)
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            scanner.initiateScan()
        }



        pausarExecucaoComCallback(1000){
            startAppAdm()
        }




    }



    fun onUser_home_btnClicked(){





        user_home_line.visibility = View.VISIBLE
        user_search_line.visibility = View.INVISIBLE
        user_add_line.visibility = View.INVISIBLE

        user_home_icon.background.setTint(getColor(R.color.selectedItem))
        user_search_icon.background.setTint(getColor(R.color.unselectedItem))

        user_home_txt.setTextColor(getColor(R.color.selectedItem))
        user_search_txt.setTextColor(getColor(R.color.unselectedItem))

        user_add_icon.background.setTint(getColor(R.color.unselectedItem))
        user_add_txt.setTextColor(getColor(R.color.unselectedItem))
        bnv_selected = true


    }

    fun onUser_search_btnClicked(){

        //alterar tela ------>>>(Search)



        user_search_line.visibility = View.VISIBLE
        user_home_line.visibility = View.INVISIBLE
        user_add_line.visibility = View.INVISIBLE

        user_search_icon.background.setTint(getColor(R.color.selectedItem))
        user_home_icon.background.setTint(getColor(R.color.unselectedItem))

        user_search_txt.setTextColor(getColor(R.color.selectedItem))
        user_home_txt.setTextColor(getColor(R.color.unselectedItem))

        user_add_icon.background.setTint(getColor(R.color.unselectedItem))
        user_add_txt.setTextColor(getColor(R.color.unselectedItem))
        bnv_selected = false

    }

    fun onUser_add_btnClicked(){





        user_search_line.visibility = View.INVISIBLE
        user_home_line.visibility = View.INVISIBLE
        user_add_line.visibility = View.VISIBLE

        user_search_icon.background.setTint(getColor(R.color.unselectedItem))
        user_home_icon.background.setTint(getColor(R.color.unselectedItem))

        user_search_txt.setTextColor(getColor(R.color.unselectedItem))
        user_home_txt.setTextColor(getColor(R.color.unselectedItem))

        user_add_icon.background.setTint(getColor(R.color.selectedItem))
        user_add_txt.setTextColor(getColor(R.color.selectedItem))



        add_popup.setContentView(R.layout.adm_add_pop_up)
        add_popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        add_popup.show()

        val btn_add_frame = add_popup.findViewById<Button>(R.id.add_frame_btn)
        val btn_add_autor = add_popup.findViewById<Button>(R.id.add_autor_btn)

        btn_add_frame.setOnClickListener(){
            replaceFragment(ObraAddFragment())
            add_popup.dismiss()
        }
        btn_add_autor.setOnClickListener(){
            replaceFragment(AutorAddFragment())
            add_popup.dismiss()
        }

        add_popup.setOnDismissListener(){
            if(bnv_selected){
                onUser_home_btnClicked()
            }else{
                onUser_search_btnClicked()
            }
        }

    }

    fun openFragmentInformation(obra:Obra){

        val fragmento = AdmObraFragment.newInstance(obra)
        val bundle = Bundle()
        bundle.putSerializable("chave", obra)
        fragmento.arguments = bundle


        supportFragmentManager.beginTransaction()
            .replace(R.id.adm_fragments_frame_layout, fragmento)
            .addToBackStack(null)
            .commit()
    }

    fun openFragmentInformationAutor(autor: Autor){

        val fragmento = AdmAutorFragment.newInstance(autor)
        val bundle = Bundle()
        bundle.putSerializable("chave2", autor)
        fragmento.arguments = bundle


        supportFragmentManager.beginTransaction()
            .replace(R.id.adm_fragments_frame_layout, fragmento)
            .addToBackStack(null)
            .commit()
    }

    fun openFragmentEdit(obra:Obra){
        val fragmento = ObraEditFragment.newInstance(obra)
        val bundle = Bundle()
        bundle.putSerializable("chave", obra)
        fragmento.arguments = bundle


        supportFragmentManager.beginTransaction()
            .replace(R.id.adm_fragments_frame_layout, fragmento,"111")
            .addToBackStack(null)
            .commit()
    }

    fun openFragmentEditAutor(autor: Autor){
        val fragmento = AutorEditFragment.newInstance(autor)
        val bundle = Bundle()
        bundle.putSerializable("chave2", autor)
        fragmento.arguments = bundle


        supportFragmentManager.beginTransaction()
            .replace(R.id.adm_fragments_frame_layout, fragmento,"111")
            .addToBackStack(null)
            .commit()
    }

     lateinit var imageUri: Uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==98 && resultCode== RESULT_OK){
            imageUri = data?.data!!
            (supportFragmentManager.findFragmentByTag("111") as AutorEditFragment).receberDados(imageUri)
        }
        else if(requestCode==99 && resultCode== RESULT_OK){
            imageUri = data?.data!!
            (supportFragmentManager.findFragmentByTag("111") as AutorAddFragment).receberDados(imageUri)
        }
        else if(requestCode==101 && resultCode== RESULT_OK){
            imageUri = data?.data!!
            (supportFragmentManager.findFragmentByTag("111") as ObraEditFragment).receberDados(imageUri)
        }
        else if(requestCode==100 && resultCode== RESULT_OK){
            imageUri = data?.data!!
            (supportFragmentManager.findFragmentByTag("111") as ObraAddFragment).receberDados(imageUri)
        }


        else if(requestCode==RQ_SPEECH_REC && resultCode== Activity.RESULT_OK){
            val result: ArrayList<String>? = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            (supportFragmentManager.findFragmentByTag("111") as AdmFrameSearchFragment).atualizarTexto(result?.get(0).toString())
        }
        else if(resultCode== RESULT_OK){
            val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
            if(result!=null){
                if(result.contents == null){
                    Toast.makeText(this,"Cancelled", Toast.LENGTH_LONG).show()
                }else{
                    var qrCode_result:String = result.contents
                    var indexObra = getQrCodeObra(qrCode_result)
                    if (indexObra==-1){
                        Toast.makeText(this,"Obra não identificada", Toast.LENGTH_LONG).show()
                    }else{
                        openFragmentInformation(database[indexObra])
                    }

                }
            }else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }


    }


    private fun getQrCodeObra(qrcode:String):Int{
        var num = 0
        for(obra in database){
            if(obra.qrCode == qrcode){
                return num
            }
            num++
        }
        return -1
    }








    private fun startAppAdm(){
        replaceFragment(AdmHomeFragment())
        onUser_home_btnClicked()
    }

    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }

    fun removerArrayObras(query:String){
        for(i in 0.. database.size-1){
            if(database[i].nome==query){
                database.removeAt(i)
                break
            }
        }
    }
    fun removerAutoresObras(query:String){
        for(i in 0.. databaseAutores.size-1){
            if(databaseAutores[i].nome==query){
                databaseAutores.removeAt(i)
                break
            }
        }
    }
    fun removerObraDoAutor(autor:String,obra:String){
        for(i in 0.. databaseAutores.size-1){
            if(databaseAutores[i].nome==autor){
                for(j in 0..databaseAutores[i].obras.size-1){
                    if(databaseAutores[i].obras[j]==obra){
                        databaseAutores[i].obras.removeAt(j);
                        break
                    }
                }
                break
            }
        }
    }

    fun idDoAutor(autor:String):String{
        for(i in databaseAutores){
            if(i.nome==autor){
                return i.id
            }
        }
        return ""
    }

    fun atualizarBanco(){
        database.clear()
        databaseAutores.clear()
        val collectionRef = db.collection("obras")
        collectionRef.get()
            .addOnSuccessListener { documents ->

                for (document in documents) {

                    val data = document.data

                    val qrcode:String = document.id
                    val nome:String = data.get("nome").toString()
                    val ano:String = data.get("ano").toString()
                    val autor:String = data.get("autor").toString()

                    val descricao:String = data.get("descricao").toString()

                    database.add(Obra(nome,autor,null,qrcode,ano,descricao))



                }
            }



        val collectionRefAutor = db.collection("autores")
        collectionRefAutor.get()
            .addOnSuccessListener { documents ->

                for (document in documents) {

                    val data = document.data


                    val nome:String = data.get("nome").toString()
                    val nascimento:String = data.get("nascimento").toString()
                    val falescimento:String = data.get("falescimento").toString()
                    val nacionalidade:String = data.get("nacionalidade").toString()

                    val descricao:String = data.get("descricao").toString()
                    val obras = data["obras"] as List<String>
                    var obrasarr : ArrayList<String> = arrayListOf()
                    for(i in obras){
                        obrasarr.add(i)
                    }

                    databaseAutores.add(Autor(nome,nascimento,falescimento,null,nacionalidade,descricao,document.id,obrasarr))



                }
            }.addOnFailureListener { exception ->

                Log.e("Firestore", "Erro ao obter documentos: $exception")
            }
    }


    private var isSafeToCommitFragmentTransaction = true

    override fun onResumeFragments() {
        super.onResumeFragments()
        isSafeToCommitFragmentTransaction = true
    }
    fun replaceFragment(fragment: Fragment) {
        if (isSafeToCommitFragmentTransaction) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.adm_fragments_frame_layout, fragment,"111")
                .addToBackStack(null)
                .commit()
        } else {
            // Se não for seguro, você pode lidar com isso de alguma forma, talvez adiando a transação
        }
    }




}