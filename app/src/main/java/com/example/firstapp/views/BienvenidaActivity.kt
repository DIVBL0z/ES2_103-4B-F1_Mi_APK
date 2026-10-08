package com.example.firstapp.views

// Importaciones para crear intenciones de navegación y ciclo de vida
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
// Importaciones de compatibilidad y diseño
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firstapp.R
// Importaciones de Firebase Auth y Cloud Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de bienvenida que consulta el perfil del usuario autenticado en Cloud Firestore
 * y muestra su nombre real, correo, rol y datos de contacto de manera sincronizada.
 */
class BienvenidaActivity : AppCompatActivity() {

    // Vistas de la interfaz
    private lateinit var tvBienvenida: TextView
    private lateinit var tvRol: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvDetallePerfil: TextView
    private lateinit var pbCargandoPerfil: ProgressBar
    private lateinit var btnPreferencias: Button

    // Variables de estado
    private var usuarioExtra: String = ""
    private var nombreMostrar: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        // Configura el ajuste de márgenes según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainBienvenida)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtiene el identificador básico enviado por el Intent
        usuarioExtra = intent.getStringExtra("EXTRA_USUARIO") ?: "Usuario"

        // Vinculación de vistas
        tvBienvenida = findViewById(R.id.tvBienvenida)
        tvRol = findViewById(R.id.tvRol)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvDetallePerfil = findViewById(R.id.tvDetallePerfil)
        pbCargandoPerfil = findViewById(R.id.pbCargandoPerfil)
        btnPreferencias = findViewById(R.id.btnPreferencias)

        // Valores iniciales mientras carga Firestore
        tvBienvenida.text = "¡Bienvenido,\n$usuarioExtra!"
        tvCorreo.text = "Correo: $usuarioExtra"

        // Carga los datos del perfil desde Cloud Firestore
        cargarPerfilFirestore()
    }

    /**
     * Consulta el documento /usuarios/{uid} en Cloud Firestore para obtener el perfil completo.
     */
    private fun cargarPerfilFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            tvRol.text = "Rol: Usuario"
            return
        }

        pbCargandoPerfil.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                pbCargandoPerfil.visibility = View.GONE
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("nombreCompleto") ?: usuarioExtra
                    val rol = doc.getString("rol") ?: "usuario"
                    val correo = doc.getString("correo") ?: currentUser.email ?: usuarioExtra
                    val rut = doc.getString("rut") ?: ""
                    val telefono = doc.getString("telefono") ?: ""

                    nombreMostrar = nombre
                    tvBienvenida.text = "¡Bienvenido,\n$nombre!"
                    tvRol.text = "Rol: ${rol.replaceFirstChar { it.uppercase() }}"
                    tvCorreo.text = "Correo: $correo"

                    val detalles = mutableListOf<String>()
                    if (rut.isNotEmpty()) detalles.add("RUT: $rut")
                    if (telefono.isNotEmpty()) detalles.add("Tel: $telefono")
                    tvDetallePerfil.text = detalles.joinToString("  |  ")
                } else {
                    tvRol.text = "Rol: Usuario"
                }
            }
            .addOnFailureListener {
                pbCargandoPerfil.visibility = View.GONE
                tvRol.text = "Rol: Usuario (Modo local)"
            }
    }

    /**
     * Abre la pantalla de preferencias pasando el nombre o correo del usuario.
     */
    fun onPreferenciasClick(view: View) {
        val intent = Intent(this, PreferenciasActivity::class.java)
        intent.putExtra("EXTRA_USUARIO", if (nombreMostrar.isNotEmpty()) nombreMostrar else usuarioExtra)
        startActivity(intent)

    }

    /**
     * Abre los registros de temperatura
     */

    fun onRegistrosClick(view: View) {
        startActivity(Intent(this, ListaActivity::class.java))
    }
}
