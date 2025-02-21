package com.sarajimenez.zenify.fragments.home

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sarajimenez.zenify.R

class FragmentHome : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var playPauseButton: ImageButton
    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val userWelcome = view.findViewById<TextView>(R.id.userWelcome)

        // Variables del Firebase
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userId = user.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username") ?: "Usuario"
                        userWelcome.text = username
                    } else {
                        Log.d("MainActivity", "No se encontró el documento del usuario")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error obteniendo el username", e)
                }
        }



        // Inicializa los componentes del layout
        playPauseButton = view.findViewById(R.id.playPauseButton)
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.musica_fondo)

        // Configura el comportamiento del botón play/pause
        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.ic_pause)
            }
        }

        // Subir imagen desde Firestore
        imageView = view.findViewById(R.id.decorativeImage) // Reemplaza con el ID de tu ImageView

        // URL de la imagen que deseas cargar
        val imageUrl = "https://i.imgur.com/Iv98Tw2.png"

        // Cargar la imagen con Glide
        Glide.with(this)  // Usar el contexto adecuado (activity o fragment)
            .load(imageUrl)  // La URL de la imagen
            .placeholder(R.drawable.cloud)  // Imagen mientras carga
            .error(R.drawable.error_image)  // Imagen si ocurre un error
            .into(imageView)  // El ImageView donde mostrar la imagen


        // Cargar el video de YouTube en el WebView
        val videoView: WebView = view.findViewById(R.id.videoView)
        val videoUrl = "https://www.youtube.com/embed/OfWxhQGY3CY?si=kTU_TfRok5itl5T0" // Video embebido
        videoView.settings.javaScriptEnabled = true
        videoView.webChromeClient = WebChromeClient()  // Mejorar la compatibilidad con videos
        videoView.loadUrl(videoUrl)

        return view
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop() // Detener la música cuando el fragmento se detiene
        }
        mediaPlayer.release() // Liberar los recursos del MediaPlayer
    }
}
