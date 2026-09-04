package com.example.firstapp

// Importación para el manejo del estado guardado de la actividad
import android.os.Bundle
// Importación de Intent para navegar entre diferentes pantallas (Activities)
import android.content.Intent
// Importación de métodos de transformación para mostrar u ocultar contraseñas
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
// Importación de Patterns para validación de patrones como direcciones de correo electrónico
import android.util.Patterns
// Importación de la clase View necesaria para los métodos vinculados a onClick(view: View)
import android.view.View
// Importación del componente Button para interactuar con botones
import android.widget.Button
// Importación del componente CheckBox para la casilla de verificación
import android.widget.CheckBox
// Importación del componente EditText para los campos de entrada de texto
import android.widget.EditText
// Importación del componente ImageButton para botones con iconos gráficos
import android.widget.ImageButton
// Importación de Toast para desplegar notificaciones flotantes breves
import android.widget.Toast
// Importación para habilitar el diseño de pantalla completa borde a borde
import androidx.activity.enableEdgeToEdge
// Importación de AppCompatActivity para asegurar compatibilidad entre versiones de Android
import androidx.appcompat.app.AppCompatActivity
// Importación de ViewCompat para manejo compatible de vistas
import androidx.core.view.ViewCompat
// Importación de WindowInsetsCompat para manejar las barras del sistema
import androidx.core.view.WindowInsetsCompat

// Definición de la clase MainActivity que hereda de AppCompatActivity
class MainActivity : AppCompatActivity() {

    // Variable global para acceder al campo de texto de usuario
    private lateinit var edtUsuario: EditText
    // Variable global para acceder al campo de texto de contraseña
    private lateinit var edtPassword: EditText
    // Variable global para acceder al botón de mostrar/ocultar contraseña
    private lateinit var btnMostrarPassword: ImageButton
    // Variable global para acceder a la casilla de verificación de recordarme
    private lateinit var chkRecordarme: CheckBox
    // Variable global para acceder al botón de ingresar
    private lateinit var btnIngresar: Button
    // Variable booleana que almacena el estado de visibilidad de la contraseña
    private var isPasswordVisible: Boolean = false
    // Variable entera a nivel de clase para contabilizar los intentos fallidos, inicializada en 0
    private var intentosFallidos: Int = 0

    // Método del ciclo de vida que se ejecuta al crearse la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama al método onCreate de la clase base
        super.onCreate(savedInstanceState)
        // Habilita la visualización de borde a borde en la pantalla
        enableEdgeToEdge()
        // Asocia el archivo de diseño activity_main.xml a esta actividad
        setContentView(R.layout.activity_main)

