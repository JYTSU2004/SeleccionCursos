package com.edu.seleccioncursos.Profesor

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edu.seleccioncursos.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow

class RegistrarProfesorActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var etNombre: EditText
    private lateinit var etEdad: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var tvUbicacionSeleccionada: TextView
    private lateinit var spinnerCursoRegistrado: Spinner
    private lateinit var imageViewProfesor: ImageView
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var databaseReference: DatabaseReference

    private var geoPointSeleccionado: GeoPoint? = null
    private var imagenUri: Uri? = null
    private val firebaseAuth = FirebaseAuth.getInstance()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_profesor)

        // Configuración de osmdroid
        Configuration.getInstance().load(this, android.preference.PreferenceManager.getDefaultSharedPreferences(this))

        // Inicializar elementos de la vista
        etNombre = findViewById(R.id.etNombre)
        etEdad = findViewById(R.id.etEdad)
        etDescripcion = findViewById(R.id.etDescripcion)
        tvUbicacionSeleccionada = findViewById(R.id.tvUbicacionSeleccionada)
        spinnerCursoRegistrado = findViewById(R.id.spinnerCursoRegistrado)
        mapView = findViewById(R.id.mapView)
        imageViewProfesor = findViewById(R.id.imageViewProfesor)
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)

        // Configuración inicial del MapView
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(GeoPoint(20.0, 0.0))

        // Configurar botón para seleccionar imagen
        btnSeleccionarImagen.setOnClickListener { mostrarOpcionesImagen() }

        // Selección de ubicación en el mapa
        mapView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val geoPoint = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                geoPointSeleccionado = geoPoint
                tvUbicacionSeleccionada.text = "Ubicación seleccionada: ${geoPoint.latitude}, ${geoPoint.longitude}"

                mapView.overlays.clear()
                val marker = Marker(mapView)
                marker.position = geoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Ubicación seleccionada"
                marker.snippet = "${geoPoint.latitude}, ${geoPoint.longitude}"
                marker.infoWindow = BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView)
                mapView.overlays.add(marker)
                mapView.invalidate()
            }
            false
        }

        // Configurar botón para guardar el profesor
        btnGuardar.setOnClickListener { guardarProfesor() }
        databaseReference = FirebaseDatabase.getInstance().getReference("Cursos")
        cargarCursos()
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Tomar Foto", "Seleccionar de la Galería")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Elige una opción")
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> abrirCamara()
                1 -> abrirGaleria()
            }
        }
        builder.show()
    }

    private fun abrirCamara() {
        val valores = ContentValues()
        valores.put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
        valores.put(MediaStore.Images.Media.DESCRIPTION, "De la Cámara")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> imageViewProfesor.setImageURI(imagenUri)
                REQUEST_IMAGE_PICK -> {
                    imagenUri = data?.data
                    imageViewProfesor.setImageURI(imagenUri)
                }
            }
        }
    }

    private fun guardarProfesor() {
        val nombre = etNombre.text.toString().trim()
        val edad = etEdad.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val cursoSeleccionado = spinnerCursoRegistrado.selectedItem?.toString()

        if (nombre.isEmpty() || edad.isEmpty() || descripcion.isEmpty() || geoPointSeleccionado == null || cursoSeleccionado.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val progreso = ProgressDialog(this)
        progreso.setMessage("Guardando profesor...")
        progreso.show()

        val storageRef = FirebaseStorage.getInstance().reference.child("profesores/${firebaseAuth.uid}/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(imagenUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    guardarInfoProfesor(nombre, edad.toInt(), descripcion, cursoSeleccionado, uri.toString())
                    progreso.dismiss()
                }
            }
            .addOnFailureListener { e ->
                progreso.dismiss()
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarInfoProfesor(nombre: String, edad: Int, descripcion: String, curso: String, imagenUrl: String) {
        val profesorData = hashMapOf(
            "nombre" to nombre,
            "edad" to edad,
            "descripcion" to descripcion,
            "curso" to curso,
            "latitud" to geoPointSeleccionado?.latitude,
            "longitud" to geoPointSeleccionado?.longitude,
            "imagenUrl" to imagenUrl
        )

        val ref = FirebaseDatabase.getInstance().getReference("Profesores")
        val profesorId = ref.push().key
        ref.child(profesorId ?: "").setValue(profesorData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profesor registrado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar el profesor: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarCursos() {
        val cursosList = mutableListOf<String>()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cursosList.clear()
                for (cursoSnapshot in snapshot.children) {
                    val cursoNombre = cursoSnapshot.child("curso").getValue(String::class.java)
                    cursoNombre?.let { cursosList.add(it) }
                }
                val adapter = ArrayAdapter(this@RegistrarProfesorActivity, android.R.layout.simple_spinner_item, cursosList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCursoRegistrado.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistrarProfesorActivity, "Error al cargar los cursos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 100
        private const val REQUEST_IMAGE_PICK = 101
    }
}
