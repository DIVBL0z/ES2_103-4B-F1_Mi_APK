package com.example.firstapp

// Importaciones para navegación y ciclo de vida de actividades
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
// Importaciones de compatibilidad y diseño
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// Importaciones de Firebase Auth y Cloud Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de configuración y preferencias del usuario.
 * Sincroniza las preferencias del usuario (notificaciones, idioma, unidad de temperatura)
 * bidireccionalmente con su documento en Cloud Firestore (/usuarios/{uid}).
 */
class PreferenciasActivity : AppCompatActivity() {

    // Vistas de la interfaz
    private lateinit var tvUsuarioPreferencias: TextView
    private lateinit var swNotificaciones: Switch
    private lateinit var spIdioma: Spinner
    private lateinit var rgUnidad: RadioGroup
    private lateinit var pbGuardando: ProgressBar
    private lateinit var btnGuardarPreferencia: Button
    private lateinit var btnVolver: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferencias)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPreferencias)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val usuario = intent.getStringExtra("EXTRA_USUARIO") ?: "Usuario"

        tvUsuarioPreferencias = findViewById(R.id.tvUsuarioPreferencias)
        tvUsuarioPreferencias.text = "Configuración para: $usuario"

        swNotificaciones = findViewById(R.id.swNotificaciones)
        spIdioma = findViewById(R.id.spIdioma)
        rgUnidad = findViewById(R.id.rgUnidad)
        pbGuardando = findViewById(R.id.pbGuardando)
        btnGuardarPreferencia = findViewById(R.id.btnGuardarPreferencia)
        btnVolver = findViewById(R.id.btnVolver)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnVolver.setOnClickListener {
            finish()
        }

        // Carga las preferencias guardadas desde Cloud Firestore
        cargarPreferenciasFirestore()
    }

    /**
     * Lee las preferencias actuales del usuario en Cloud Firestore y las refleja en la UI.
     */
    private fun cargarPreferenciasFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val prefs = doc.get("preferencias") as? Map<String, Any>
                    if (prefs != null) {
                        val notif = prefs["notificaciones"] as? Boolean ?: true
                        val idioma = prefs["idioma"] as? String ?: "Español"
                        val unidad = prefs["unidadTemperatura"] as? String ?: "Celsius"

                        swNotificaciones.isChecked = notif

                        if (unidad.equals("Fahrenheit", ignoreCase = true)) {
                            findViewById<RadioButton>(R.id.rbFahrenheit)?.isChecked = true
                        } else {
                            findViewById<RadioButton>(R.id.rbCelsius)?.isChecked = true
                        }

                        val adapter = spIdioma.adapter
                        for (i in 0 until adapter.count) {
                            if (adapter.getItem(i).toString().equals(idioma, ignoreCase = true)) {
                                spIdioma.setSelection(i)
                                break
                            }
                        }
                    }
                }
            }
    }

    /**
     * Guarda las preferencias seleccionadas en Cloud Firestore (/usuarios/{uid}).
     */
    fun onGuardarPreferenciaClick(view: View) {
        pbGuardando.visibility = View.VISIBLE
        btnGuardarPreferencia.isEnabled = false

        val notificaciones = swNotificaciones.isChecked
        val idioma = spIdioma.selectedItem?.toString() ?: "Español"
        val idSeleccionado = rgUnidad.checkedRadioButtonId
        val rbSeleccionado = findViewById<RadioButton>(idSeleccionado)
        val unidad = rbSeleccionado?.text?.toString() ?: "Celsius"

        val prefsMap = mapOf(
            "notificaciones" to notificaciones,
            "idioma" to idioma,
            "unidadTemperatura" to unidad
        )

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(currentUser.uid)
                .update("preferencias", prefsMap)
                .addOnSuccessListener {
                    pbGuardando.visibility = View.GONE
                    btnGuardarPreferencia.isEnabled = true
                    Toast.makeText(
                        this,
                        "Preferencias guardadas en la nube ($unidad, $idioma)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    pbGuardando.visibility = View.GONE
                    btnGuardarPreferencia.isEnabled = true
                    Toast.makeText(
                        this,
                        "Error al guardar preferencias: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            pbGuardando.visibility = View.GONE
            btnGuardarPreferencia.isEnabled = true
            Toast.makeText(this, "Unidad guardada localmente: $unidad", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cierra la sesión en Firebase Authentication y redirige a MainActivity.
     */
    fun onCerrarSesionClick(view: View) {
        FirebaseAuth.getInstance().signOut()

        val prefs = getSharedPreferences("IoT_Prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("recordarme", false).apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
