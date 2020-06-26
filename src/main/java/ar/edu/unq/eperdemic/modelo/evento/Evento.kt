package ar.edu.unq.eperdemic.modelo.evento

import ar.edu.unq.eperdemic.modelo.evento.tipoEvento.TipoEvento
import java.time.Instant
import java.time.format.DateTimeFormatter

class Evento {
    //Aca faltan colaboradores, ejemplo la especie y bla
   var tipoEvento: TipoEvento? = null
   var accionQueLoDesencadena : String? = null
   var tipoPatogeno : String? = null
    //Quizas convenga mas guardar una instancia de especie que solo el nombre, aunque en esta primer parte no nevcesitamos mas
   var nombreEspecie : String? = null
   var nombreUbicacion : String? = null
    var idVector:Int?=null
var idVectorAInfectar:Int?=null
   var ubicacionContagio : String? = null
   var ubicacionOrigen : String? = null
   var ubicacionDestino : String? = null
   var idVectorQueInfecta : Long? = null
   var idVectorinfectado : Long? = null
   lateinit var mensaje : String
   lateinit var fecha : String

   //Seguramente aca faltan todos los otros atributos de los eventos, independientemente de que se usen o no.

    constructor() {}
    constructor( _tipoEvento : TipoEvento, _accion : String, _tipoPatogeno : String?=null, _nombreEspecie : String? = null, _nombreUbicacion : String? = null,_nombreubicacionOrigen:String?=null,_idVector:Int?=null,_idVectorAInfectar:Int?=null) {
        this.tipoEvento = _tipoEvento
        this.accionQueLoDesencadena = _accion
        this.tipoPatogeno = _tipoPatogeno
        this.nombreEspecie = _nombreEspecie
        this.nombreUbicacion = _nombreUbicacion
        this.ubicacionOrigen=_nombreubicacionOrigen
        this.idVector=_idVector
        this.fecha = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        this.idVectorAInfectar=_idVectorAInfectar
        this.ubicacionContagio = _nombreUbicacion
        this.fecha = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        this.mensaje = this.log()
   }

    fun log() : String = tipoEvento!!.log(this)
}
