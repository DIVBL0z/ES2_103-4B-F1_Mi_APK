package com.example.firstapp

// Importación para crear intenciones de navegación entre actividades
import android.content.Intent
// Importación para el manejo del estado guardado de la actividad
import android.os.Bundle
// Importación de la clase View necesaria para el método onPreferenciasClick(view: View)
import android.view.View
// Importación del componente TextView para mostrar el saludo de bienvenida
import android.widget.TextView
// Importación para habilitar el diseño de pantalla completa borde a borde
import androidx.activity.enableEdgeToEdge
// Importación de la clase base compatible AppCompatActivity
import androidx.appcompat.app.AppCompatActivity
// Importación de utilidades para compatibilidad de vistas
import androidx.core.view.ViewCompat
// Importación para gestionar las barras del sistema (insets)
import androidx.core.view.WindowInsetsCompat

// Definición de la clase BienvenidaActivity que hereda de AppCompatActivity
class BienvenidaActivity : AppCompatActivity() {

    // Variable a nivel de clase para almacenar el usuario recibido
    private var usuario: String = ""

    // Método del ciclo de vida que se ejecuta al crearse la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama al método onCreate de la clase padre
        super.onCreate(savedInstanceState)
        // Habilita la visualización de borde a borde en la pantalla
        enableEdgeToEdge()
        // Asocia el archivo de diseño activity_bienvenida.xml con esta actividad
        setContentView(R.layout.activity_bienvenida)

        // Configura el ajuste de márgenes según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainBienvenida)) { v, insets ->
            // Obtiene las dimensiones de las barras del sistema
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplica el relleno para no solapar la interfaz con las barras del sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Retorna las inserciones procesadas
            insets
        }

        // Obtiene el dato del usuario enviado desde MainActivity mediante el Intent
        usuario = intent.getStringExtra("EXTRA_USUARIO") ?: "Usuario"

        // Obtiene la referencia del TextView centrado para la bienvenida
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        // Establece el mensaje de bienvenida con el nombre de usuario
        tvBienvenida.text = "¡Bienvenido,\n$usuario!"
    }

    // Método vinculado al atributo android:onClick="onPreferenciasClick" del botón Preferencias
    fun onPreferenciasClick(view: View) {
        // Crea un Intent explícito para navegar hacia PreferenciasActivity
        val intent = Intent(this, PreferenciasActivity::class.java)
        // Pasa el usuario recibido como dato extra al nuevo Intent
        intent.putExtra("EXTRA_USUARIO", usuario)
        // Inicia la actividad PreferenciasActivity
        startActivity(intent)
    }
}
