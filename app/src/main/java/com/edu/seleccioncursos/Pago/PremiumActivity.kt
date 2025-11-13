package com.edu.seleccioncursos.Pago

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edu.seleccioncursos.Principal.MainActivity
import com.edu.seleccioncursos.R

class PremiumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        val btnBuyPremium = findViewById<Button>(R.id.btnBuyPremium)
        btnBuyPremium.setOnClickListener {
            comprarMembresiaPremium()
        }
    }
    private fun comprarMembresiaPremium() {

        Toast.makeText(this, "¡Compra exitosa! Ahora eres un miembro premium.", Toast.LENGTH_SHORT).show()

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isPremiumUser", true)
        editor.apply()

        MainActivity.getInstance()?.ocultarItemPremium()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()

        ocultarItemPremiumEnNavigationView()
    }


    private fun ocultarItemPremiumEnNavigationView() {

        val mainActivity =
            MainActivity.getInstance()
        mainActivity?.ocultarItemPremium()
    }
}