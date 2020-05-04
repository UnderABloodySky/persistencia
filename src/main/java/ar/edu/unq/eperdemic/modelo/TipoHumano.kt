package ar.edu.unq.eperdemic.modelo

class TipoHumano(vector: Vector) : TipoVector(vector) {
    override fun puedeSerContagiadoPor(unTipo : TipoVector) = true

    override fun esHumano() = true

    override fun factorContagio(especie : Especie): Int = 2//especie.factorContagioHumano()

    override fun agregarInfectado(especie: Especie) {
        //especie.agregarInfectado()
    }
}