package com.edu.seleccioncursos.Usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edu.seleccioncursos.R
import com.edu.seleccioncursos.Splash.CorreoSplashActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPassActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pass)

        val txtmail: TextView = findViewById(R.id.et_email)
        val btnResetRequest: Button = findViewById(R.id.btn_reset_request)

        firebaseAuth = FirebaseAuth.getInstance()

        btnResetRequest.setOnClickListener {
            val email = txtmail.text.toString().trim()
            if (email.isEmpty()) {
                // Mostrar mensaje de error si el campo está vacío
                Toast.makeText(this, "Complete el campo", Toast.LENGTH_SHORT).show()
            } else {
                // Iniciar la actividad para mostrar la animación de envío de correo
                val intent = Intent(this, CorreoSplashActivity::class.java)
                startActivity(intent)

                // Enviar el correo de restablecimiento
                sendPasswordReset(email)
            }
        }
    }

    private fun sendPasswordReset(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Error al completar el proceso", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
