package com.example.firstapp.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.adapters.RegistroAdapter
import com.example.firstapp.models.COLECCION
import com.example.firstapp.models.RegistroTemperatura
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ListaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private val pedirPermisoNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var listener: ListenerRegistration? = null
    private lateinit var adapter: RegistroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        adapter = RegistroAdapter(
            onEditar = { r ->
                startActivity(Intent(this, FormularioActivity::class.java).apply {
                    putExtra("id", r.id)
                    putExtra("ubicacion", r.ubicacion)
                    putExtra("temperatura", r.temperatura)
                    putExtra("hora", r.hora)
                })
            },
            onEliminar = { confirmarEliminar(it) }
        )
        findViewById<RecyclerView>(R.id.rvRegistros).apply {
            layoutManager = LinearLayoutManager(this@ListaActivity)
            adapter = this@ListaActivity.adapter
        }
        findViewById<FloatingActionButton>(R.id.fabAgregar).setOnClickListener {
            startActivity(Intent(this, FormularioActivity::class.java))
        }

        // NUEVO: pedir permiso de notificaciones (Android 13 o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Escucha en tiempo real: se activa al ver la pantalla y se libera al salir
    override fun onStart() {
        super.onStart()
        listener = db.collection(COLECCION)
            .orderBy("hora", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                adapter.actualizar(snap?.toObjects(RegistroTemperatura::class.java) ?: emptyList())
            }
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
    }

    private fun confirmarEliminar(r: RegistroTemperatura) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Eliminar el registro de ${r.ubicacion} (${r.temperatura} °C)?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection(COLECCION).document(r.id!!).delete()
                    .addOnSuccessListener { Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show() }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}