package ar.edu.unq.eperdemic.utils.mongoDB

import ar.edu.unq.eperdemic.estado.Infectado
import ar.edu.unq.eperdemic.estado.Sano
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.evento.Accion
import ar.edu.unq.eperdemic.modelo.evento.Evento
import ar.edu.unq.eperdemic.modelo.evento.EventoFactory
import ar.edu.unq.eperdemic.modelo.evento.tipoEvento.Contagio
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.FeedMongoDAO
import ar.edu.unq.eperdemic.services.*
import ar.edu.unq.eperdemic.services.impl.FeedServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.services.FeedService
import ar.edu.unq.eperdemic.tipo.Animal
import ar.edu.unq.eperdemic.tipo.Humano
import ar.edu.unq.eperdemic.tipo.Insecto
import ar.edu.unq.eperdemic.utils.DataService
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FeedServiceTest {
    lateinit var dao : FeedMongoDAO
    lateinit var feedService : FeedService
    lateinit var ubicacionService : UbicacionService
    lateinit var patogenoService : PatogenoService
    lateinit var vectorService : VectorService
    lateinit var hibernateData : DataService

    @Before
    fun setUp(){
        dao = FeedMongoDAO()
        feedService = FeedServiceImpl(dao)
        val ubicacionDAO = HibernateUbicacionDAO()
        ubicacionService = UbicacionServiceImpl(ubicacionDAO)
        patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO(), HibernateEspecieDAO())
        vectorService = VectorServiceImpl(HibernateVectorDAO(), ubicacionDAO)
        hibernateData = HibernateDataService()
    }

    @Test
    fun alpedirLosEventosDeContagioDeUnPatogenoNoCreadoDevuelveUnaListaVacia(){
        val result = feedService.feedPatogeno("sarasa")
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun alBuscarLosEventosDeMutacionDeUnPatogenoTieneUnResultadoCuandoSeCreaUnaEspecieDelPatogeno(){
        val patogenoModel = Patogeno()
        patogenoModel.tipo = "virus"
        val especie = patogenoService.agregarEspecie(patogenoService.crearPatogeno(patogenoModel), "gripe", "Narnia")
        val result = feedService.feedPatogeno(patogenoModel.tipo)
        val eventosDeCreacion = result.filter { it.accionQueLoDesencadena == Accion.ESPECIE_CREADA.name }
        val unicoEventoCreacion = eventosDeCreacion.get(0)
        Assert.assertEquals(1, result.size)
        Assert.assertTrue(unicoEventoCreacion is Evento)
        Assert.assertEquals(Accion.ESPECIE_CREADA.name, unicoEventoCreacion.accionQueLoDesencadena)
    }

    @Test
    fun alBuscarLosEventosDeContagioDeUnPatogenoTieneCuatroResultadosCuandoElPatogenoSoloSeVolvioPandemiaUnaUnicaVez(){
        //Una especie se vuelve pandemia se encuentra presenta en mas de la mitad de las locaciones
        val jamaica = ubicacionService.crearUbicacion("Jamaica")
        val babilonia = ubicacionService.crearUbicacion("Babilonia")
        ubicacionService.crearUbicacion("NismanLandia")
        val patogenoModel = Patogeno()
        patogenoModel.tipo = "virus"
        val especie = patogenoService.agregarEspecie(patogenoService.crearPatogeno(patogenoModel), "gripe", "Narnia")
        val vectorJamaiquino = Vector()
        vectorJamaiquino.ubicacion = jamaica
        vectorJamaiquino.tipo = Humano()
        val vectorBabilonico = Vector()
        vectorBabilonico.ubicacion = babilonia
        vectorBabilonico.tipo= Humano()
        vectorService.crearVector(vectorJamaiquino)
        vectorService.crearVector(vectorBabilonico)
        vectorService.infectar(vectorJamaiquino, especie)
        vectorService.infectar(vectorBabilonico, especie)
        val result = feedService.feedPatogeno(patogenoModel.tipo )
        val eventosPandemia = result.filter{it.accionQueLoDesencadena == Accion.PATOGENO_ES_PANDEMIA.name}
        val unicoEventoPandemia = eventosPandemia.get(0)
        Assert.assertEquals(4, result.size)
        Assert.assertEquals(1, eventosPandemia.size)
        Assert.assertTrue(unicoEventoPandemia is Evento)
        Assert.assertTrue(patogenoService.esPandemia(especie.id!!))
    }


    @Test
    fun vectorInfectadoMueveAUbicacionDondeHayVectoresLosInfectaYSeDisparaEvento(){
        var patogeno = Patogeno()
        patogeno.tipo = "virus"
        patogeno.factorContagioHumano= 1000
        var especie1 = Especie()
        especie1.cantidadInfectadosParaADN = 42
        especie1.nombre = "soyUnaEspecie"
        especie1.paisDeOrigen = "Masachuset"
        especie1.patogeno = patogeno
        var vector= Vector()
        vector.agregarEspecie(especie1)
        vector.tipo=Humano()
        vector.estado= Infectado()
        var vector1= Vector()
        vector1.tipo=Humano()
        vector1.estado= Sano()
        var ubicacionCreada = ubicacionService.crearUbicacion("Florencio Varela")
        vector.ubicacion=ubicacionCreada
        vectorService.crearVector(vector)
        var vectorCreado=vectorService.recuperarVector(1)
        Assert.assertEquals(vectorCreado.ubicacion?.nombreUbicacion,"Florencio Varela")
        vector1.ubicacion= ubicacionService.crearUbicacion("Quilmes")
        vectorService.crearVector(vector1)
        ubicacionService.conectar("Florencio Varela", "Quilmes", "Terrestre")
        ubicacionService.conectar("Quilmes", "Florencio Varela", "Terrestre")
        ubicacionService.mover(1,"Quilmes")
        val result = feedService.feedUbicacion("Quilmes")
        Assert.assertEquals(3, result.size)
        Assert.assertEquals("ARRIBO", result.get(0).accionQueLoDesencadena)
    }
    @Test
    fun ubicacionRecibeUnArriboYSeLanzaUnEvento() {
        var vector1= Vector()
        vector1.tipo=Humano()
        vector1.estado= Sano()
        vector1.ubicacion= ubicacionService.crearUbicacion("Florencio Varela")
        vectorService.crearVector(vector1)
        ubicacionService.crearUbicacion("Quilmes")
        ubicacionService.conectar("Florencio Varela","Quilmes","Terrestre")
        ubicacionService.mover(vector1.id?.toInt()!!,"Quilmes")
        val result = feedService.feedUbicacion("Quilmes")
        Assert.assertEquals(1, result.size)
        Assert.assertEquals(result.get(0).ubicacionDestino,"Quilmes")
    }
    @Test
    fun ubicacionRecibeDosArribos_SeLanzanDosEvento_ElUltimoQueSeLanzaEsElPrimero () {
        var vector1= Vector()
        vector1.tipo=Humano()
        vector1.estado= Sano()
        vector1.ubicacion= ubicacionService.crearUbicacion("Florencio Varela")
        var vector2= Vector()
        vector2.tipo=Humano()
        vector2.estado= Sano()
        vector2.ubicacion= ubicacionService.recuperarUbicacion("Florencio Varela")
        vectorService.crearVector(vector1)
        vectorService.crearVector(vector2)
        ubicacionService.crearUbicacion("Quilmes")
        ubicacionService.conectar("Florencio Varela","Quilmes","Terrestre")
        ubicacionService.mover(vector1.id?.toInt()!!,"Quilmes")
        ubicacionService.mover(vector2.id?.toInt()!!,"Quilmes")
        val result = feedService.feedUbicacion("Quilmes")
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(2L,result.get(0).idVectorQueSeMueve)
    }

    @Test
    fun `feedVector de un vector recien creado retorna una lista vacia`() {
        val vectorNuevo = Vector()
        vectorNuevo.estado = Sano()
        vectorNuevo.tipo = Humano()
        vectorNuevo.ubicacion = ubicacionService.crearUbicacion("Jamaica")
        vectorService.crearVector(vectorNuevo)

        val listaDeEventos = feedService.feedVector(vectorNuevo.id!!)
        Assert.assertTrue(listaDeEventos.isEmpty())
        Assert.assertEquals(0, listaDeEventos.size)
    }

    @Test
    fun `mover un vector a una ubicacion sin vectores genera 1 evento de arribo feedVector`() {
        val vector = Vector()
        vector.tipo = Animal()
        vector.estado = Sano()
        vector.ubicacion = ubicacionService.crearUbicacion("Jamaica")
        vectorService.crearVector(vector)

        val fiyi = ubicacionService.crearUbicacion("Fiyi")
        ubicacionService.conectar("Jamaica", "Fiyi", "Aereo")
        ubicacionService.mover(vector.id!!.toInt(), "Fiyi")

        val eventos = feedService.feedVector(vector.id!!)
        Assert.assertEquals(1, eventos.size)
        Assert.assertEquals(Accion.ARRIBO.name, eventos.first().accionQueLoDesencadena)
    }

    @Test
    fun `mover un vector sano a una ubicacion con vectores genera 1 evento de arribo feedVector`() {
        val vector = Vector()
        vector.tipo = Animal()
        vector.estado = Sano()
        vector.ubicacion = ubicacionService.crearUbicacion("Jamaica")
        vectorService.crearVector(vector)

        val fiyi = ubicacionService.crearUbicacion("Fiyi")
        ubicacionService.conectar("Jamaica", "Fiyi", "Aereo")

        val vector1 = Vector()
        vector1.ubicacion = fiyi
        vector1.estado = Infectado()
        vector1.tipo = Insecto()
        val vector2 = Vector()
        vector2.ubicacion = fiyi
        vector2.estado = Sano()
        vector2.tipo = Humano()
        vectorService.crearVector(vector1)
        vectorService.crearVector(vector2)
        ubicacionService.mover(vector.id!!.toInt(), "Fiyi")

        val eventos = feedService.feedVector(vector.id!!)
        Assert.assertEquals(1, eventos.size)
        Assert.assertEquals(Accion.ARRIBO.name, eventos.first().accionQueLoDesencadena)
    }

    @Test
    fun `mover un vector infectado a una ubicacion con un vector sano genera 3 eventos(2 arribos y 1 contagio feedVector)`() {
        val patogeno = Patogeno()
        patogeno.tipo = "virus"
        patogeno.factorContagioHumano= 1000
        patogenoService.crearPatogeno(patogeno)
        val especie = patogenoService.agregarEspecie(patogeno.id!!, "varicela", "brasil")

        val vectorInfectado = Vector()
        vectorInfectado.estado = Infectado()
        vectorInfectado.tipo = Animal()
        vectorInfectado.agregarEspecie(especie)
        vectorInfectado.ubicacion = ubicacionService.crearUbicacion("Jamaica")
        vectorService.crearVector(vectorInfectado)

        val kongo = ubicacionService.crearUbicacion("Kongo")
        ubicacionService.conectar("Jamaica", "Kongo", "Maritimo")
        val vectorSano = Vector()
        vectorSano.estado = Sano()
        vectorSano.tipo = Humano()
        vectorSano.ubicacion = kongo
        vectorService.crearVector(vectorSano)
        ubicacionService.mover(vectorInfectado.id!!.toInt(), kongo.nombreUbicacion)
        val eventosDelQueInfecta = feedService.feedVector(vectorInfectado.id!!)
        Assert.assertEquals(2, eventosDelQueInfecta.size)
        var accionesDelFeed = eventosDelQueInfecta.map{ it.accionQueLoDesencadena }
        Assert.assertTrue(accionesDelFeed.containsAll(listOf(Accion.ARRIBO.name, Accion.CONTAGIO_NORMAL.name)))

        val eventosDelQueEsInfectado = feedService.feedVector(vectorSano.id!!)
        Assert.assertEquals(1, eventosDelQueEsInfectado.size)
        accionesDelFeed = eventosDelQueEsInfectado.map{ it.accionQueLoDesencadena }
        Assert.assertTrue(accionesDelFeed.contains(Accion.CONTAGIO_NORMAL.name))
    }

    @After
    fun dropAll() {
        MegalodonService().eliminarTodo()
     }
}