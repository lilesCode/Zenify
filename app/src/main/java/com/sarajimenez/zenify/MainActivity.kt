package com.sarajimenez.zenify

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sarajimenez.zenify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Limpio la cache local
        FirebaseFirestore.getInstance().clearPersistence();



        //Variables de los componentes gráficos
        val navigationView = findViewById<NavigationView>(R.id.myNavView)
        val headerView = navigationView.getHeaderView(0)  // Obtiene la vista del nav_header.xml
        val emailTextView = headerView.findViewById<TextView>(R.id.txt_email)
        val userTextView = headerView.findViewById<TextView>(R.id.txt_name)

        //Variables del Firebase
        val sharedPreferences = getSharedPreferences(LoginActivity.Global.preferencias_compartidas, Context.MODE_PRIVATE)
        //val nombreUsuario = sharedPreferences.getString("NombreUsuario", "Usuario")
        val email = FirebaseAuth.getInstance().currentUser?.email

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
                        userTextView.text = username

                        // Guardar en SharedPreferences para futuros accesos rápidos
                        val sharedPreferences = getSharedPreferences(LoginActivity.Global.preferencias_compartidas, Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("NombreUsuario", username)
                        editor.apply()
                    } else {
                        Log.d("MainActivity", "No se encontró el documento del usuario")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error obteniendo el username", e)
                }
        }

        val nombreUsuario = sharedPreferences.getString("NombreUsuario", "Usuario")

        //Setteamos el email y el nombre de usuario
        emailTextView.text = email
        userTextView.text = nombreUsuario

        //NAV
        // Configura el NavController desde el fragmento de host
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHost.navController

        // Configura el Navigation View y el DrawerLayout
        val navView = binding.myNavView

        // Configura AppBarConfiguration con los destinos principales
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.fragmentHome, R.id.fragmentConf, R.id.fragmentTasks, R.id.fragmentTimer),
            binding.main
        )

        // Configura la ActionBar con el NavController y AppBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configura el Navigation View para usar con el NavController
        navView.setupWithNavController(navController)

        // Maneja la selección de los ítems del Drawer
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fragmentHome -> {
                    // Navega al fragmento Home
                    navController.navigate(R.id.fragmentHome)
                }

                R.id.fragmentConf -> {
                    // Navega al fragmento de Configuración
                    navController.navigate(R.id.fragmentConf)
                }

                R.id.fragmentTasks -> {
                    // Navega al fragmento de Precipitaciones
                    navController.navigate(R.id.fragmentTasks)
                }
                R.id.fragmentTimer -> {
                    // Navega al fragmento de Precipitaciones
                    navController.navigate(R.id.fragmentTimer)
                }
                //Logout
                R.id.logout -> {
                    // Lógica de logout
                    logout()
                }
            }

            // Cierra el Drawer después de la navegación
            binding.main.closeDrawer(GravityCompat.START)
            true
        }
    }


    override fun onSupportNavigateUp(): Boolean{
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_op, menu)
        return true
    }


    //Navegación del menú de opciones.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.fragmentHome -> {
                navController.navigate(R.id.fragmentHome)
                true
            }
            R.id.fragmentConf -> {
                navController.navigate(R.id.fragmentConf)
                true
            }
            R.id.fragmentTasks -> {
                navController.navigate(R.id.fragmentTasks)
                true
            }
            R.id.fragmentTimer -> { // Navegación al temporizador
                navController.navigate(R.id.fragmentTimer)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Método para hacer logout que se redirige al login
    private fun logout() {
        // Eliminar datos de SharedPreferences (si los tienes)
        val sharedPreferences = getSharedPreferences(LoginActivity.Global.preferencias_compartidas, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("Correo")
        editor.remove("Proveedor") // Eliminar otros datos guardados si es necesario
        editor.apply()

        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut()

        // Redirigir al LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()  // Terminar MainActivity para que no se quede en la pila de actividades
    }



}