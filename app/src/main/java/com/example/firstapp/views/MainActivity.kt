package com.example.firstapp.views

// Importación para crear intenciones de navegación entre actividades
import android.content.Intent
// Importación para el manejo del estado guardado de la actividad
import android.os.Bundle
// Importación de métodos de transformación para mostrar u ocultar contraseñas
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
// Importación de Patterns para validación de patrones como direcciones de correo electrónico
import android.util.Patterns
// Importación de la clase View necesaria para los métodos vinculados a onClick(view: View)
import android.view.View
// Importación de componentes de la interfaz de usuario
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
// Importaciones de compatibilidad y diseño moderno de Android
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firstapp.R
// Importaciones del SDK oficial de Firebase Authentication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * Actividad principal de inicio de sesión y registro de usuarios,
 * integrada con el servicio backend de Firebase Authentication.
 */
class MainActivity : AppCompatActivity() {

    // Instancia principal del servicio de Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Variable global para acceder al campo de texto de usuario/correo
    private lateinit var edtUsuario: EditText
    // Variable global para acceder al campo de texto de contraseña
    private lateinit var edtPassword: EditText
    // Variable global para acceder al botón de mostrar/ocultar contraseña
    private lateinit var btnMostrarPassword: ImageButton
    // Variable global para acceder a la casilla de verificación de recordarme
    private lateinit var chkRecordarme: CheckBox
    // Variable global para acceder al botón de ingresar
    private lateinit var btnIngresar: Button
    // Variable global para acceder al botón de registrarse con Firebase
    private lateinit var btnRegistrar: Button
    // Variable global para acceder al botón de limpiar
    private lateinit var btnLimpiar: Button
    // Variable global para acceder al indicador de carga circular
    private lateinit var pbCargando: ProgressBar

    // Variable booleana que almacena el estado de visibilidad de la contraseña
    private var isPasswordVisible: Boolean = false
    // Variable entera para contabilizar los intentos fallidos de autenticación
    private var intentosFallidos: Int = 0

    // Constante para el nombre de las preferencias compartidas
    private val PREFS_NAME = "IoT_Prefs"
    private val KEY_RECORDARME = "recordarme"
    private val KEY_EMAIL_GUARDADO = "email_guardado"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita la visualización de borde a borde en la pantalla
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configura un listener para ajustar márgenes según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de la instancia de Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Vinculación de los elementos de la interfaz por sus IDs
        edtUsuario = findViewById(R.id.edtUsuario)
        edtPassword = findViewById(R.id.edtPassword)
        btnMostrarPassword = findViewById(R.id.btnMostrarPassword)
        chkRecordarme = findViewById(R.id.chkRecordarme)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        pbCargando = findViewById(R.id.pbCargando)

        // Asigna el evento de clic al botón de mostrar u ocultar contraseña
        btnMostrarPassword.setOnClickListener {
            alternarVisibilidadPassword()
        }

        // Asigna los eventos de clic a los botones de acción
        btnIngresar.setOnClickListener {
            onIngresarClick(it)
        }

        btnRegistrar.setOnClickListener {
            onRegistrarClick(it)
        }

