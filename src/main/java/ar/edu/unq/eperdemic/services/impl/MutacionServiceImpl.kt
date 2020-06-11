package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateEspecieDAO
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class MutacionServiceImpl(var mutacionDao : MutacionDAO, val patogenoService: PatogenoService) : MutacionService{
    override fun mutar(especieId: Int, mutacionId: Int) {
        TransactionRunner.addHibernate().runTrx {
            val especieDAO = HibernateEspecieDAO()

            val mutacion = mutacionDao.recuperar(mutacionId)
            val especie = especieDAO.recuperarEspecie(especieId)

            especie.mutar(mutacion)
            especieDAO.actualizar(especie)
        }
    }

    override fun crearMutacion(mutacion: Mutacion): Mutacion {
        return TransactionRunner.addHibernate().runTrx { mutacionDao.crear(mutacion) }
    }

    override fun recuperarMutacion(mutacionId: Int): Mutacion = TransactionRunner.addHibernate().runTrx { mutacionDao.recuperar(mutacionId) }

    override fun actualizarMutacion(mutacion: Mutacion) {
        TransactionRunner.addHibernate().runTrx {
            mutacionDao.actualizar(mutacion)
        }
    }
}