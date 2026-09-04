package com.example.firstapp

// Importación para crear intenciones de navegación entre actividades
import android.content.Intent
// Importación para el manejo del estado guardado de la actividad
import android.os.Bundle
// Importación de Handler para programar tareas diferidas en el hilo principal
import android.os.Handler
// Importación de Looper para obtener la cola de mensajes del hilo principal
import android.os.Looper
// Importación de la clase View necesaria para los métodos vinculados a onClick(view: View)
import android.view.View
// Importación del componente Button para interactuar con botones
import android.widget.Button
// Importación del componente ProgressBar para la barra de progreso
import android.widget.ProgressBar
// Importación del componente RadioButton para leer la opción seleccionada
import android.widget.RadioButton
// Importación del componente RadioGroup para gestionar el grupo de opciones
import android.widget.RadioGroup
// Importación del componente TextView para mostrar información en pantalla
import android.widget.TextView
// Importación de Toast para desplegar notificaciones flotantes breves
import android.widget.Toast
// Importación para habilitar el diseño de pantalla completa borde a borde
import androidx.activity.enableEdgeToEdge
// Importación de la clase base compatible AppCompatActivity
import androidx.appcompat.app.AppCompatActivity
// Importación de utilidades para compatibilidad de vistas
import androidx.core.view.ViewCompat
// Importación para gestionar las barras del sistema (insets)
import androidx.core.view.WindowInsetsCompat

// Definición de la clase PreferenciasActivity que hereda de AppCompatActivity
class PreferenciasActivity : AppCompatActivity() {

    // Método del ciclo de vida que se ejecuta al crearse la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama al método onCreate de la clase base
        super.onCreate(savedInstanceState)
        // Habilita la visualización de borde a borde en la pantalla
        enableEdgeToEdge()
        // Asocia el archivo de diseño activity_preferencias.xml con esta actividad
        setContentView(R.layout.activity_preferencias)

        // Configura el ajuste de márgenes según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPreferencias)) { v, insets ->
            // Obtiene las dimensiones de las barras del sistema
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplica el relleno a la vista principal para no solapar el contenido
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Retorna las inserciones procesadas
            insets
        }

        // Obtiene el usuario transferido desde BienvenidaActivity a través del Intent
        val usuario = intent.getStringExtra("EXTRA_USUARIO") ?: "Usuario"

        // Obtiene la referencia del TextView que muestra el usuario
        val tvUsuarioPreferencias = findViewById<TextView>(R.id.tvUsuarioPreferencias)
        // Muestra el correo o usuario actual en el TextView
        tvUsuarioPreferencias.text = "Configuración para: $usuario"

        // Obtiene la referencia del botón para volver
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        // Asigna el evento de clic al botón volver
        btnVolver.setOnClickListener {
            // Finaliza esta actividad y regresa a BienvenidaActivity
            finish()
        }
    }

    // Método vinculado al atributo android:onClick="onGuardarPreferenciaClick" del botón Guardar
    fun onGuardarPreferenciaClick(view: View) {
        // Obtiene la referencia a la barra de progreso pbGuardando por su ID
        val pbGuardando = findViewById<ProgressBar>(R.id.pbGuardando)
        // Muestra la barra de progreso en pantalla haciéndola visible
        pbGuardando.visibility = View.VISIBLE

        // Obtiene la referencia al grupo de botones de radio por su ID
        val rgUnidad = findViewById<RadioGroup>(R.id.rgUnidad)
        // Obtiene el identificador del RadioButton que se encuentra seleccionado
        val idSeleccionado = rgUnidad.checkedRadioButtonId
        // Busca el RadioButton correspondiente al ID seleccionado
        val rbSeleccionado = findViewById<RadioButton>(idSeleccionado)
        // Extrae el texto de la unidad elegida ('Celsius' o 'Fahrenheit')
        val unidad = rbSeleccionado?.text?.toString() ?: "Celsius"

        // Programa la ejecución diferida de una tarea en el hilo principal tras 1000 milisegundos (1 segundo)
        Handler(Looper.getMainLooper()).postDelayed({
            // Oculta la barra de progreso transcurrido 1 segundo
            pbGuardando.visibility = View.GONE
            // Despliega una notificación Toast confirmando la unidad seleccionada
            Toast.makeText(this, "Unidad guardada: $unidad", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    // Método vinculado al atributo android:onClick="onCerrarSesionClick" del botón Cerrar sesión
    fun onCerrarSesionClick(view: View) {
        // Crea una intención para navegar a la pantalla de login (MainActivity)
        val intent = Intent(this, MainActivity::class.java)
        // Configura las banderas para limpiar la pila de tareas y crear una nueva
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // Inicia la actividad MainActivity dejando la pantalla de login limpia
        startActivity(intent)
        // Finaliza la actividad actual
        finish()
    }
}
