package com.edu.seleccioncursos.Usuario

import android.app.Activity
import android.app.ProgressDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.edu.seleccioncursos.Fragmentos.FragmentCuenta
import com.edu.seleccioncursos.R
import com.edu.seleccioncursos.databinding.ActivityEditarPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class EditarPerfil : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarInfo()

        // Mostrar el DatePickerDialog al tocar el campo de fecha de nacimiento
        binding.EtFNac.setOnClickListener {
            mostrarDatePickerDialog()
        }
        binding.FABCambiarImg.setOnClickListener {
            selec_imagen_de()
        }
        binding.BtnActualizar.setOnClickListener {
            if (imagenUri != null) {

            } else {
                actualizarDatos()
            }
        }


    }

    private fun mostrarDatePickerDialog() {
        // Obtenemos la fecha actual
        val calendario = Calendar.getInstance()
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        // Creamos y mostramos el DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Actualizamos el EditText con la fecha seleccionada
                val fechaSeleccionada = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.EtFNac.setText(fechaSeleccionada)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun actualizarDatos() {
        val nombres = binding.EtNombres.text.toString().trim()
        val fechaNacimiento = binding.EtFNac.text.toString().trim()
        val telefono = binding.EtTelefono.text.toString().trim()
        val codigoTelefono = binding.selectorCod.selectedCountryCodeWithPlus

        // Verificar que los campos no estén vacíos (opcional)
        if (nombres.isEmpty()) {
            binding.EtNombres.error = "Por favor ingresa tu nombre"
            binding.EtNombres.requestFocus()
            return
        }

        // Crear un HashMap con los datos actualizados
        val hashMap = HashMap<String, Any>()
        hashMap["nombres"] = nombres
        hashMap["fecha_nac"] = fechaNacimiento
        hashMap["telefono"] = telefono
        hashMap["codigoTelefono"] = codigoTelefono

        // Referencia a la base de datos
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                // Éxito al actualizar los datos
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al actualizar los datos
                Toast.makeText(this, "No se pudieron actualizar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codtelefono = "${snapshot.child("codigoTelefono").value}"

                    // Setear los valores en los campos
                    binding.EtNombres.setText(nombres)
                    binding.EtFNac.setText(f_nac)
                    binding.EtTelefono.setText(telefono)

                    try {
                        val codigo = codtelefono.replace("+", "").toInt()
                        binding.selectorCod.setCountryForPhoneCode(codigo)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Cargar la imagen si está disponible
                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.editar)
                            .into(binding.editar)
                    } catch (e: Exception) {
                        Toast.makeText(this@EditarPerfil, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditarPerfil, "Error al cargar la información: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun subirImagenStorage(){
        progressDialog.setMessage("Subiendo imagen...")
        progressDialog.show()

        val rutaImagen = "ImagenesPerfil/" + firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.putFile(imagenUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagenCargada = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    actualizarImgenDB(urlImagenCargada)
                }
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo subir la imagen debido a ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun actualizarImgenDB(urlImagenCargada: String) {
        progressDialog.setMessage("Actualizando imagen")
        progressDialog.show()

        val hashMap : HashMap<String, Any> = HashMap()
        if (imagenUri != null){
            hashMap["urlImagenPerfil"] = urlImagenCargada
        }

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo actualizar la imagen debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selec_imagen_de() {
        val popupMenu = PopupMenu(this, binding.FABCambiarImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galeria")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                //Cámara
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    concederPermisoCamara.launch(arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            } else if (itemId == 2){
                //Galería
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imagenGaleria()
                } else {
                    concederPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private val concederPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
            var concedidoTodos = true
            for (seConcede in resultado.values) {
                concedidoTodos = concedidoTodos && seConcede
            }

            if (concedidoTodos) {
                imageCamara()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de la cámara o almacenamiento ha sido denegado, o ambas fueron denegadas",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    private fun imageCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripon_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
               subirImagenStorage()
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }

        }

    private val concederPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) {
                imagenGaleria()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento ha sido denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }
    private fun cambiarAFragmentCuenta() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FragmentCuenta, FragmentCuenta())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data

                subirImagenStorage()

            }else{
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }

        }
}

