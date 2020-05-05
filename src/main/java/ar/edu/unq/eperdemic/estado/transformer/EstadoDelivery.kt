package ar.edu.unq.eperdemic.estado.transformer

import ar.edu.unq.eperdemic.estado.EstadoVector
import ar.edu.unq.eperdemic.estado.Infectado
import ar.edu.unq.eperdemic.estado.Sano
import ar.edu.unq.eperdemic.modelo.exception.ClaveRepetidaDeEstadoException
import ar.edu.unq.eperdemic.modelo.exception.EstadoNoEncontradoException

class EstadoDelivery {

    private var estados = mutableMapOf<String, EstadoVector>()

    init {
        estados.put("sano", Sano())
        estados.put("infectado", Infectado())
    }

    fun agregarEstado(nuevoEstado: EstadoVector) {
        val nombre = this.format(nuevoEstado.nombre())
        this.ifConditionThrow(this.estaEnLaLista(nombre), ClaveRepetidaDeEstadoException(nombre), { estados.put(nombre, nuevoEstado) })!!
    }

    fun estado(unNombre: String): EstadoVector? {
        return this.ifConditionThrow(!this.estaEnLaLista(unNombre), EstadoNoEncontradoException(unNombre), { estados.get(this.format(unNombre)) })!!
    }

    private fun estaEnLaLista(unNombre: String) = estados.keys.map{this.format(it)}.contains(this.format(unNombre))

    private fun <T> ifConditionThrow(condition: Boolean, e: Exception, bloque: () -> T): T {
        if (condition) {
            throw e
        }
        return bloque()
    }

    private fun format(unNombre : String) = unNombre.toLowerCase()
}