        btnLimpiar.setOnClickListener {
            onLimpiarClick(it)
        }
    }

    /**
     * Al iniciar la actividad se verifica si existe una sesión activa y si el usuario
     * tenía seleccionada la opción de recordar sesión.
     */
    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val recordarme = prefs.getBoolean(KEY_RECORDARME, false)
        val currentUser = auth.currentUser

        if (currentUser != null && recordarme) {
            // Usuario con sesión activa en Firebase y opción de recordarme activada
            irABienvenida(currentUser.email ?: "Usuario")
        } else if (recordarme) {
            // Prellenar campo con el último correo recordado
            val emailGuardado = prefs.getString(KEY_EMAIL_GUARDADO, "")
            if (!emailGuardado.isNullOrEmpty()) {
                edtUsuario.setText(emailGuardado)
                chkRecordarme.isChecked = true
            }
        }
    }

    /**
     * Alterna la visibilidad del texto de la contraseña entre texto plano y caracteres ocultos.
     */
    private fun alternarVisibilidadPassword(mostrar: Boolean = !isPasswordVisible): Boolean {
        isPasswordVisible = mostrar

        if (isPasswordVisible) {
            edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            btnMostrarPassword.setImageResource(R.drawable.ic_visibility_off_24)
        } else {
            edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            btnMostrarPassword.setImageResource(R.drawable.ic_visibility_24)
        }

        edtPassword.setSelection(edtPassword.text.length)
        return isPasswordVisible
    }

    /**
     * Valida los campos de correo y contraseña devolviendo true si cumplen con los requisitos.
     */
    private fun validarCampos(usuario: String, password: String): Boolean {
        var esValido = true

        if (usuario.isEmpty()) {
            edtUsuario.error = "El correo no puede estar vacío"
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            edtUsuario.error = "Ingresa un correo electrónico válido (ej: usuario@correo.com)"
            esValido = false
        } else {
            edtUsuario.error = null
        }

        if (password.isEmpty()) {
            edtPassword.error = "La contraseña no puede estar vacía"
            esValido = false
        } else if (password.length < 6) {
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            esValido = false
        } else {
            edtPassword.error = null
        }

        return esValido
    }

    /**
     * Habilita o deshabilita los controles del formulario y conmuta la visibilidad
     * de la barra de progreso mientras se procesa la petición en Firebase.
     */
    private fun setCargando(cargando: Boolean) {
        pbCargando.visibility = if (cargando) View.VISIBLE else View.GONE
        btnIngresar.isEnabled = !cargando
        btnRegistrar.isEnabled = !cargando
        btnLimpiar.isEnabled = !cargando
        edtUsuario.isEnabled = !cargando
        edtPassword.isEnabled = !cargando
        chkRecordarme.isEnabled = !cargando
    }

    /**
     * Inicia sesión con Firebase Authentication utilizando correo y contraseña.
     */
    fun onIngresarClick(view: View) {
        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val recordarme = chkRecordarme.isChecked

        if (!validarCampos(usuario, password)) {
            intentosFallidos++
            return
        }

        setCargando(true)

        // Llamada asíncrona a Firebase Auth para iniciar sesión
        auth.signInWithEmailAndPassword(usuario, password)
            .addOnCompleteListener(this) { task ->
                setCargando(false)
                if (task.isSuccessful) {
                    intentosFallidos = 0
                    guardarPreferenciaRecordarme(recordarme, usuario)

                    val user = auth.currentUser
                    Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                    irABienvenida(user?.email ?: usuario)
                } else {
                    intentosFallidos++
                    val exception = task.exception
                    val mensaje = when (exception) {
                        is FirebaseAuthInvalidUserException -> "No existe una cuenta registrada con este correo"
                        is FirebaseAuthInvalidCredentialsException -> "Contraseña o credenciales incorrectas"
                        else -> "Error de autenticación: ${exception?.localizedMessage ?: "Verifique su conexión"}"
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Abre la pantalla de RegistroActivity para crear una cuenta con perfil completo en Firestore.
     */
    fun onRegistrarClick(view: View) {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }

    /**
     * Almacena o limpia la preferencia de 'recordarme' en SharedPreferences.
     */
    private fun guardarPreferenciaRecordarme(recordarme: Boolean, correo: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_RECORDARME, recordarme)
            putString(KEY_EMAIL_GUARDADO, if (recordarme) correo else "")
            apply()
        }
    }

    /**
     * Navega a la pantalla BienvenidaActivity transfiriendo el correo como extra.
     */
    private fun irABienvenida(correo: String) {
        val intent = Intent(this, BienvenidaActivity::class.java)
        intent.putExtra("EXTRA_USUARIO", correo)
        startActivity(intent)
    }

    /**
     * Limpia los campos de texto, mensajes de error y desmarca la casilla de recordarme.
     */
    fun onLimpiarClick(view: View) {
        edtUsuario.text.clear()
        edtPassword.text.clear()
        edtUsuario.error = null
        edtPassword.error = null
        chkRecordarme.isChecked = false
        alternarVisibilidadPassword(false)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}