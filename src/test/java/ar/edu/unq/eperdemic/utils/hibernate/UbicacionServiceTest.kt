package ar.edu.unq.eperdemic.utils.hibernate

import ar.edu.unq.eperdemic.estado.Sano
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.exception.MoverUnVectorQueNoEstaCreado
import ar.edu.unq.eperdemic.modelo.exception.NoExisteUbicacion
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.tipo.Humano
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionServiceTest {
    var vectorService = VectorServiceImpl(HibernateVectorDAO(), HibernateDataDAO())
    var ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO(), HibernateDataDAO())
     val vector = Vector()
    val vector1=Vector()
    val tipo = Humano()
    val estado = Sano()
   lateinit var ubicacionCreada:Ubicacion
    lateinit var ubicacionCreada1:Ubicacion

    @Before
    fun setUp(){
    vector.tipo=tipo
    vector.estado=estado
        vector1.tipo=tipo
        vector1.estado=estado

        ubicacionCreada = ubicacionService.crearUbicacion("Florencio Varela")
        ubicacionCreada1 = ubicacionService.crearUbicacion("Berazategui")

    }

    @Test
    fun creacionDeUbicacion() {
        var ubicacion2Creada= ubicacionService.crearUbicacion("Quilmes")

        Assert.assertEquals(ubicacion2Creada.nombreUbicacion, "Quilmes")
    }

    @Test

    fun verificacionDeVectorAlojado() {
        vector.ubicacion=ubicacionCreada
        vectorService.crearVector(vector)
        Assert.assertEquals(vectorService.recuperarVector(1).ubicacion?.nombreUbicacion,"Florencio Varela")
    }

    @Test
    fun alMoverEnUbicacionMueveElVectorDeseadoALaUbicacionDeseada(){
        vector.ubicacion=ubicacionCreada
        vectorService.crearVector(vector)
        var vectorCreado=vectorService.recuperarVector(1)
        Assert.assertEquals(vectorCreado.ubicacion?.nombreUbicacion,"Florencio Varela")
        ubicacionService.crearUbicacion("Quilmes")
        ubicacionService.mover(1,"Quilmes")
        var vectorActualizado=vectorService.recuperarVector(1)
    Assert.assertEquals(vectorActualizado.ubicacion?.nombreUbicacion,"Quilmes")
    }

@Test (expected = NoExisteUbicacion::class)
 fun seMueveAUbicacionQueNoExiste(){
    vector.ubicacion=ubicacionCreada
    vectorService.crearVector(vector)
    ubicacionService.mover(1,"Sarandi")
}

@Test
fun alMoverAMismaUbicacionDondeEstaSeQuedaEnLaMismaUbicacion(){
    vector.ubicacion=ubicacionCreada
    vectorService.crearVector(vector)

    ubicacionService.mover(1,"Florencio Varela")
  var vectorActualizado= vectorService.recuperarVector(1)
          Assert.assertEquals(vectorActualizado.ubicacion?.nombreUbicacion,"Florencio Varela")
}

    @Test(expected = MoverUnVectorQueNoEstaCreado::class )
    fun moverUnVectorQueNoExiste(){
        vector.ubicacion=ubicacionCreada
        ubicacionService.mover(1,"Florencio Varela")
    }


    @Test
    fun moverDosVectoresAUnaMismaUbicacion() {
        vector.ubicacion = ubicacionCreada
        vector1.ubicacion = ubicacionCreada
        vectorService.crearVector(vector)
        vectorService.crearVector(vector1)
        var ubicacionRecuperada= ubicacionService.recuperarUbicacion("Florencio Varela")
        Assert.assertEquals(ubicacionRecuperada.vectores.size,2)
        ubicacionService.mover(1, "Berazategui")
        ubicacionService.mover(2,"Berazategui")
        var ubicacionRecuperada1= ubicacionService.recuperarUbicacion("Berazategui")
        Assert.assertEquals(ubicacionRecuperada1.vectores.size,2)
    }
    @Test
    fun  alMoveVectorAlojadoEnUnaPosicionLaUbicacionTieneUnVectorMenosAlojado(){
        vector.ubicacion=ubicacionCreada
        vectorService.crearVector(vector)
      var  ubicacionCreadaActualizada=ubicacionService.recuperarUbicacion("Florencio Varela")
        ubicacionCreada1 = ubicacionService.crearUbicacion("Quilmes")
        Assert.assertEquals(ubicacionCreadaActualizada.vectores.size,1)
        ubicacionService.mover(1,"Quilmes")
        var ubicacionRecuperada= ubicacionService.recuperarUbicacion("Florencio Varela")
        Assert.assertEquals(ubicacionRecuperada.vectores.size.toInt(),0)
    }
    @Test
    fun recuperarUbicacion(){
        var ubicacion= ubicacionService.recuperarUbicacion("Florencio Varela")
        Assert.assertEquals(ubicacion.nombreUbicacion,"Florencio Varela")
    }
    
    @Test(expected =  NoExisteUbicacion::class)
    fun recuperarUbicacionQueNoExiste(){
        ubicacionService.recuperarUbicacion("Avellaneda")
    }

  @After
  open fun eliminarTodo(){
        vectorService.borrarTodo()
    }
}