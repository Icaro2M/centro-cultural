package com.example.centrocultural

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity(){

    var RQ_SPEECH_REC = 102

    var administrador:Boolean = false
    var database:MutableList<Obra> = mutableListOf()
    var databaseAutores:MutableList<Autor> = mutableListOf()







    private lateinit var user_home_btn : Button
    private lateinit var user_home_line : TextView
    private lateinit var user_home_icon : ImageView
    private lateinit var user_home_txt : TextView

    private lateinit var user_search_btn : Button
    private lateinit var user_search_line : TextView
    private lateinit var user_search_icon : ImageView
    private lateinit var user_search_txt : TextView


    private lateinit var qrcode_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("obras")
        collectionRef.get()
            .addOnSuccessListener { documents ->
                // Iterando sobre os documentos retornados
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
            }


            .addOnFailureListener { exception ->

                Log.e("Firestore", "Erro ao obter documentos: $exception")
            }


        administrador = this.getSharedPreferences("MyPrefs", MODE_PRIVATE).getBoolean("adm",false)



        

        user_home_btn = findViewById(R.id.user_bnv_home_button)
        user_home_line = findViewById(R.id.user_bnv_home_selected_line)
        user_home_icon = findViewById(R.id.user_bnv_home_icon)
        user_home_txt = findViewById(R.id.user_bnv_home_txt)

        user_search_btn = findViewById(R.id.user_bnv_search_button)
        user_search_line = findViewById(R.id.user_bnv_search_selected_line)
        user_search_icon = findViewById(R.id.user_bnv_search_icon)
        user_search_txt = findViewById(R.id.user_bnv_search_txt)

        qrcode_button = findViewById(R.id.user_bnv_qrcode_button)

        qrcode_button.setOnClickListener(){
            val scanner = IntentIntegrator(this)
            scanner.setBeepEnabled(false)
            scanner.setOrientationLocked(true)
            scanner.setCaptureActivity(CaptureActivityPortrait::class.java)
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            scanner.initiateScan()
        }
        pausarExecucaoComCallback(1000){
            startAplication()
        }




        user_home_btn.setOnClickListener(){
            onUser_home_btnClicked()

        }
        user_search_btn.setOnClickListener(){
            onUser_search_btnClicked()
        }


    }
    private fun startAplication(){



        if(administrador){
            val intent = Intent(this, AdmMainActivity::class.java)
            startActivity(intent)
        }

        onUser_home_btnClicked()
        saveContentBoolean("saveBoolean",true,this)
        saveContentString("saveString","",this)
    }

    fun onUser_home_btnClicked(){


        //alterar tela ------>>>(Home)
        replaceFragment(UserHomeFragment())

        user_home_line.visibility = View.VISIBLE
        user_search_line.visibility = View.INVISIBLE

        user_home_icon.background.setTint(getColor(R.color.selectedItem))
        user_search_icon.background.setTint(getColor(R.color.unselectedItem))

        user_home_txt.setTextColor(getColor(R.color.selectedItem))
        user_search_txt.setTextColor(getColor(R.color.unselectedItem))


    }

    private fun onUser_search_btnClicked(){

        //alterar tela ------>>>(Search)

        replaceFragment(UserFrameSearchFragment())

        user_search_line.visibility = View.VISIBLE
        user_home_line.visibility = View.INVISIBLE

        user_search_icon.background.setTint(getColor(R.color.selectedItem))
        user_home_icon.background.setTint(getColor(R.color.unselectedItem))

        user_search_txt.setTextColor(getColor(R.color.selectedItem))
        user_home_txt.setTextColor(getColor(R.color.unselectedItem))

    }






    private fun saveContentBoolean(key:String, value: Boolean,context: Context){
        var sp:SharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
        var editor: SharedPreferences.Editor = sp.edit()
        editor.putBoolean(key,value)
        editor.apply()
    }
    private fun saveContentString(key:String, value: String,context: Context){
        var sp:SharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
        var editor: SharedPreferences.Editor = sp.edit()
        editor.putString(key,value)
        editor.apply()
    }



    fun openFragmentInformation(obra:Obra){
        Log.e("CREATION","funcao acessada 2")
        val fragmento = UserObraFragment.newInstance(obra)
        val bundle = Bundle()
        bundle.putSerializable("chave", obra)
        fragmento.arguments = bundle


        supportFragmentManager.beginTransaction()
            .replace(R.id.user_fragments_frame_layout, fragmento)
            .addToBackStack(null)
            .commit()
    }

    fun openFragmentInformationAutor(autor: Autor){

        val fragmento = UserAutorFragment.newInstance(autor)
        val bundle = Bundle()
        bundle.putSerializable("chave2", autor)
        fragmento.arguments = bundle


        supportFragmentManager.beginTransaction()
            .replace(R.id.user_fragments_frame_layout, fragmento)
            .addToBackStack(null)
            .commit()
    }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if(requestCode==RQ_SPEECH_REC && resultCode==Activity.RESULT_OK){
                val result: ArrayList<String>? = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                (supportFragmentManager.findFragmentByTag("111") as UserFrameSearchFragment).atualizarTexto(result?.get(0).toString())
            }
            else if(resultCode== RESULT_OK){
                val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
                if(result!=null){
                    if(result.contents == null){
                        Toast.makeText(this,"Cancelled",Toast.LENGTH_LONG).show()
                    }else{
                        var qrCode_result:String = result.contents
                        var indexObra = getQrCodeObra(qrCode_result)
                        if (indexObra==-1){
                            Toast.makeText(this,"Obra não identificada",Toast.LENGTH_LONG).show()
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

    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }

    private var isSafeToCommitFragmentTransaction = true

    override fun onResumeFragments() {
        super.onResumeFragments()
        isSafeToCommitFragmentTransaction = true
    }
    fun replaceFragment(fragment: Fragment) {
        if (isSafeToCommitFragmentTransaction) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.user_fragments_frame_layout, fragment,"111")
                .addToBackStack(null)
                .commit()
        } else {
            // Se não for seguro, você pode lidar com isso de alguma forma, talvez adiando a transação
        }
    }





}