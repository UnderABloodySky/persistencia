package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.exception.PatogenoNotFoundRunTimeException
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.jdbc.JDBCPatogenoDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.jdbc.DataServiceJDBC
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PatogenoServiceTest{
    private var dao : PatogenoDAO = JDBCPatogenoDAO()
    private val dataService : DataService = DataServiceJDBC(dao)
    private val service : PatogenoService = PatogenoServiceImpl(dao)

    @Before
    fun crearModelo() {
        dataService.crearSetDeDatosIniciales()
    }

    @Test
    fun alCrearUnPatogenoSinElementosAnterioresElIDAsignadoEs1() {
        this.eliminarModelo()
        val idPatogenoCreado = service.crearPatogeno(Patogeno("ProbandoService"))
        val patogenoRecuperado = service.recuperarPatogeno(idPatogenoCreado)
        Assert.assertEquals(1, idPatogenoCreado)
        Assert.assertEquals(idPatogenoCreado, patogenoRecuperado.id)
    }

    @Test
    fun alCrearPatogenoYLuegoRecuperarSeObtienePatogenosSimilares() {
        val idPatogenoCreado = service.crearPatogeno(Patogeno("ProbandoService"))
        val patogenoRecuperado = service.recuperarPatogeno(idPatogenoCreado)
        Assert.assertEquals(idPatogenoCreado, patogenoRecuperado.id)
        Assert.assertEquals("ProbandoService", patogenoRecuperado.tipo)
        Assert.assertEquals(0, patogenoRecuperado.cantidadDeEspecies)
    }


    @Test(expected = PatogenoNotFoundRunTimeException::class)
    fun alRecuperarUnPatogenoNoExistenteLanzaUnaExcepcion() {
        dao.recuperar(42)
    }

    @Test
    fun seModificanLosValoresDelPatogenoAlActualizarlo() {
        val patogenoOriginal = service.recuperarPatogeno(4)
        Assert.assertEquals(4, patogenoOriginal.id)
        Assert.assertEquals("Bacteria", patogenoOriginal.tipo)
        Assert.assertEquals(0, patogenoOriginal.cantidadDeEspecies)

        service.agregarEspecie(4, "Sarasa", "MiCasa")
        val patogenoRecuperado = service.recuperarPatogeno(4)
        Assert.assertEquals(4, patogenoRecuperado.id)
        Assert.assertEquals("Bacteria", patogenoRecuperado.tipo)
        Assert.assertEquals(1, patogenoRecuperado.cantidadDeEspecies)
        Assert.assertFalse(patogenoOriginal.cantidadDeEspecies == patogenoRecuperado.cantidadDeEspecies)
    }

    @Test
    fun elRecuperarTodosTraeUnaListaVaciaCuandoNoHayNingunDatoCargado() {
        this.eliminarModelo()
        val patogenosRecuperados = service.recuperarATodosLosPatogenos()
        Assert.assertEquals(0, patogenosRecuperados.size)
        Assert.assertTrue(patogenosRecuperados.isEmpty())
    }

    @Test
    fun elRecuperarTodosTraeTodosLosPatogenosPersistidos() {
        val patogenosRecuperados = service.recuperarATodosLosPatogenos()
        Assert.assertEquals(4, patogenosRecuperados.size)
        val nombres = patogenosRecuperados.map{ it.tipo }
        Assert.assertTrue(nombres.contains("Bacteria"))
        Assert.assertTrue(nombres.contains("Protozoo"))
        Assert.assertTrue(nombres.contains("Hongo"))
        Assert.assertTrue(nombres.contains("Virus"))
        val ids = patogenosRecuperados.map{ it.id}
        Assert.assertTrue(ids.contains(1))
        Assert.assertTrue(ids.contains(2))
        Assert.assertTrue(ids.contains(3))
        Assert.assertTrue(ids.contains(4))
    }

    @Test
    fun elRecuperarTodosTraeUnaListaConToodosLosPatogenosOrdenadosAlfabeticamenteSegunSuTipo() {
        val patogenosRecuperados = service.recuperarATodosLosPatogenos()
        Assert.assertEquals("Bacteria", patogenosRecuperados.get(0).tipo)
        Assert.assertEquals("Hongo", patogenosRecuperados.get(1).tipo)
        Assert.assertEquals("Protozoo", patogenosRecuperados.get(2).tipo)
        Assert.assertEquals("Virus", patogenosRecuperados.get(3).tipo)
    }

    @After
    fun eliminarModelo() {
        dataService.eliminarTodo()
    }
}