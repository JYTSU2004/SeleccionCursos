package com.edu.seleccioncursos.Fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.edu.seleccioncursos.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView

class FragmentInicio : Fragment() {

    private lateinit var spinnerCursos: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var layoutProfesores: LinearLayout
    private val cursosList = mutableListOf<String>()
    private val mapViews = mutableListOf<MapView>() // Lista para almacenar los MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        spinnerCursos = view.findViewById(R.id.spinnerCursos)
        btnBuscar = view.findViewById(R.id.btnBuscar)
        layoutProfesores = view.findViewById(R.id.layoutProfesores) // Contenedor para mostrar profesores

        // Inicializar configuración de OSMDroid
        Configuration.getInstance().load(context, context?.getSharedPreferences("osmdroid", 0))

        // Cargar cursos desde Firebase y configurar el Spinner
        cargarCursos()

        // Configurar el botón de búsqueda
        btnBuscar.setOnClickListener {
            val cursoSeleccionado = spinnerCursos.selectedItem.toString()
            if (cursoSeleccionado == "Seleccione el curso en el que necesite ayuda" ||
                cursoSeleccionado == "Ningún curso seleccionado") {
                Toast.makeText(requireContext(), "Por favor, seleccione un curso válido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Buscando: $cursoSeleccionado", Toast.LENGTH_SHORT).show()
                buscarProfesoresPorCurso(cursoSeleccionado)
            }
        }


        return view
    }

    // Función para cargar los cursos desde Firebase y configurar el Spinner
    private fun cargarCursos() {
        val ref = FirebaseDatabase.getInstance().getReference("Cursos")

        // Agrega una opción predeterminada en la lista
        cursosList.add("Seleccione el curso en el que necesite ayuda")

        // Escuchar los cambios en Firebase y actualizar el Spinner
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cursosList.clear() // Limpiar la lista antes de cargar nuevos datos

                for (cursoSnapshot in snapshot.children) {
                    val curso = cursoSnapshot.child("curso").getValue(String::class.java)
                    curso?.let { cursosList.add(it) }
                }
                // Configurar el adaptador para el Spinner con los cursos
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cursosList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCursos.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar los cursos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarProfesoresPorCurso(curso: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Profesores")

        // Limpiar el layout de profesores antes de mostrar nuevos resultados
        layoutProfesores.removeAllViews()

        // Buscar profesores que tengan el curso seleccionado
        ref.orderByChild("curso").equalTo(curso).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (profesorSnapshot in snapshot.children) {
                        val nombre = profesorSnapshot.child("nombre").getValue(String::class.java) ?: "Nombre no disponible"
                        val edad = profesorSnapshot.child("edad").getValue(Int::class.java) ?: 0
                        val descripcion = profesorSnapshot.child("descripcion").getValue(String::class.java) ?: "Sin descripción"
                        val latitud = profesorSnapshot.child("latitud").getValue(Double::class.java) ?: 0.0
                        val longitud = profesorSnapshot.child("longitud").getValue(Double::class.java) ?: 0.0

                        // Crear una vista para mostrar la información del profesor
                        val profesorView = LayoutInflater.from(context).inflate(R.layout.item_profesor, layoutProfesores, false)
                        profesorView.findViewById<TextView>(R.id.tvNombreProfesor).text = "Nombre: $nombre"
                        profesorView.findViewById<TextView>(R.id.tvEdadProfesor).text = "Edad: $edad"
                        profesorView.findViewById<TextView>(R.id.tvDescripcionProfesor).text = "Descripción: $descripcion"

                        // Configurar el MapView para mostrar la ubicación del profesor
                        //val mapView = profesorView.findViewById<MapView>(R.id.mapViewProfesor)
                        //mapView.setMultiTouchControls(true)
                        //val startPoint = GeoPoint(latitud, longitud)
                        //mapView.controller.setZoom(15.0)
                        //mapView.controller.setCenter(startPoint)
                        //
                        // Agregar un marcador en la ubicación del profesor
                        //val marker = Marker(mapView)
                        //marker.position = startPoint
                        //marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        //marker.title = nombre
                        //mapView.overlays.add(marker)

                        // Agregar la vista al layout de profesores
                        layoutProfesores.addView(profesorView)
                    }
                } else {
                    Toast.makeText(requireContext(), "No se encontraron profesores para este curso", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al buscar profesores: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Métodos para el ciclo de vida del MapView
    override fun onResume() {
        super.onResume()
        layoutProfesores.forEach { mapView ->
            if (mapView is MapView) mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        layoutProfesores.forEach { mapView ->
            if (mapView is MapView) mapView.onPause()
        }
    }

    override fun onStart() {
        super.onStart()
        layoutProfesores.forEach { mapView ->
            if (mapView is MapView) mapView.onResume()
        }
    }

    override fun onStop() {
        super.onStop()
        layoutProfesores.forEach { mapView ->
            if (mapView is MapView) mapView.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        layoutProfesores.forEach { mapView ->
            if (mapView is MapView) mapView.onDetach()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        layoutProfesores.forEach { mapView ->
            if (mapView is MapView) mapView.onResume()
        }
    }
}