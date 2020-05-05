package ar.edu.unq.eperdemic.tipo

import ar.edu.unq.eperdemic.modelo.Especie

class Insecto() : TipoVector() {
    override fun puedeSerContagiadoPor(tipo : TipoVector): Boolean = true //tipo.esHumano() || tipo.esAnimal()

    override fun esInsecto() = true

    override fun factorContagio(especie : Especie): Int = 2//especie.factorContagioInsecto()
}