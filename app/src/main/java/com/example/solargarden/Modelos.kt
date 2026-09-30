package com.example.solargarden

data class EmpleadoRanking(
    val EmployeeId: Int,
    val Empleado: String,
    val PuntajeUnificado: Double,
    val HorasTotales: Double,
    val SinDatosMedibles: Int,
    val Score0a100: Double?
)

data class Diagnostico(
    val puntaje: Double,
    val lectura_ia: String
)

data class PuntoHistorico(
    val Semana: String,
    val Puntaje: Double,
    val Horas: Double
)

data class Asistencia(
    val DiasTrabajados: Int,
    val DiasOperadosInvernadero: Int,
    val PctAsistencia: Double
)

data class Atributos(
    val Eficiencia: Int,
    val Consistencia: Int,
    val Asistencia: Int,
    val Experiencia: Int,
    val Ritmo: Int,
    val Generalizacion: Int
)

data class ActividadConMeta(
    val Empleado: String,
    val Fecha: String,
    val Actividad: String,
    val Horas: Double,
    val Cantidad: Double,
    val Meta: Double,
    val PorcentajeMeta: Double?
)

data class ActividadSinMeta(
    val Empleado: String,
    val Fecha: String,
    val Actividad: String,
    val Horas: Double,
    val Rendimiento: Double?,
    val PromedioCompaneros: Double?,
    val ZActividad: Double?,
    val NEmpleadosBenchmark: Int?
)

data class PuntoDiario(
    val Fecha: String,
    val PuntajeDia: Double,
    val HorasDia: Double
)

data class ProductoEmpaque(
    val Empleado: String,
    val Fecha: String,
    val Producto: String,
    val Horas: Double,
    val Unidades: Double?,
    val PromedioCompaneros: Double?,
    val ZActividad: Double?,
    val NEmpleadosBenchmark: Int?
)

/**
 * Fila del ranking cuando está filtrado a una sola actividad.
 * Corresponde a lo que regresa leer_ranking_por_actividad() en cache_db.py:
 * EmployeeId, Empleado, ValorPromedio (PorcentajeMeta o ZActividad según el
 * tipo de actividad), HorasActividad.
 */
data class EmpleadoRankingActividad(
    val EmployeeId: Int,
    val Empleado: String,
    val ValorPromedio: Double?,
    val HorasActividad: Double?
)

/**
 * Lectura de IA enfocada en una sola actividad (cuando el detalle está
 * filtrado). "texto" viene null si esa actividad no tiene feature propia
 * en el modelo (ej. "Barrer entrada surco") o si esa persona no tiene
 * diagnóstico calculado esta semana.
 */
data class LecturaActividad(
    val pct_horas: Double?,
    val contribucion_shap: Double?,
    val texto: String?
)

/**
 * Un punto del histórico de UNA actividad (Semana + Valor promedio esa
 * semana -- PorcentajeMeta o ZActividad según el tipo). Viene de
 * leer_historico_actividad() en cache_db.py.
 */
data class PuntoHistoricoActividad(
    val Semana: String,
    val Valor: Double
)

/**
 * Eficiencia, Consistencia y Ritmo calculados SOLO con el histórico de
 * una actividad específica. Viene de /atributos-actividad. Asistencia,
 * Experiencia y Generalizacion no tienen equivalente por actividad, así
 * que no vienen aquí -- se siguen tomando de Atributos (el general).
 */
data class AtributosActividad(
    val Eficiencia: Int,
    val Consistencia: Int,
    val Ritmo: Int
)