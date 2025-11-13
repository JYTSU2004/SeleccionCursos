package com.edu.seleccioncursos.Modelos

data class Profesor(
    val nombre: String = "",
    val edad: Int = 0,
    val descripcion: String = "",
    val curso: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val imagenUrl: String = ""
)

