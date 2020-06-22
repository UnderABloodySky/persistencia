package ar.edu.unq.eperdemic.persistencia.dao.mongoDB

import ar.edu.unq.eperdemic.modelo.evento.Accion
import ar.edu.unq.eperdemic.modelo.evento.Evento
import ar.edu.unq.eperdemic.modelo.evento.tipoEvento.TipoEvento
import ar.edu.unq.eperdemic.persistencia.dao.FeedDAO
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Indexes

class FeedMongoDAO : GenericMongoDAO<Evento>(Evento::class.java), FeedDAO {

    fun getByTipoPatogeno(tipo: String): Evento? = getBy("tipoPatogeno", tipo)

    fun getByTipoEvento(tipoEvento: TipoEvento): List<Evento?> = findEq("tipoEvento", tipoEvento)

    override fun feedPatogeno(tipoPatogeno : String) : List<Evento>{
        //Me fijo que: Dado un evento, ese evento
        //      ((Sea de Por Pandemia **O** Por Contagio por Primera vez) **Y** (Sea del tipo de patogeno dado))
        val match = Aggregates.match(
                and
                (or (eq("eventos.accion", Accion.PATOGENO_ES_PANDEMIA.name), eq("eventos.accion", Accion.PATOGENO_CONTAGIA_1RA_VEZ_EN_UBICACION.name)),
                     eq("eventos.tipoPatogeno", tipoPatogeno))
        )
//      val sort = Aggregates.sort(Indexes.descending("fecha"))
        return aggregate(listOf(match/*, sort*/), Evento::class.java)
//      return listOf()
    }

    override fun feedVector(tipoPatogeno: String): List<Evento> {
        TODO("Not yet implemented")
    }

    override fun feedUbicacion(tipoPatogeno: String): List<Evento> {
        TODO("Not yet implemented")
    }

}