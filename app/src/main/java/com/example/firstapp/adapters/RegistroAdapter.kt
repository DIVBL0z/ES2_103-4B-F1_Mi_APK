package com.example.firstapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.models.RegistroTemperatura

class RegistroAdapter(
    private val onEditar: (RegistroTemperatura) -> Unit,
    private val onEliminar: (RegistroTemperatura) -> Unit
) : RecyclerView.Adapter<RegistroAdapter.VH>() {

    private var items: List<RegistroTemperatura> = emptyList()

    fun actualizar(nuevos: List<RegistroTemperatura>) {
        items = nuevos
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvUbicacion: TextView = v.findViewById(R.id.tvUbicacion)
        val tvTemperatura: TextView = v.findViewById(R.id.tvTemperatura)
        val tvHora: TextView = v.findViewById(R.id.tvHora)
        val btnEliminar: ImageButton = v.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_registro, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val r = items[position]
        h.tvUbicacion.text = "Sala: ${r.ubicacion}"
        h.tvTemperatura.text = "Temperatura: ${r.temperatura} °C"
        h.tvHora.text = "Hora del registro: ${r.hora}"
        h.itemView.setOnClickListener { onEditar(r) }
        h.btnEliminar.setOnClickListener { onEliminar(r) }
    }
}