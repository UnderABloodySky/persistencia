package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner
import org.hibernate.Transaction
import java.util.*

class HibernatePatogenoDAO  : HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {
    override fun crear(patogeno: Patogeno): Int {
        super.guardar(patogeno)
        return patogeno.id!!
    }

    override fun recuperar(patogenoId: Int): Patogeno{
        val session = TransactionRunner.currentSession
        return session.get(entityType, patogenoId)
    }


    override fun recuperarATodos(): List<Patogeno> {
        TODO("Not yet implemented")
    }

    override fun eliminarTodos() {
        TODO("Not yet implemented")
    }

}
