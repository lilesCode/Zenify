package com.sarajimenez.zenify

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {

    //SharedPreferences
    object Global{
        var preferencias_compartidas = "sharedpreferences"
    }

    //Contraseña: 123123

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var auth : FirebaseAuth
    private lateinit var textRecoverPass : TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()          //Referenciamos los componentes
        start()         //Comienza todo

    }

    //Metodo que comprueba si hay una sesion activa antes de mostrar el login
    override fun onStart() {
        super.onStart()

        // Comprobar si el usuario ya está logueado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Si el usuario está logueado, ir directamente a MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra LoginActivity para evitar que el usuario regrese a ella
        }
    }


    private fun init(){
        usernameEditText = findViewById(R.id.userEditText)
        passwordEditText = findViewById(R.id.passEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        auth = Firebase.auth
        textRecoverPass = findViewById(R.id.forgotPasswordText)

    }

    private fun start(){
        //Listener del Button
        loginButton.setOnClickListener{
            val userInput = usernameEditText.text.toString().trim()
            val passInput = passwordEditText.text.toString().trim()

            if(userInput.isNotEmpty() && passInput.isNotEmpty()) {
                //El método recibe el usuario, la contraseña y un método
                startLogin(userInput, passInput) { result, msg ->
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    if (result) {
                        intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }else{
                Toast.makeText(this, "Tienes algún campo vacío", Toast.LENGTH_LONG).show()
            }
        }

        //Listener boton registrar

        registerButton.setOnClickListener{
            intent = Intent (this, RegisterActivity::class.java)
            startActivity(intent)
        }

        textRecoverPass.setOnClickListener{
            val user = usernameEditText.text.toString()
            if (user.isNotEmpty())
                recoverPassword(user){
                        result, msg ->
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    if (!result)
                        usernameEditText.setText("")
                }
            else
                Toast.makeText(this, "Debes rellenar el campo email", Toast.LENGTH_LONG).show()
        }

    }

    private fun recoverPassword(email : String, onResult: (Boolean, String)->Unit){
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener{
                    taskResetEmail ->
                if (taskResetEmail.isSuccessful){
                    onResult (true, "Acabamos de enviarte un email con la nueva contraseña")
                }else{
                    var msg = ""
                    try{
                        throw taskResetEmail.exception?:Exception("Error de reseteo inesperado")
                    }catch (e : FirebaseAuthInvalidCredentialsException){
                        msg = "El formato del email es incorrecto"
                    }catch (e: Exception){
                        msg = e.message.toString()
                    }
                    onResult(false, msg)
                }
            }
    }

    //startLogin() nuevo
    private fun startLogin(user: String, pass: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(user, pass)
            .addOnCompleteListener { taskAssin ->
                if (taskAssin.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val db = Firebase.firestore
                        db.collection("users").document(currentUser.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val username = document.getString("username") ?: "Usuario"

                                    // Guardamos la sesión en SharedPreferences
                                    val sharedPreferences = getSharedPreferences(Global.preferencias_compartidas, Context.MODE_PRIVATE)
                                    with(sharedPreferences.edit()) {
                                        putString("Correo", user)
                                        putString("NombreUsuario", username)
                                        apply()
                                    }

                                    // Pasamos a la MainActivity
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    onResult(false, "No se encontró el usuario en la base de datos")
                                }
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Error al obtener usuario: ${e.message}")
                            }
                    }
                } else {
                    val exception = taskAssin.exception
                    val msg = when (exception) {
                        is FirebaseAuthInvalidUserException -> "El usuario no existe"
                        is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta"
                        else -> exception?.message ?: "Error desconocido"
                    }
                    onResult(false, msg)
                }
            }
    }


}