        // Configura un listener para ajustar márgenes según las barras de estado y navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Obtiene las dimensiones de las barras del sistema
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplica el relleno a la vista principal para evitar solapamiento
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Retorna las inserciones procesadas
            insets
        }

        // Obtiene la referencia del campo de texto de usuario por su ID
        edtUsuario = findViewById(R.id.edtUsuario)
        // Obtiene la referencia del campo de texto de contraseña por su ID
        edtPassword = findViewById(R.id.edtPassword)
        // Obtiene la referencia del ImageButton para mostrar contraseña por su ID
        btnMostrarPassword = findViewById(R.id.btnMostrarPassword)
        // Obtiene la referencia de la casilla de verificación por su ID
        chkRecordarme = findViewById(R.id.chkRecordarme)
        // Obtiene la referencia del botón ingresar por su ID
        btnIngresar = findViewById(R.id.btnIngresar)

        // Asigna el evento de clic al botón de mostrar u ocultar contraseña
        btnMostrarPassword.setOnClickListener {
            // Invoca la función booleana para alternar la visibilidad de la contraseña
            alternarVisibilidadPassword()
        }

        // Asigna el evento de clic al botón ingresar
        btnIngresar.setOnClickListener {
            // Llama a la función onIngresarClick pasando la vista del botón
            onIngresarClick(it)
        }
    }

    /**
     * Función booleana que muestra u oculta el texto de la contraseña
     * @param mostrar Parámetro booleano opcional; por defecto invierte el valor actual
     * @return Retorna true si la contraseña queda visible, o false si queda oculta
     */
    private fun alternarVisibilidadPassword(mostrar: Boolean = !isPasswordVisible): Boolean {
        // Asigna el nuevo valor booleano a la variable de estado
        isPasswordVisible = mostrar

        // Evalúa la condición booleana para mostrar u ocultar
        if (isPasswordVisible) {
            // Muestra los caracteres de la contraseña en texto plano
            edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            // Cambia el icono al de ocultar contraseña (ojo tachado)
            btnMostrarPassword.setImageResource(R.drawable.ic_visibility_off_24)
        } else {
            // Oculta los caracteres de la contraseña usando puntos
            edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            // Restablece el icono original (ojo normal)
            btnMostrarPassword.setImageResource(R.drawable.ic_visibility_24)
        }

        // Mantiene el cursor ubicado al final del texto ingresado
        edtPassword.setSelection(edtPassword.text.length)

        // Retorna el estado booleano de visibilidad resultante
        return isPasswordVisible
    }

    // Método que se ejecuta al presionar el botón Ingresar mediante android:onClick="onIngresarClick"
    fun onIngresarClick(view: View) {
        // Lee el texto del usuario y elimina espacios en blanco al inicio y final
        val usuario = edtUsuario.text.toString().trim()
        // Lee la contraseña ingresada y elimina espacios en blanco al inicio y final
        val password = edtPassword.text.toString().trim()
        // Obtiene si la casilla recordarme está seleccionada (true) o no (false)
        val recordarme = chkRecordarme.isChecked
        // Variable booleana bandera para indicar si el formulario cumple todas las validaciones
        var esValido = true

        // Valida si el campo de usuario se encuentra vacío
        if (usuario.isEmpty()) {
            // Asigna un mensaje de error directo en el campo de usuario
            edtUsuario.error = "El usuario no puede estar vacío"
            // Marca la validación como no superada
            esValido = false
        // Valida si el formato del correo electrónico no cumple el estándar usando Patterns.EMAIL_ADDRESS
        } else if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            // Asigna un mensaje de error indicando formato de correo electrónico inválido
            edtUsuario.error = "Ingresa un correo electrónico válido"
            // Marca la validación como no superada
            esValido = false
        } else {
            // Elimina cualquier mensaje de error previo si el campo es correcto
            edtUsuario.error = null
        }

        // Valida si el campo de contraseña se encuentra vacío
        if (password.isEmpty()) {
            // Asigna un mensaje de error directo en el campo de contraseña
            edtPassword.error = "La contraseña no puede estar vacía"
            // Marca la validación como no superada
            esValido = false
        // Valida si la longitud de la contraseña es menor a los 6 caracteres requeridos
        } else if (password.length < 6) {
            // Asigna un mensaje de error indicando la cantidad mínima de caracteres
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            // Marca la validación como no superada
            esValido = false
        } else {
            // Elimina cualquier mensaje de error previo si el campo es correcto
            edtPassword.error = null
        }

        // Comprueba si alguna de las validaciones anteriores falló
        if (!esValido) {
            // Suma 1 al contador de intentos fallidos antes de terminar el proceso
            intentosFallidos++
        } else {
            // Reinicia el contador de intentos fallidos al superar exitosamente las validaciones
            intentosFallidos = 0
            // Crea un Intent explícito para navegar hacia la nueva pantalla de bienvenida (BienvenidaActivity)
            val intent = Intent(this, BienvenidaActivity::class.java)
            // Adjunta el nombre del usuario ingresado como dato extra para mostrarlo en la nueva pantalla
            intent.putExtra("EXTRA_USUARIO", usuario)
            // Inicia la nueva actividad mostrando la pantalla de bienvenida
            startActivity(intent)
        }
    }

    // Método que se ejecuta al presionar el botón Limpiar mediante android:onClick="onLimpiarClick"
    fun onLimpiarClick(view: View) {
        // Vacía el campo de texto del usuario
        edtUsuario.text.clear()
        // Vacía el campo de texto de la contraseña
        edtPassword.text.clear()
        // Limpia el mensaje de error del campo de usuario si existía
        edtUsuario.error = null
        // Limpia el mensaje de error del campo de contraseña si existía
        edtPassword.error = null
        // Desmarca la casilla de verificación recordarme
        chkRecordarme.isChecked = false
        // Restablece la visibilidad de la contraseña a oculta mediante la función booleana
        alternarVisibilidadPassword(false)
    }
}