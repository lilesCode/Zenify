package com.sarajimenez.zenify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnRegister : Button
    private lateinit var btnLastRegister : Button
    private lateinit var editUser : EditText
    private lateinit var editPassword : EditText
    private lateinit var editRepeatPassword : EditText
    private lateinit var editUsername : EditText
    private lateinit var auth : FirebaseAuth  //para autenticarme en firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()  //referenciamos los componentes.
        start() //comienza todo.
    }

    private fun init(){
        btnRegister = findViewById(R.id.btn_register_in_register)
        btnLastRegister = findViewById(R.id.btn_last_register)
        editUser = findViewById(R.id.edit_user_register)
        editPassword = findViewById(R.id.edit_pass_register)
        editRepeatPassword = findViewById(R.id.pass_register_repeat_in_register)
        editUsername = findViewById(R.id.edit_username_register)
        
        auth = Firebase.auth  //Creamos nuestro objeto de autenticación

    }


    private fun start() {
        btnRegister.setOnClickListener{
            val username = editUsername.text.toString()
            val email = editUser.text.toString()
            val pass = editPassword.text.toString()
            val repeatPass = editRepeatPassword.text.toString()
            if (pass != repeatPass
                || username.isEmpty()
                || email.isEmpty()
                || pass.isEmpty()
                || repeatPass.isEmpty())
                Toast.makeText(this, "Campos vacíos y/o contraseñas diferentes", Toast.LENGTH_LONG).show()
            else{
                //todo correcto, vamos a verificar el registro.
                /*
                    Supongo que this, hará referencia al objeto Button, no al Activity.
                     Cuando creamos una lambda para una llamada de órden superior, el contexto
                     no tiene porqué ser el Activity. Hay que curarse en salud. registerUser, es un
                     método mío, no predefinido como el caso del .setOnClickListener de un Buttom, que
                     en este caso, el this hace referencia al activity, no al buttom.
                     Cuando creemos una lambda sin parámetros, cuidado con el this porque no es el Activity.
                */
                registerUser(username, email, pass) { result, msg ->
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                    if (result) startActivityLogin()
                }
            }
        }

        btnLastRegister.setOnClickListener{
            // Como la lambda tiene un parámetro view, el this es el Activity
                view->
            val intent = Intent (this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun startActivityLogin() {
        //Tengo que lanzar un intent con el Activity a loguear.
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() //No quiero que sigua el Activity del registro.

    }

    //Método para registrar usuario en Fireauth y en Firestore
    private fun registerUser(username: String, email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                    //Si la creación del usuario es exitosa, almacenamos al usuario en la base de datos Firestore
                    if (profileTask.isSuccessful) {
                        val db = Firebase.firestore
                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(user.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                user.sendEmailVerification().addOnCompleteListener { verificationTask ->
                                    val msg = if (verificationTask.isSuccessful) {
                                        "Usuario registrado. Verifique su correo."
                                    } else {
                                        "Usuario registrado. No se pudo enviar el correo de verificación."
                                    }
                                    auth.signOut()
                                    onResult(true, msg)
                                }
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Error al guardar usuario: ${e.message}")
                            }
                    } else {
                        onResult(false, "Error al actualizar el perfil: ${profileTask.exception?.message}")
                    }
                }
            } else {
                handleRegistrationError(task.exception, onResult)
            }
        }
    }



    private fun handleRegistrationError(exception: Exception?, onResult: (Boolean, String) -> Unit) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> onResult(false, "Ese usuario ya existe")
            is FirebaseAuthWeakPasswordException -> onResult(false, "Contraseña débil: ${exception.reason}")
            is FirebaseAuthInvalidCredentialsException -> onResult(false, "El email proporcionado no es válido")
            else -> onResult(false, exception?.message ?: "Error desconocido")
        }
    }
}