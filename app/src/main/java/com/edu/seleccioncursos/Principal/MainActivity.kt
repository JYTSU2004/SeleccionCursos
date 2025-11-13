package com.edu.seleccioncursos.Principal

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.edu.seleccioncursos.Fragmentos.FragmentChats
import com.edu.seleccioncursos.Fragmentos.FragmentCuenta
import com.edu.seleccioncursos.Fragmentos.FragmentInicio
import com.edu.seleccioncursos.Fragmentos.FragmentMisAnuncios
import com.edu.seleccioncursos.Fragmentos.FragmentPremium
import com.edu.seleccioncursos.Opciones_login.Login_email
import com.edu.seleccioncursos.R
import com.edu.seleccioncursos.databinding.NavigationviewBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: NavigationviewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private var instance: MainActivity? = null

        fun getInstance(): MainActivity? {
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this // Inicializa la instancia
        binding = NavigationviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Verifica si el usuario está autenticado, si no lo está, redirige al Login
        if (firebaseAuth.currentUser == null) {
            // El usuario no está autenticado, redirigir a la pantalla de inicio de sesión
            startActivity(Intent(this, Login_email::class.java))
            finishAffinity()
            return // Salir del método para evitar continuar con la inicialización de MainActivity
        }

        // Si el usuario está autenticado, continuar con la configuración de la actividad principal
        verFragmentInicio()
        verificarCorreo()


        // Inicializa sharedPreferences antes de usarlo
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val imageButtonToggleTheme: ImageButton = findViewById(R.id.imageButtonToggleTheme)
        imageButtonToggleTheme.setOnClickListener {
            toggleTheme(imageButtonToggleTheme)
        }

        val isNightModeEnabled = sharedPreferences.getBoolean("NightMode", false)

        // Establecer el ícono inicial según el modo actual
        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            imageButtonToggleTheme.setImageResource(R.drawable.ic_night)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            imageButtonToggleTheme.setImageResource(R.drawable.ic_sun)
        }

        val isPremiumUser = sharedPreferences.getBoolean("isPremiumUser", false)
        if (isPremiumUser) {
            ocultarItemPremium()
        } else {
            mostrarItemPremium()
        }

        // Verificar el estado del correo electrónico
        verificarCorreoVerificado()

        binding.BottomNV.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Item_Inicio -> {
                    verFragmentInicio()
                    true
                }
                R.id.Item_Chats -> {
                    verFragmentChats()
                    true
                }
                R.id.Item_Mis_Anuncios -> {
                    verFragmenteMisAnuncios()
                    true
                }
                R.id.Item_Cuenta -> {
                    verFragmentCuenta()
                    true
                }
                R.id.Item_Premium -> {
                    verFragmentPremium()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun verificarCorreo() {
        // Obtener el usuario actualmente autenticado
        val user: FirebaseUser? = firebaseAuth.currentUser

        if (user != null) {
            // Comprobar si el correo electrónico del usuario está verificado
            if (!user.isEmailVerified) {
                // Si no está verificado, mostrar mensaje y enviar correo de verificación
                Toast.makeText(this, "Por favor, verifica tu correo", Toast.LENGTH_LONG).show()

                // Llamar a la función para enviar el correo de verificación
                enviarCorreoVerificacion(user)
            } else {
                // Si está verificado, no hacer nada o continuar con el flujo de la app
            }
        }
    }

    private fun enviarCorreoVerificacion(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Mostrar mensaje de éxito
                    Toast.makeText(this, "Correo de verificación enviado a ${user.email}", Toast.LENGTH_LONG).show()
                } else {
                    // Mostrar mensaje de error
                    Toast.makeText(this, "Error al enviar el correo de verificación", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun verificarCorreoVerificado() {
        val usuario = firebaseAuth.currentUser
        usuario?.reload()?.addOnCompleteListener {
            if (usuario.isEmailVerified) {
            } else {
                Toast.makeText(this, "Correo no verificado. Por favor, verifíquelo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleTheme(imageButton: ImageButton) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            imageButton.setImageResource(R.drawable.ic_sun)
            saveThemePreference(false)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            imageButton.setImageResource(R.drawable.ic_night)
            saveThemePreference(true)
        }
    }

    private fun saveThemePreference(isNightMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("NightMode", isNightMode)
        editor.apply()
    }

    private fun verFragmentInicio() {
        binding.TituloRl.text = "Inicio"
        val fragment = FragmentInicio()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmentTransaction.commit()
    }

    private fun verFragmentChats() {
        binding.TituloRl.text = "Chats"
        val fragment = FragmentChats()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmentTransaction.commit()
    }

    private fun verFragmenteMisAnuncios() {
        binding.TituloRl.text = "Mis anuncios"
        val fragment = FragmentMisAnuncios()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentL1.id, fragment, "FragmentMisAnuncios")
        fragmentTransaction.commit()
    }

    private fun verFragmentCuenta() {
        binding.TituloRl.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransaction.commit()
    }

    private fun verFragmentPremium() {
        binding.TituloRl.text = "Premium"
        val fragment = FragmentPremium()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentL1.id, fragment, "FragmentPremium")
        fragmentTransaction.commit()
    }

    private fun signOutGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener {
            // Cerrar la sesión en Firebase
            firebaseAuth.signOut()
            startActivity(Intent(this, Login_email::class.java))
            finishAffinity()
        }
    }

    fun ocultarItemPremium() {
        val menu = binding.BottomNV.menu
        menu.findItem(R.id.Item_Premium)?.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null // Limpia la instancia cuando la actividad se destruye
    }

    fun mostrarItemPremium() {
        val menu = binding.BottomNV.menu
        menu.findItem(R.id.Item_Premium)?.isVisible = true
    }
}
