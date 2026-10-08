package com.example.firstapp.views

// Importaciones para navegación y ciclo de vida de actividades
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
// Importaciones de compatibilidad de Android
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firstapp.R
import com.example.firstapp.models.Usuario
// Importaciones de Firebase Authentication y Cloud Firestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de registro de nuevos usuarios en el sistema.
 * Realiza una doble operación:
 * 1. Registra las credenciales en Firebase Authentication.
 * 2. Guarda el perfil completo y metadatos en Cloud Firestore (colección 'usuarios/{uid}').
 */
class RegistroActivity : AppCompatActivity() {

    // Instancias de Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Vistas de la interfaz
    private lateinit var edtNombre: EditText
    private lateinit var edtRut: EditText
    private lateinit var edtTelefono: EditText
    private lateinit var spRol: Spinner
    private lateinit var edtCorreo: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmarPassword: EditText
    private lateinit var pbCargando: ProgressBar
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolverLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        // Ajuste para compatibilidad con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRegistro)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de servicios Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Vinculación de vistas
        edtNombre = findViewById(R.id.edtRegistroNombre)
        edtRut = findViewById(R.id.edtRegistroRut)
        edtTelefono = findViewById(R.id.edtRegistroTelefono)
        spRol = findViewById(R.id.spRegistroRol)
        edtCorreo = findViewById(R.id.edtRegistroCorreo)
        edtPassword = findViewById(R.id.edtRegistroPassword)
        edtConfirmarPassword = findViewById(R.id.edtRegistroConfirmarPassword)
        pbCargando = findViewById(R.id.pbRegistroCargando)
        btnRegistrar = findViewById(R.id.btnConfirmarRegistro)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)

        // Eventos de botones
        btnRegistrar.setOnClickListener {
            ejecutarRegistro()
        }

        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * Valida los campos ingresados y ejecuta el flujo de registro dual: Auth + Firestore.
     */
    private fun ejecutarRegistro() {
        val nombre = edtNombre.text.toString().trim()
        val rut = edtRut.text.toString().trim()
        val telefono = edtTelefono.text.toString().trim()
        val rolSeleccionado = spRol.selectedItem?.toString() ?: "Usuario"
        val correo = edtCorreo.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val confirmarPassword = edtConfirmarPassword.text.toString().trim()

        var esValido = true

        // Validación de Nombre
        if (nombre.isEmpty()) {
            edtNombre.error = "El nombre no puede estar vacío"
            esValido = false
        } else if (nombre.length < 2) {
            edtNombre.error = "Ingresa al menos 2 caracteres"
            esValido = false
        } else {
            edtNombre.error = null
        }

        // Validación de Correo
        if (correo.isEmpty()) {
            edtCorreo.error = "El correo es obligatorio"
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreo.error = "Ingresa un correo electrónico válido"
            esValido = false
        } else {
            edtCorreo.error = null
        }

        // Validación de Contraseña
        if (password.isEmpty()) {
            edtPassword.error = "La contraseña es obligatoria"
            esValido = false
        } else if (password.length < 6) {
            edtPassword.error = "Debe tener al menos 6 caracteres"
            esValido = false
        } else {
            edtPassword.error = null
        }

        // Validación de Confirmación de Contraseña
        if (confirmarPassword != password) {
            edtConfirmarPassword.error = "Las contraseñas no coinciden"
            esValido = false
        } else {
            edtConfirmarPassword.error = null
        }

        if (!esValido) return

        // Normalización del rol para coherencia en base de datos
        val rolNormalizado = when (rolSeleccionado.lowercase()) {
            "administrador" -> "administrador"
            "técnico", "tecnico" -> "tecnico"
            else -> "usuario"
        }

        setCargando(true)

        // Paso 1: Crear credenciales en Firebase Authentication
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""

                    // Paso 2: Crear el objeto de perfil para Cloud Firestore
                    val nuevoUsuario = Usuario(
                        uid = uid,
                        correo = correo,
                        nombreCompleto = nombre,
                        rut = rut,
                        telefono = telefono,
                        rol = rolNormalizado,
                        fechaRegistro = Timestamp.now(),
                        activo = true,
                        preferencias = mapOf(
                            "notificaciones" to true,
                            "idioma" to "Español",
                            "unidadTemperatura" to "Celsius"
                        )
                    )

                    // Paso 3: Guardar el documento en Firestore en /usuarios/{uid}
                    db.collection("usuarios").document(uid)
                        .set(nuevoUsuario.toMap())
                        .addOnSuccessListener {
                            setCargando(false)
                            Toast.makeText(
                                this,
                                "¡Usuario y perfil creados con éxito!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navegar hacia BienvenidaActivity limpiando la pila
                            val intent = Intent(this, BienvenidaActivity::class.java)
                            intent.putExtra("EXTRA_USUARIO", correo)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { errorFirestore ->
                            setCargando(false)
                            Toast.makeText(
                                this,
                                "Usuario creado en Auth, pero ocurrió un error en Firestore: ${errorFirestore.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    setCargando(false)
                    val exception = task.exception
                    val mensaje = when (exception) {
                        is FirebaseAuthUserCollisionException ->
                            "Este correo ya está registrado en Firebase. Inicia sesión directamente."
                        is FirebaseAuthWeakPasswordException ->
                            "La contraseña es demasiado débil (mínimo 6 caracteres)."
                        else ->
                            "Error de registro: ${exception?.localizedMessage ?: "Verifique su conexión"}"
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Alterna la disponibilidad de los controles de la pantalla durante peticiones de red.
     */
    private fun setCargando(cargando: Boolean) {
        pbCargando.visibility = if (cargando) View.VISIBLE else View.GONE
        btnRegistrar.isEnabled = !cargando
        btnVolverLogin.isEnabled = !cargando
        edtNombre.isEnabled = !cargando
        edtRut.isEnabled = !cargando
        edtTelefono.isEnabled = !cargando
        spRol.isEnabled = !cargando
        edtCorreo.isEnabled = !cargando
        edtPassword.isEnabled = !cargando
        edtConfirmarPassword.isEnabled = !cargando
    }
}
