package ar.edu.unq.eperdemic.tipo

import ar.edu.unq.eperdemic.modelo.Especie

class Animal() : TipoVector() {
    override fun puedeSerContagiadoPor(tipo : TipoVector) = tipo.esInsecto()
    override fun esAnimal() = true

    override fun factorContagio(especie : Especie): Int = especie.factorContagioAnimal()
}