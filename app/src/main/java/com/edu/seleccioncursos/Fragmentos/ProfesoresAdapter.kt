package com.edu.seleccioncursos.Fragmentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.edu.seleccioncursos.Modelos.Profesor
import com.edu.seleccioncursos.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ProfesoresAdapter(private val profesoresList: List<Profesor>) :
    RecyclerView.Adapter<ProfesoresAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombreTextView: TextView = view.findViewById(R.id.tvNombreProfesor)
        val edadTextView: TextView = view.findViewById(R.id.tvEdadProfesor)
        val descripcionTextView: TextView = view.findViewById(R.id.tvDescripcionProfesor)
        val ubicacionTextView: TextView = view.findViewById(R.id.tvUbicacionProfesor)
        val mapViewProfesor: MapView = view.findViewById(R.id.mapViewProfesor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profesor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profesor = profesoresList[position]


        // Configurar los TextViews
        holder.nombreTextView.text = profesor.nombre
        holder.edadTextView.text = "Edad: ${profesor.edad}"
        holder.descripcionTextView.text = profesor.descripcion

        if (profesor.latitud != null && profesor.longitud != null) {
            holder.ubicacionTextView.text = "Ubicación: ${profesor.latitud}, ${profesor.longitud}"

            // Configurar el MapView con la ubicación del profesor
            holder.mapViewProfesor.setMultiTouchControls(true)
            val startPoint = GeoPoint(profesor.latitud, profesor.longitud)
            holder.mapViewProfesor.controller.setZoom(15.0)
            holder.mapViewProfesor.controller.setCenter(startPoint)

            val marker = Marker(holder.mapViewProfesor)
            marker.position = startPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = profesor.nombre
            holder.mapViewProfesor.overlays.add(marker)
        } else {
            holder.ubicacionTextView.text = "Ubicación no disponible"
        }
    }


    override fun getItemCount(): Int {
        return profesoresList.size
    }
}