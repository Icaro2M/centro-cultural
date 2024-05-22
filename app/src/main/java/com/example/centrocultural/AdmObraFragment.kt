package com.example.centrocultural

import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.util.Locale

class AdmObraFragment : Fragment(), TextToSpeech.OnInitListener {


    private var str:String = ""

    private lateinit var tts: TextToSpeech

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false
    private var resumePosition: Int = 0
    private var startAudio:Boolean = true
    private var startAgain:Boolean = false


    private lateinit var pop_up:Dialog
    private lateinit var qrCodeButton: Button
    private lateinit var edit_btn:ImageButton
    private lateinit var remove_btn:ImageButton

    private  lateinit var obraInfo: Serializable
    private lateinit var obra_nome: TextView
    private lateinit var obra_ano: TextView
    private lateinit var obra_image: ImageView
    private lateinit var obra_autor_nome: TextView
    private lateinit var obra_autor_image: ImageView
    private lateinit var obra_descrição: TextView

    var autor:Autor? = null

    private lateinit var obra:Obra

    private lateinit var play_btn: ImageButton
    private lateinit var pause_btn: ImageButton
    private lateinit var audio_icon: ImageView
    private lateinit var audio_bar: SeekBar

    private lateinit var autorButton: Button

    private lateinit var back_button: ImageButton

    companion object{

        fun newInstance(obra:Obra):AdmObraFragment{
            val fragmento = AdmObraFragment()
            val args = Bundle()

            args.putSerializable("chave", obra)
            fragmento.arguments = args
            return fragmento
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_adm_obra, container, false)
        tts = TextToSpeech(requireContext(), this)

        arguments?.let {
            Log.e("CREATION","item recuperado 3")
            obraInfo = it.getSerializable("chave") ?: throw IllegalArgumentException("Obra não encontrada")
        }
        obra = obraInfo as Obra

        val array = (requireActivity() as AdmMainActivity).databaseAutores

        for(i in array){
            if(i.nome== obra.autor) {
                autor = i

            }
        }

        obra_nome = view.findViewById(R.id.frame_info_name)
        obra_image = view.findViewById(R.id.frame_info_image)
        obra_autor_nome = view.findViewById(R.id.frame_info_autor_name)
        obra_autor_image = view.findViewById(R.id.frame_info_autor_image)
        obra_ano = view.findViewById(R.id.frame_info_year)
        obra_descrição = view.findViewById(R.id.frame_info_description)
        autorButton = view.findViewById(R.id.autor_button)

        qrCodeButton = view.findViewById(R.id.qrcode_button)
        edit_btn = view.findViewById(R.id.edit_button)
        remove_btn = view.findViewById(R.id.remove_button)

        pop_up = Dialog(requireContext())

        edit_btn.setOnClickListener(){
            (requireActivity() as AdmMainActivity).openFragmentEdit(obra)
        }

        qrCodeButton.setOnClickListener(){
            pop_up.setContentView(R.layout.qrcode_popup)
            pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val img_popup = pop_up.findViewById<ImageView>(R.id.qrcode_image)
            val share = pop_up.findViewById<ImageButton>(R.id.share)
            val download = pop_up.findViewById<ImageButton>(R.id.download)
            val cancel = pop_up.findViewById<ImageButton>(R.id.cancel)
            val qrcodebitmap = Utilidades.generateQRCode(obra.qrCode,400,400)
            img_popup.setImageBitmap(qrcodebitmap)
            pop_up.show()
            cancel.setOnClickListener(){
                pop_up.dismiss()
            }
            download.setOnClickListener(){
                downloadQRCode(qrcodebitmap,obra.nome)
            }
            share.setOnClickListener(){
                shareQRCode(qrcodebitmap,obra.nome)
            }


        }

        remove_btn.setOnClickListener(){
            pop_up.setContentView(R.layout.adm_delete_pop_up)
            var txt_popup = pop_up.findViewById<TextView>(R.id.text_popup)
            txt_popup.text = "Tem certeza de que deseja remover a obra ${obra.nome}?"


            pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pop_up.show()

            var sim_btn = pop_up.findViewById<Button>(R.id.confirm_button)
            var nao_btn = pop_up.findViewById<Button>(R.id.deny_button)
            var cancel_btn = pop_up.findViewById<ImageButton>(R.id.adm_pop_up_button_cancel)

            sim_btn.setOnClickListener(){
                Utilidades.removerElemento("obras",obra.qrCode)


                pop_up.dismiss()
                if(!obra.autor.isNullOrEmpty()){
                    Utilidades.removerObradoAutor((requireActivity() as AdmMainActivity).idDoAutor(obra.autor),obra.nome)

                }
                (requireActivity() as AdmMainActivity).atualizarBanco()
                pausarExecucaoComCallback(300){
                    (requireActivity() as AdmMainActivity).onUser_home_btnClicked()
                    (requireActivity() as AdmMainActivity).replaceFragment(AdmHomeFragment())
                }



            }
            nao_btn.setOnClickListener(){
                pop_up.dismiss()
            }
            cancel_btn.setOnClickListener(){
                pop_up.dismiss()
            }
        }




        play_btn = view.findViewById(R.id.play_audio)
        pause_btn = view.findViewById(R.id.pause_audio)
        audio_icon = view.findViewById(R.id.audio_ic)
        audio_bar = view.findViewById(R.id.audio_bar)

        if(obra.autor=="Sem informação"){
            autorButton.visibility == View.INVISIBLE
        }

        autorButton.setOnClickListener(){


            (requireActivity() as AdmMainActivity).openFragmentInformationAutor(autor!!)
        }

        back_button = view.findViewById(R.id.frame_back_button)
        back_button.setOnClickListener(){
            activity?.onBackPressed()
            Log.e("CREATION","deu erro")
        }

        if(!obra.nome.isNullOrEmpty()){
            obra_nome.text = obra.nome
        }else{
            obra_nome.text = "Sem informação"
        }

        if(!obra.autor.isNullOrEmpty()){
            obra_autor_nome.text = obra.autor
        }else{
            obra_autor_nome.text = "Sem informação"
        }

        if(!obra.ano.isNullOrEmpty()){
            obra_ano.text = obra.ano
        }else{
            obra_ano.text = "Sem informação"
        }

        if(!obra.descricao.isNullOrEmpty()){
            obra_descrição.text = obra.descricao
        }else{
            obra_descrição.text = "Sem informação"
        }
        if(!obra.autor.isNullOrEmpty()){
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap = Utilidades.getImage("imgAutores", autor!!.id)
                bitmap?.let { obra_autor_image.setImageBitmap(it) }
            }
        }
        loadAndSetImage()










