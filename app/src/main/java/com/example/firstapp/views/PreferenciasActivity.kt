package com.example.firstapp.views

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
import com.example.firstapp.R
// Importaciones de Firebase Auth y Cloud Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de preferencias del negocio.
 * - Switch: activa o desactiva las alertas de temperatura.
 * - Spinner: sala o ubicación que se monitorea.
 * - Radio: unidad de temperatura.
 * Se sincroniza con /usuarios/{uid} en Firestore y, además, el estado de las alertas
 * se guarda localmente para que el notificador lo lea al instante.
 */
class PreferenciasActivity : AppCompatActivity() {

    // Vistas de la interfaz
    private lateinit var tvUsuarioPreferencias: TextView
    private lateinit var swNotificaciones: Switch
    private lateinit var spSala: Spinner
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
        spSala = findViewById(R.id.spSala)
        rgUnidad = findViewById(R.id.rgUnidad)
        pbGuardando = findViewById(R.id.pbGuardando)
        btnGuardarPreferencia = findViewById(R.id.btnGuardarPreferencia)
        btnVolver = findViewById(R.id.btnVolver)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Muestra primero el estado local de las alertas (sin esperar a la red)
        swNotificaciones.isChecked = getSharedPreferences("IoT_Prefs", MODE_PRIVATE)
            .getBoolean("alertas_activas", true)

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
                        val alertas = prefs["notificaciones"] as? Boolean ?: true
                        val sala = prefs["salaMonitoreada"] as? String
                        val unidad = prefs["unidadTemperatura"] as? String ?: "Celsius"

                        swNotificaciones.isChecked = alertas
                        // Deja el estado local igual al de la nube
                        getSharedPreferences("IoT_Prefs", MODE_PRIVATE).edit()
                            .putBoolean("alertas_activas", alertas).apply()

                        if (unidad.equals("Fahrenheit", ignoreCase = true)) {
                            findViewById<RadioButton>(R.id.rbFahrenheit)?.isChecked = true
                        } else {
                            findViewById<RadioButton>(R.id.rbCelsius)?.isChecked = true
                        }

                        // Selecciona en el Spinner la sala guardada
                        if (sala != null) {
                            val adapter = spSala.adapter
                            for (i in 0 until adapter.count) {
                                if (adapter.getItem(i).toString().equals(sala, ignoreCase = true)) {
                                    spSala.setSelection(i)
                                    break
                                }
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

        val alertas = swNotificaciones.isChecked
        val sala = spSala.selectedItem?.toString() ?: ""
        val idSeleccionado = rgUnidad.checkedRadioButtonId
        val rbSeleccionado = findViewById<RadioButton>(idSeleccionado)
        val unidad = rbSeleccionado?.text?.toString() ?: "Celsius"

        // Guarda el estado de las alertas en local: el notificador lo lee desde aquí
        getSharedPreferences("IoT_Prefs", MODE_PRIVATE).edit()
            .putBoolean("alertas_activas", alertas).apply()

        val prefsMap = mapOf(
            "notificaciones" to alertas,
            "salaMonitoreada" to sala,
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
                    val estado = if (alertas) "alertas activadas" else "alertas desactivadas"
                    Toast.makeText(
                        this,
                        "Preferencias guardadas ($estado, $sala, $unidad)",
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
            Toast.makeText(this, "Preferencias guardadas localmente", Toast.LENGTH_SHORT).show()
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