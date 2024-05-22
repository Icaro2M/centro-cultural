package com.example.centrocultural

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.Serializable
import java.util.Locale


class UserAutorFragment : Fragment(), ObraAdapter.OnItemClickListener,TextToSpeech.OnInitListener{
    private lateinit var autorInfo:Serializable


    private var str:String = ""

    private lateinit var tts: TextToSpeech

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false
    private var resumePosition: Int = 0
    private var startAudio:Boolean = true
    private var startAgain:Boolean = false

    private lateinit var play_btn:ImageButton
    private lateinit var pause_btn:ImageButton
    private lateinit var audio_icon:ImageView
    private lateinit var audio_bar: SeekBar

    private lateinit var autor: Autor



    private lateinit var autor_nome:TextView
    private lateinit var autor_data:TextView
    private lateinit var autor_nacionalidade:TextView
    private lateinit var descricao:TextView
    private lateinit var autor_imagem:ImageView
    private lateinit var autor_obras: MutableList<Obra>
    private lateinit var obrasArray: MutableList<Obra>

    private lateinit var back_button: ImageButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:ObraAdapter


    companion object{

        fun newInstance(autor: Autor):UserAutorFragment{
            val fragmento = UserAutorFragment()
            val args = Bundle()

            args.putSerializable("chave2", autor)
            fragmento.arguments = args
            return fragmento
        }

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_autor, container, false)

        arguments?.let {

            autorInfo = it.getSerializable("chave2") ?: throw IllegalArgumentException("Autor não encontrado")
        }
        autor = autorInfo as Autor
        if(autor.obras.size==0){
            var linhaObras = view.findViewById<TextView>(R.id.obras_autor)
            linhaObras.visibility = View.INVISIBLE
        }



        tts = TextToSpeech(requireContext(), this)

        autor_nome = view.findViewById(R.id.autor_info_name)
        autor_data = view.findViewById(R.id.autor_info_year)
        autor_nacionalidade = view.findViewById(R.id.nacionalidade)
        descricao = view.findViewById(R.id.autor_info_description)
        autor_imagem = view.findViewById(R.id.autor_info_image)
        obrasArray = (requireActivity() as MainActivity).database

        setInfo()



        play_btn = view.findViewById(R.id.play_audio)
        pause_btn = view.findViewById(R.id.pause_audio)
        audio_icon = view.findViewById(R.id.audio_ic)
        audio_bar = view.findViewById(R.id.audio_bar)

        back_button = view.findViewById(R.id.frame_back_button)
        back_button.setOnClickListener(){
            activity?.onBackPressed()
        }

        autor_obras = arrayFiltrado(autor.nome)

        recyclerView = view.findViewById(R.id.autor_obras_recycler)
        adapter = ObraAdapter(autor_obras)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter


        handler.postDelayed(updateSeekBar, 0)




        audio_bar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
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




    private fun arrayFiltrado(query: String):MutableList<Obra>{

        var obrasFiltradas:MutableList<Obra> = mutableListOf()
        for(i in obrasArray){
            if(i.autor==query){
                obrasFiltradas.add(i)

            }

        }

        return obrasFiltradas

    }

    override fun onItemClick(obra: Obra) {
        (requireActivity() as MainActivity).openFragmentInformation(obra)
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

    private fun generateString():String{
        str = ""
        val bnome:Boolean = !autor.nome.isNullOrEmpty()
        val bnascimento:Boolean = !autor.nascimento.isNullOrEmpty()
        val bfalescimento:Boolean = !autor.falescimento.isNullOrEmpty()
        val bnacionalidade:Boolean = !autor.nacionalidade.isNullOrEmpty()
        val bdescricao:Boolean = !autor.descricao.isNullOrEmpty()
        val bobras:Int = autor.obras.size

        if(bnome){
            str+="O nome do autor é ${autor.nome}; "
        }
        if(bnascimento){
            str+="nascido em ${autor.nascimento}; "
            if(bfalescimento){
                if(bnacionalidade){
                    str+="em ${autor.nacionalidade} ;"
                }
                str+="e falescido em ${autor.falescimento}; "
            }

        }
        if(bdescricao){
            str+=" ${autor.descricao}; "
        }
        if(bobras>0){
            if(bobras>1){
                str+="algumas de suas obras são; "
                for(i in 0..bobras-1){
                    if(i==bobras-1){
                       str+="e ${autor.obras[i]}; "
                    }
                    else{
                        str+="${autor.obras[i]}; "
                    }
                }
            }
            else{
                str+="Uma de suas obras é ${autor.obras[0]}; "
            }
        }




        return str

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

    private fun setInfo(){
        var data:String = ""

        if(!autor.nome.isNullOrEmpty()){
            autor_nome.text = autor.nome
        }else{
            autor_nome.text = "Sem informação"
        }

        if(!autor.nascimento.isNullOrEmpty()){

            data = "${autor.nascimento}"
        }else{

            data = "?"
        }

        if(!autor.falescimento.isNullOrEmpty()){
            autor_data.text = data+" - ${autor.falescimento}"
        }
        else{
            autor_data.text = data
        }

        if(!autor.nacionalidade.isNullOrEmpty()){
            autor_nacionalidade.text = autor.nacionalidade
        }else{
            autor_nacionalidade.text = "Sem informação"
        }

        if(!autor.descricao.isNullOrEmpty()){
            descricao.text = autor.descricao
        }else{
            view?.findViewById<TextView>(R.id.description)?.visibility = View.INVISIBLE
            descricao.visibility = View.INVISIBLE
        }
        loadAndSetImage()




    }


    fun loadAndSetImage() {
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = Utilidades.getImage("imgAutores", autor.id)
            bitmap?.let { autor_imagem.setImageBitmap(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()

        handler.removeCallbacks(updateSeekBar)
    }

}