package com.example.firstapp.views

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.R
import com.example.firstapp.models.COLECCION
import com.example.firstapp.utils.NotificadorTemperatura // NUEVO
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idEdicion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        val edtUbicacion = findViewById<EditText>(R.id.edtUbicacion)
        val edtTemperatura = findViewById<EditText>(R.id.edtTemperatura)
        val edtHora = findViewById<EditText>(R.id.edtHora)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        idEdicion = intent.getStringExtra("id")
        if (idEdicion != null) {
            title = "Editar registro"
            edtUbicacion.setText(intent.getStringExtra("ubicacion"))
            edtTemperatura.setText(intent.getDoubleExtra("temperatura", 0.0).toString())
            edtHora.setText(intent.getStringExtra("hora"))
        } else {
            title = "Nuevo registro"
            edtHora.setText(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
        }

        btnGuardar.setOnClickListener {
            val ubicacion = edtUbicacion.text.toString().trim()
            val tempTexto = edtTemperatura.text.toString().trim()
            val hora = edtHora.text.toString().trim()

            // Validación ANTES de tocar Firebase
            var ok = true
            if (ubicacion.isEmpty()) { edtUbicacion.error = "Ingresa la sala o ubicación"; ok = false }
            val temperatura = tempTexto.toDoubleOrNull()
            if (temperatura == null) { edtTemperatura.error = "Ingresa una temperatura válida"; ok = false }
            if (hora.isEmpty()) { edtHora.error = "Ingresa la hora del registro"; ok = false }
            if (!ok) {
                Toast.makeText(this, "Faltan campos por completar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datos = mapOf("ubicacion" to ubicacion, "temperatura" to temperatura!!, "hora" to hora)
            val id = idEdicion
            if (id == null) {
                db.collection(COLECCION).add(datos)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show()
                        NotificadorTemperatura.evaluar(applicationContext, ubicacion, temperatura) // NUEVO
                        finish()
                    }
                    .addOnFailureListener { Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show() }
            } else {
                db.collection(COLECCION).document(id).update(datos)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registro actualizado", Toast.LENGTH_SHORT).show()
                        NotificadorTemperatura.evaluar(applicationContext, ubicacion, temperatura) // NUEVO
                        finish()
                    }
                    .addOnFailureListener { Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }
}