package com.edu.seleccioncursos.Fragmentos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.edu.seleccioncursos.Profesor.RegistrarProfesorActivity
import android.widget.Button
import com.edu.seleccioncursos.Curso.RegistrarCursoActivity
import com.edu.seleccioncursos.Splash.ClosedActivity
import com.edu.seleccioncursos.Principal.Constantes
import com.edu.seleccioncursos.Usuario.EditarPerfil
import com.edu.seleccioncursos.Principal.MainActivity
import com.edu.seleccioncursos.R
import com.edu.seleccioncursos.databinding.FragmentCuentaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class FragmentCuenta : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCuentaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el botón "Profesor" para iniciar RegistrarProfesorActivity
        binding.btnProfesor.setOnClickListener {
            startActivity(Intent(mContext, RegistrarProfesorActivity::class.java))
        }
        binding.btnCursos.setOnClickListener {
            startActivity(Intent(mContext, RegistrarCursoActivity::class.java))
        }
        // Configurar el botón "Cancelar Suscripción"
        val sharedPreferences = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isPremiumUser = sharedPreferences.getBoolean("isPremiumUser", false)

        // Mostrar u ocultar el botón según el estado de la suscripción
        if (isPremiumUser) {
            binding.btnCancelarSuscripcion.visibility = View.VISIBLE
        } else {
            binding.btnCancelarSuscripcion.visibility = View.GONE
        }

        binding.btnCancelarSuscripcion.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        leerInfo()
        verificarMembresia()
        verificarRolUsuario()

        // Generar y mostrar el código QR
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            val qrData = "UserID: $uid"
            val qrBitmap = generateQRCode(qrData)
            binding.qrImageView.setImageBitmap(qrBitmap)
        }

        binding.BtnEditarPerfil.setOnClickListener {
            startActivity(Intent(mContext, EditarPerfil::class.java))
        }

        binding.BtnCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, ClosedActivity::class.java))
            activity?.finishAffinity()
        }
    }

    private fun leerInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    var tiempo = "${snapshot.child("tiempo").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val cod_tel = codTelefono + telefono

                    if (tiempo == "null") {
                        tiempo = "0"
                    }

                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    // Setear información
                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvTelefono.text = cod_tel
                    binding.TvNacimiento.text = f_nac
                    binding.TvMiembro.text = for_tiempo

                    // Cargar la imagen de perfil
                    Glide.with(mContext)
                        .load(imagen)
                        .placeholder(R.drawable.editar)  // Imagen por defecto mientras se carga
                        .into(binding.IvPerfil)  // Asegúrate que binding esté bien inicializado y coincide con el ID

                    if (proveedor == "Email") {
                        val esVerificado = firebaseAuth.currentUser!!.isEmailVerified
                        binding.TvEstadoCuenta.text = if (esVerificado) "Verificado" else "No Verificado"
                    } else {
                        binding.TvEstadoCuenta.text = "Verificado"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error al cargar la información", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun verificarRolUsuario() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("isDeveloper")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isDeveloper = snapshot.getValue(Boolean::class.java) ?: false
                    // Mostrar el botón solo si el usuario tiene el rol de Developer
                    if (isDeveloper) {
                        binding.btnProfesor.visibility = View.VISIBLE
                        binding.btnCursos.visibility = View.VISIBLE
                    } else {
                        binding.btnProfesor.visibility = View.GONE
                        binding.btnCursos.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error al verificar el rol del usuario", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun verificarMembresia() {
        val sharedPreferences = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isPremiumUser = sharedPreferences.getBoolean("isPremiumUser", false)

        if (isPremiumUser) {
            // Actualiza la interfaz para reflejar que el usuario es premium
            binding.TvPremium.text = "Membresía: Premium"
        } else {
            binding.TvPremium.text = "Membresía: Estándar"
        }
    }

    private fun mostrarDialogoConfirmacion() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Cancelar Suscripción")
        builder.setMessage("¿Estás seguro de que deseas cancelar tu suscripción premium?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            cancelarSuscripcion()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun cancelarSuscripcion() {
        // Cambiar el estado de suscripción en SharedPreferences
        val sharedPreferences = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isPremiumUser", false)
        editor.apply()

        // Mostrar un mensaje de cancelación exitosa
        Toast.makeText(mContext, "Suscripción cancelada con éxito.", Toast.LENGTH_SHORT).show()

        // Hacer que el ítem "Premium" vuelva a aparecer en el BottomNavigationView
        (activity as? MainActivity)?.mostrarItemPremium()

        // Ocultar el botón "Cancelar Suscripción"
        binding.btnCancelarSuscripcion.visibility = View.GONE
    }

    // Método para generar el código QR
    private fun generateQRCode(data: String): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}
