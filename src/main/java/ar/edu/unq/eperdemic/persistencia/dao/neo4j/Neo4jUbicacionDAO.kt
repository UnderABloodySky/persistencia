package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import ar.edu.unq.eperdemic.modelo.TipoCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.exception.CaminoNoSoportado
import ar.edu.unq.eperdemic.modelo.exception.UbicacionMuyLejana
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.runner.TransactionNeo4j
import org.neo4j.driver.Values


class Neo4jUbicacionDAO : UbicacionDAO {

    override fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: String) {
        val transaction = TransactionNeo4j.currentTransaction
        val query = """Match(ubicacionUno:Ubicacion {nombre:"$ubicacion1"}),(ubicacionDos:Ubicacion{nombre:"$ubicacion2"}) MERGE (ubicacionUno)-[c:$tipoCamino]->(ubicacionDos)"""
        transaction.run(query)
    }

    override fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        val ubicacion = Ubicacion()
        ubicacion.nombreUbicacion = "TibetDojo"
        val list = listOf(ubicacion)
        return list
    }

    override fun mover(vector: Vector, nombreUbicacion: String) {
        var nombreDeUbicacionV = vector.ubicacion?.nombreUbicacion
        var ubicacionesAledañas = conectados(nombreDeUbicacionV.toString())
        var ubicacionDestino = Ubicacion()
        ubicacionDestino.nombreUbicacion = nombreUbicacion
        var ubicacionAux = ubicacionesAledañas.firstOrNull { u -> u.nombreUbicacion == nombreUbicacion }
        var noPuedeMoverseTanLejos = ubicacionAux == null
        if (noPuedeMoverseTanLejos && noEsCapazDeMoverPorCamino(vector, ubicacionDestino)) {
            throw (lanzarExcepcionAlMover(noPuedeMoverseTanLejos, noEsCapazDeMoverPorCamino(vector, ubicacionDestino)))
        }
    }

    override fun capacidadDeExpansion(vectorId: Long, movimientos: Int): Int {
        val vector = HibernateVectorDAO().recuperar(vectorId)
        val nombreUbicacion = vector.ubicacion!!.nombreUbicacion
        val tipos = vector.tipo.posiblesCaminos.map{it.nombre()}
        val tiposQueryConMovimientos = this.tiposFormateados(tipos, movimientos)
        val transaction = TransactionNeo4j.currentTransaction
        val intQuery =  """
                        MATCH (n:Ubicacion {nombre:"${nombreUbicacion}"})-${tiposQueryConMovimientos} -> (fof) WHERE fof.nombre <> n.nombre RETURN COUNT(DISTINCT fof) AS result
                        """
        val result = transaction.run(intQuery, Values.parameters("nombreUbicacion", nombreUbicacion, "tiposQueryConMovimientos", tiposQueryConMovimientos, "movimientos", movimientos))
        return result.single().get("result").asInt()
        }

    private fun tiposFormateados(tipos : List<String>, movimientos: Int) : String = tipos.toString().toString().replace("[", "[:").replace(",", " |").replace("]", "*0..${movimientos.toString()}]").trim().trim()

    fun esAledaña(nombreDeUbicacion: String, uPosibleAledaña: String) {
        var transaction = TransactionNeo4j.currentTransaction
        var query = """ Match(u1:Ubicacion {nombre:"$nombreDeUbicacion"})-[c]->(u2:Ubicacion {nombre:"$uPosibleAledaña" }) return(c) """

        print("~~~~~~~~~~~~~~~~~~~~~~~~~~~ VAMOS LOS PIBES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        print("~~~~~~~~~~~~~~~~~~~~~~~~~~~ Query: ~~~~~~~~~~~~~~~~~~~~~~~ \n   $query")
        var result = transaction.run(query)
        if (result.list().isEmpty()) {
            throw UbicacionMuyLejana(nombreDeUbicacion,uPosibleAledaña)
        }
    }

    fun noEsCapazDeMoverPorCamino(vector: Vector, ubicacionDestino: String) {
        var transaction = TransactionNeo4j.currentTransaction
        var nombreUbicacionActual = vector.ubicacion?.nombreUbicacion
        var query = """ match(u1:Ubicacion{nombre:"$nombreUbicacionActual"})-[c:Camino ]-> (u2:Ubicacion{nombre:"$ubicacionDestino"}) return(c.nombre)  """
        var result = transaction.run(query).list()
        if ((result.map { i-> vector.tipo.posiblesCaminos.contains(darTipo(i.get("nombre").toString()))}).contains(true) ){
            throw CaminoNoSoportado()
        }
    }

    private fun lanzarExcepcionAlMover(noPuedeMoverseTanLejos: Boolean, noPuedeMoversePorCamino: Boolean): Throwable {
        var excepcion: Exception
        if (noPuedeMoverseTanLejos) {
            excepcion = UbicacionMuyLejana("ne","")
        } else {
            excepcion = CaminoNoSoportado()
        }
        return excepcion
    }

    private fun darTipo(camino: String): TipoCamino? {
        when (camino) {
            "Terrestre" -> return TipoCamino.Terrestre
            "Aereo" -> return TipoCamino.Aereo
            "Maritimo" -> return TipoCamino.Maritimo
            else -> return null
        }
    }

    private fun noEsCapazDeMoverPorCamino(vector: Vector, ubicacionDestino: Ubicacion?): Boolean {
//        val session = TransactionNeo4j.currentSession
        val transaction = TransactionNeo4j.currentTransaction

        var query = """ match(u1:Ubicacion{nombre:"${vector.ubicacion?.nombreUbicacion}"})-[c:Camino]-> (u2:Ubicacion{nombre:"${ubicacionDestino?.nombreUbicacion}"}) return(c.nombre)  """

        var result = transaction.run(query)

        if (result.list().isEmpty()) {
            return true
        } else {
            var camino = result.list().get(0)["(c.nombre)"]

            return vector.tipo.posiblesCaminos().contains(darTipo(camino.toString())).not()

        }
    }

    override fun crear(ubicacion: Ubicacion): Ubicacion {
        return Ubicacion()
    }

    override fun recuperar(nombre: String): Ubicacion {
        return Ubicacion()
    }

    override fun actualizar(ubicacion: Ubicacion) {
        TODO("Not yet implemented")
    }

    override fun agregarVector(vector: Vector, ubicacion: Ubicacion) {
        TODO("Not yet implemented")
    }
}



