package com.edu.seleccioncursos.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.edu.seleccioncursos.Opciones_login.Login_email
import com.edu.seleccioncursos.R

class CorreoSplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correo_splash)

        // Mostrar la animación durante 3 segundos antes de cerrar la actividad
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login_email::class.java)
            startActivity(intent)
            finish()
        }, 4000)
    }
}
