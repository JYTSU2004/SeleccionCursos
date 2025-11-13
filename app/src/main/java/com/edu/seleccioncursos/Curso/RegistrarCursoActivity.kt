package com.edu.seleccioncursos.Curso

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.edu.seleccioncursos.R
import com.google.firebase.database.FirebaseDatabase

class RegistrarCursoActivity : AppCompatActivity() {

    private lateinit var etCurso: EditText
    private lateinit var btnGuardarCurso: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_curso)

        // Configurar los márgenes de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Inicializar elementos de la vista
        etCurso = findViewById(R.id.etCurso)
        btnGuardarCurso = findViewById(R.id.btnGuardarCurso)
        // Configurar el botón para guardar el curso
        btnGuardarCurso.setOnClickListener {
            guardarCurso()
        }
    }
    //Firebase
    private fun guardarCurso() {
        val curso = etCurso.text.toString().trim()
        // Validación de campos
        if (curso.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el nombre del curso", Toast.LENGTH_SHORT).show()
            return
        }
        // Crear el HashMap de datos para Firebase
        val cursoData = hashMapOf(
            "curso" to curso
        )
        // Guardar los datos en Firebase Database
        val ref = FirebaseDatabase.getInstance().getReference("Cursos")
        val cursoId = ref.push().key // Genera una ID única para el curso
        // Guardar los datos en Firebase usando la ID generada
        ref.child(cursoId ?: "").setValue(cursoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Curso registrado con éxito", Toast.LENGTH_SHORT).show()
                etCurso.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar el curso: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}