        handler.postDelayed(updateSeekBar, 0)




        audio_bar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        val handler2 = Handler()
        var iniciarAudio = false
        handler2.postDelayed({
            iniciarAudio = true
        },4000)
        play_btn.setOnClickListener(){
            if(iniciarAudio){
                playAudio()
            }


        }
        pause_btn.setOnClickListener(){
            pauseAudio()
        }



        return view
    }



    private fun playAudio(){
        play_btn.visibility = View.INVISIBLE
        pause_btn.visibility = View.VISIBLE
        audio_icon.background.setTint(resources.getColor(R.color.selectedItem))
        if(startAudio){
            startAudio = false
            mediaPlayer = MediaPlayer().apply {
                val filePath = File(requireContext().cacheDir, "audio_file_unifor.mp3").absolutePath
                setDataSource(filePath)
                prepare()
                start()
                setOnPreparedListener(){
                    audio_bar.max = mediaPlayer?.duration!!
                }
                setOnCompletionListener {
                    mediaPlayer?.seekTo(0)
                    play_btn.visibility = View.VISIBLE
                    pause_btn.visibility = View.INVISIBLE
                    startAgain = true

                }

            }


        }else if(startAgain){
            startAgain = false
            mediaPlayer?.start()

        }
        else{
            mediaPlayer?.apply {
                if (isPaused) {
                    seekTo(resumePosition)
                    start()
                    isPaused = false
                }
            }
        }


    }



    private fun pauseAudio(){
        play_btn.visibility = View.VISIBLE
        pause_btn.visibility = View.INVISIBLE
        audio_icon.background.setTint(resources.getColor(R.color.unselectedItem))
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                isPaused = true
                resumePosition = currentPosition
            }
        }


    }















    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = tts.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Trate o caso em que a linguagem não é suportada
            } else {
                // Salvar o áudio em um arquivo
                val texto:String = generateString()
                val file = File(requireContext().cacheDir, "audio_file_unifor.mp3")

                tts.synthesizeToFile(texto, null,file, null)
            }
        }
    }

    private val handler = Handler()

    private val updateSeekBar = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                audio_bar.progress = currentPosition
            }
            handler.postDelayed(this, 100) // Atualize a cada 1 segundo
        }
    }


    private fun generateString():String{
        str = ""

        var bnome:Boolean = !obra.nome.isNullOrEmpty()
        var bano:Boolean = !obra.ano.isNullOrEmpty()
        var bautor:Boolean = !obra.autor.isNullOrEmpty()
        var bdescricao:Boolean = !obra.descricao.isNullOrEmpty()

        if(!bnome && !bano && !bautor && !bdescricao){
            str = "Não existe nenhuma informação sobre essa obra."
        }


        if(!bnome && !bano && !bautor && bdescricao){
            str = obra.descricao
        }

        if(!bnome && !bano && bautor && !bdescricao){
            str = "Esta é uma obra não identificada feita por ${obra.autor}."
        }

        if(!bnome && !bano && bautor && bdescricao){
            str = "Esta é uma obra de ${obra.autor}; ${obra.descricao}"
        }

        if(!bnome && bano && !bautor && !bdescricao){
            str = "Está é uma obra feita em ${obra.ano}, não se tem mais informações sobre ela."
        }

        if(!bnome && bano && !bautor && bdescricao){
            str = "Está é uma obra feita em ${obra.ano}; ${obra.descricao}"
        }

        if(!bnome && bano && bautor && !bdescricao){
            str = "Esta é uma obra não identificada feita em ${obra.ano} por ${obra.autor}."
        }

        if(!bnome && bano && bautor && bdescricao){
            str = "Esta é uma obra não identificada feita em ${obra.ano} por ${obra.autor}; ${obra.descricao}"
        }
        if(bnome && !bano && !bautor && !bdescricao){
            str = "O nome desta obra é ${obra.nome}, não se tem mais informações sobre ela."
        }

        if(bnome && !bano && !bautor && bdescricao){
            str = "O nome desta obra é ${obra.nome}; ${obra.descricao}"
        }

        if(bnome && !bano && bautor && !bdescricao){
            str = "O nome desta obra é ${obra.nome}, de ${obra.autor}."
        }

        if(bnome && !bano && bautor && bdescricao){
            str = "O nome desta obra é ${obra.nome}, de ${obra.autor}; ${obra.descricao}"
        }

        if(bnome && bano && !bautor && !bdescricao){
            str = "O nome desta obra é ${obra.nome}, feita em ${obra.ano}, não se tem mais informações sobre ela."
        }

        if(bnome && bano && !bautor && bdescricao){
            str = "O nome desta obra é ${obra.nome}, feita em ${obra.ano}; ${obra.descricao}"
        }

        if(bnome && bano && bautor && !bdescricao){
            str = "O nome desta obra é ${obra.nome}, feita em ${obra.ano} por ${obra.autor}."
        }

        if(bnome && bano && bautor && bdescricao){
            str = "O nome desta obra é ${obra.nome}, feita em ${obra.ano} por ${obra.autor}; ${obra.descricao}"
        }

        return str


    }

    fun loadAndSetImage() {
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = Utilidades.getImage("imgObras", obra.qrCode)
            bitmap?.let { obra_image.setImageBitmap(it) }
        }
    }
    fun pausarExecucaoComCallback(milissegundos: Long, callback: () -> Unit) {
        // Usando um Handler para postar uma execução com atraso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            callback()
        }, milissegundos)
    }



    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()

        handler.removeCallbacks(updateSeekBar)
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
                Toast.makeText(requireContext(), "QR Code baixado com sucesso", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao salvar o QR Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareQRCode(qrCodeBitmap: Bitmap?,name: String) {
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