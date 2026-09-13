package com.example.solargarden

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "ranking") {
        composable("ranking") {
            PantallaRanking(navController)
        }
        composable("detalle/{oid}/{nombre}") { backStackEntry ->
            val oid = backStackEntry.arguments?.getString("oid")?.toIntOrNull() ?: 0
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            PantallaDetalle(oid, nombre, navController)
        }
    }
}

@Composable
fun PantallaRanking(navController: NavController) {
    var empleados by remember { mutableStateOf<List<EmpleadoRanking>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                empleados = RetrofitClient.api.getRanking("2026-05-25")
                cargando = false
            } catch (e: Exception) {
                error = e.message
                cargando = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ranking del invernadero", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            cargando -> Text("Cargando...")
            error != null -> Text("Error: $error")
            else -> {
                LazyColumn {
                    items(empleados) { emp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("detalle/${emp.EmployeeId}/${emp.Empleado}")
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(emp.Empleado, style = MaterialTheme.typography.titleMedium)
                                if (emp.SinDatosMedibles == 1) {
                                    Text("Sin datos suficientes esta semana")
                                } else {
                                    Text("Score: ${emp.Score0a100?.toInt() ?: "-"}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalle(oid: Int, nombre: String, navController: NavController) {
    var diagnosticoCampo by remember { mutableStateOf<Diagnostico?>(null) }
    var diagnosticoEmpaque by remember { mutableStateOf<Diagnostico?>(null) }
    var historicoCampo by remember { mutableStateOf<List<PuntoHistorico>>(emptyList()) }
    var historicoEmpaque by remember { mutableStateOf<List<PuntoHistorico>>(emptyList()) }
    var atributosCampo by remember { mutableStateOf<Atributos?>(null) }
    var atributosEmpaque by remember { mutableStateOf<Atributos?>(null) }
    var conMeta by remember { mutableStateOf<List<ActividadConMeta>>(emptyList()) }
    var sinMeta by remember { mutableStateOf<List<ActividadSinMeta>>(emptyList()) }
    var tendenciaDiaria by remember { mutableStateOf<List<PuntoDiario>>(emptyList()) }
    var empaquePorProducto by remember { mutableStateOf<List<ProductoEmpaque>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(oid) {
        scope.launch {
            try {
                val resultado = RetrofitClient.api.getDiagnosticoCampo(oid)
                diagnosticoCampo = if (resultado.lectura_ia != null) resultado else null
            } catch (e: Exception) { diagnosticoCampo = null }

            try {
                val resultado = RetrofitClient.api.getDiagnosticoEmpaque(oid)
                diagnosticoEmpaque = if (resultado.lectura_ia != null) resultado else null
            } catch (e: Exception) { diagnosticoEmpaque = null }

            try { historicoCampo = RetrofitClient.api.getHistoricoCampo(oid) }
            catch (e: Exception) { historicoCampo = emptyList() }

            try { historicoEmpaque = RetrofitClient.api.getHistoricoEmpaque(oid) }
            catch (e: Exception) { historicoEmpaque = emptyList() }

            try { atributosCampo = RetrofitClient.api.getAtributosCampo(oid) }
            catch (e: Exception) { atributosCampo = null }

            try { atributosEmpaque = RetrofitClient.api.getAtributosEmpaque(oid) }
            catch (e: Exception) { atributosEmpaque = null }

            try { conMeta = RetrofitClient.api.getConMeta(oid) }
            catch (e: Exception) { conMeta = emptyList() }

            try { sinMeta = RetrofitClient.api.getSinMeta(oid) }
            catch (e: Exception) { sinMeta = emptyList() }

            try { tendenciaDiaria = RetrofitClient.api.getTendenciaDiaria(oid) }
            catch (e: Exception) { tendenciaDiaria = emptyList() }

            try { empaquePorProducto = RetrofitClient.api.getEmpaque(oid) }
            catch (e: Exception) { empaquePorProducto = emptyList() }

            val hayDatosCampo = diagnosticoCampo != null || conMeta.isNotEmpty() || sinMeta.isNotEmpty()
            val hayDatosEmpaque = diagnosticoEmpaque != null || empaquePorProducto.isNotEmpty()
            if (!hayDatosCampo && !hayDatosEmpaque) {
                error = "Sin datos suficientes para esta persona esta semana"
            }
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = { navController.popBackStack() }) {
            Text("← Volver")
        }
        Text(nombre, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            cargando -> Text("Cargando diagnóstico...")
            error != null -> Text(error!!)
            else -> {
                val hayDatosCampoUI = diagnosticoCampo != null || conMeta.isNotEmpty() || sinMeta.isNotEmpty()
                val hayDatosEmpaqueUI = diagnosticoEmpaque != null || empaquePorProducto.isNotEmpty()
                if (hayDatosCampoUI) {
                    Text("Campo", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (diagnosticoCampo != null) {
                        Text("Puntaje: ${"%.2f".format(diagnosticoCampo!!.puntaje)}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(diagnosticoCampo!!.lectura_ia, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        Text(
                            "No se pudo generar el diagnóstico general esta semana, pero sí hay actividades registradas:",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (historicoCampo.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tendencia · últimas ${historicoCampo.size} semanas", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        GraficaLineaTendencia(historicoCampo)
                    }

                    if (atributosCampo != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Atributos del desempeño", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ListaAtributos(atributosCampo!!)
                    }

                    if (tendenciaDiaria.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tendencia — Puntaje Z compuesto por día", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                GraficaBarrasTendenciaDiaria(tendenciaDiaria)
                            }
                        }
                    }

                    if (conMeta.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Actividades con meta oficial", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val agrupadoConMeta = agruparConMetaPorActividad(conMeta)
                        agrupadoConMeta.forEach { (actividad, dias) ->
                            TarjetaActividadConMeta(actividad, dias)
                        }
                    }

                    if (sinMeta.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Actividades sin meta oficial", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val agrupadoSinMeta = agruparSinMetaPorActividad(sinMeta)
                        agrupadoSinMeta.forEach { (actividad, dias) ->
                            TarjetaActividadSinMeta(actividad, dias)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (hayDatosEmpaqueUI) {
                    Text("Empaque", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (diagnosticoEmpaque != null) {
                        Text("Puntaje: ${"%.2f".format(diagnosticoEmpaque!!.puntaje)}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(diagnosticoEmpaque!!.lectura_ia, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        Text(
                            "No se pudo generar el diagnóstico general esta semana, pero sí hay actividades registradas:",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (historicoEmpaque.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tendencia · últimas ${historicoEmpaque.size} semanas", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        GraficaLineaTendencia(historicoEmpaque)
                    }

                    if (atributosEmpaque != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Atributos del desempeño", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ListaAtributos(atributosEmpaque!!)
                    }

                    if (empaquePorProducto.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Actividades de empaque", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val agrupadoEmpaque = agruparEmpaquePorProducto(empaquePorProducto)
                        agrupadoEmpaque.forEach { (producto, dias) ->
                            TarjetaProductoEmpaque(producto, dias)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ListaAtributos(atributos: Atributos) {
    val lista = listOf(
        "Eficiencia" to atributos.Eficiencia,
        "Consistencia" to atributos.Consistencia,
        "Asistencia" to atributos.Asistencia,
        "Experiencia" to atributos.Experiencia,
        "Ritmo" to atributos.Ritmo,
        "Generalizacion" to atributos.Generalizacion
    )

    Column {
        lista.forEach { (nombre, valor) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(nombre)
                Text("$valor", style = MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(
                progress = valor / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GraficaLineaTendencia(puntos: List<PuntoHistorico>) {
    val ordenados = puntos.sortedBy { it.Semana }
    val maximo = ordenados.maxOf { it.Puntaje }
    val minimo = ordenados.minOf { it.Puntaje }
    val rango = (maximo - minimo).let { if (it == 0.0) 1.0 else it }

    val colorLinea = Color(0xFF2F6B3D)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        if (ordenados.size < 2) {
            // Con un solo punto no se puede dibujar línea, se dibuja un punto
            if (ordenados.size == 1) {
                val y = size.height - ((ordenados[0].Puntaje - minimo) / rango * size.height).toFloat()
                drawCircle(color = colorLinea, radius = 6f, center = androidx.compose.ui.geometry.Offset(size.width / 2, y))
            }
            return@Canvas
        }

        val espacioX = size.width / (ordenados.size - 1)

        val puntosPixel = ordenados.mapIndexed { index, punto ->
            val x = index * espacioX
            val y = size.height - ((punto.Puntaje - minimo) / rango * size.height).toFloat()
            androidx.compose.ui.geometry.Offset(x, y)
        }

        // Dibuja la línea conectando cada punto con el siguiente
        for (i in 0 until puntosPixel.size - 1) {
            drawLine(
                color = colorLinea,
                start = puntosPixel[i],
                end = puntosPixel[i + 1],
                strokeWidth = 5f
            )
        }

        // Dibuja un círculo en cada punto real
        puntosPixel.forEach { offset ->
            drawCircle(color = colorLinea, radius = 6f, center = offset)
        }
    }
}

fun agruparConMetaPorActividad(filas: List<ActividadConMeta>): Map<String, List<ActividadConMeta>> {
    return filas.groupBy { it.Actividad }
}

fun agruparSinMetaPorActividad(filas: List<ActividadSinMeta>): Map<String, List<ActividadSinMeta>> {
    return filas.groupBy { it.Actividad }
}

fun agruparEmpaquePorProducto(filas: List<ProductoEmpaque>): Map<String, List<ProductoEmpaque>> {
    return filas.groupBy { it.Producto }
}

@Composable
fun TarjetaActividadConMeta(actividad: String, dias: List<ActividadConMeta>) {
    val diasOrdenados = dias.sortedBy { it.Fecha }
    val meta = dias.firstOrNull()?.Meta
    val ultimoPorcentaje = diasOrdenados.lastOrNull()?.PorcentajeMeta

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(actividad, style = MaterialTheme.typography.titleSmall)
                if (ultimoPorcentaje != null) {
                    BadgePorcentaje(ultimoPorcentaje)
                } else {
                    Text("Sin horas", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (meta != null) {
                Text("Meta oficial: $meta", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            GraficaBarrasPorcentaje(
                fechas = diasOrdenados.map { it.Fecha },
                valores = diasOrdenados.map { it.PorcentajeMeta ?: 0.0 }
            )
        }
    }
}

@Composable
fun BadgePorcentaje(valor: Double) {
    val color = when {
        valor >= 100 -> Color(0xFF2F6B3D)
        valor >= 80 -> Color(0xFFE3A72B)
        else -> Color(0xFFD64541)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            "${valor.toInt()}%",
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// Convierte "2026-05-23" (o "2026-05-23T00:00:00") a "23/05" para las etiquetas del eje X
fun formatearFechaCorta(fecha: String): String {
    return try {
        val partes = fecha.substring(0, 10).split("-")
        "${partes[2]}/${partes[1]}"
    } catch (e: Exception) {
        fecha
    }
}

@Composable
fun GraficaBarrasPorcentaje(fechas: List<String>, valores: List<Double>) {
    val maximo = maxOf(120.0, valores.maxOrNull() ?: 100.0)
    val colorTexto = android.graphics.Color.rgb(107, 122, 109) // #6B7A6D

    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        val anchoBarra = size.width / valores.size
        val alturaMaxima = size.height - 26 // deja espacio abajo para la fecha

        // Línea de referencia en 100%
        val yLinea100 = (alturaMaxima - (100.0 / maximo * alturaMaxima)).toFloat()
        drawLine(
            color = Color(0xFF6B7A6D),
            start = androidx.compose.ui.geometry.Offset(0f, yLinea100),
            end = androidx.compose.ui.geometry.Offset(size.width, yLinea100),
            strokeWidth = 2f
        )

        valores.forEachIndexed { index, valor ->
            val alturaBarra = (valor / maximo * alturaMaxima).toFloat()
            val x = index * anchoBarra + anchoBarra * 0.2f
            val anchoReal = anchoBarra * 0.6f
            val y = alturaMaxima.toFloat() - alturaBarra

            val colorBarra = when {
                valor >= 100 -> Color(0xFF2F6B3D)
                valor >= 80 -> Color(0xFFE3A72B)
                else -> Color(0xFFD64541)
            }

            drawRect(
                color = colorBarra,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(anchoReal, alturaBarra)
            )

            // Etiqueta de fecha debajo de la barra
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = colorTexto
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    formatearFechaCorta(fechas.getOrElse(index) { "" }),
                    x + anchoReal / 2,
                    size.height - 4,
                    paint
                )
            }
        }
    }
}

@Composable
fun TarjetaActividadSinMeta(actividad: String, dias: List<ActividadSinMeta>) {
    val diasOrdenados = dias.sortedBy { it.Fecha }
    val nEmpleados = dias.firstOrNull()?.NEmpleadosBenchmark
    val ultimoZ = diasOrdenados.lastOrNull()?.ZActividad

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(actividad, style = MaterialTheme.typography.titleSmall)
                if (ultimoZ != null) {
                    BadgeZScore(ultimoZ)
                }
            }
            if (nEmpleados != null) {
                Text("Benchmark con $nEmpleados compañeros", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            GraficaBarrasZ(
                fechas = diasOrdenados.map { it.Fecha },
                valores = diasOrdenados.map { it.ZActividad ?: 0.0 }
            )
        }
    }
}

@Composable
fun BadgeZScore(valor: Double) {
    val color = if (valor >= 0) Color(0xFF2F6B3D) else Color(0xFFD64541)
    val texto = if (valor >= 0) "+${"%.2f".format(valor)}" else "%.2f".format(valor)
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(texto, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun GraficaBarrasZ(fechas: List<String>, valores: List<Double>) {
    val maximo = maxOf(1.0, valores.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0)
    val colorTexto = android.graphics.Color.rgb(107, 122, 109) // #6B7A6D

    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        val anchoBarra = size.width / valores.size
        val alturaGrafica = size.height - 20 // deja espacio abajo para la fecha
        val mitad = alturaGrafica / 2

        drawLine(
            color = Color(0xFFD8DED2),
            start = androidx.compose.ui.geometry.Offset(0f, mitad),
            end = androidx.compose.ui.geometry.Offset(size.width, mitad),
            strokeWidth = 2f
        )

        valores.forEachIndexed { index, valor ->
            val alturaCalculada = (kotlin.math.abs(valor) / maximo * (mitad - 10)).toFloat()
            // Altura mínima visible (3dp) para que un valor distinto de cero siempre se note,
            // aunque sea muy pequeño. Si el valor es exactamente 0, no se dibuja barra.
            val alturaBarra = if (valor != 0.0) alturaCalculada.coerceAtLeast(3.dp.toPx()) else 0f
            val x = index * anchoBarra + anchoBarra * 0.2f
            val anchoReal = anchoBarra * 0.6f
            val colorBarra = if (valor >= 0) Color(0xFF2F6B3D) else Color(0xFFD64541)
            val y = if (valor >= 0) mitad - alturaBarra else mitad

            drawRect(
                color = colorBarra,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(anchoReal, alturaBarra)
            )

            // Etiqueta de fecha debajo de la barra
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = colorTexto
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    formatearFechaCorta(fechas.getOrElse(index) { "" }),
                    x + anchoReal / 2,
                    size.height - 4,
                    paint
                )
            }
        }
    }
}
@Composable
fun GraficaBarrasTendenciaDiaria(puntos: List<PuntoDiario>) {
    val ordenados = puntos.sortedBy { it.Fecha }
    val maximo = maxOf(0.3, ordenados.maxOfOrNull { kotlin.math.abs(it.PuntajeDia) } ?: 0.3)
    val colorTexto = android.graphics.Color.rgb(107, 122, 109) // #6B7A6D

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val anchoBarra = size.width / ordenados.size
        val alturaGrafica = size.height - 24 // espacio abajo para la fecha
        val mitad = alturaGrafica / 2 // línea de referencia en 0

        drawLine(
            color = Color(0xFFD8DED2),
            start = androidx.compose.ui.geometry.Offset(0f, mitad),
            end = androidx.compose.ui.geometry.Offset(size.width, mitad),
            strokeWidth = 2f
        )

        ordenados.forEachIndexed { index, punto ->
            val alturaCalculada = (kotlin.math.abs(punto.PuntajeDia) / maximo * (mitad - 10)).toFloat()
                .coerceAtMost(mitad.toFloat() - 10f)
            val alturaBarra = if (punto.PuntajeDia != 0.0) alturaCalculada.coerceAtLeast(3.dp.toPx()) else 0f
            val x = index * anchoBarra + anchoBarra * 0.12f
            val anchoReal = anchoBarra * 0.76f
            val colorBarra = if (punto.PuntajeDia >= 0) Color(0xFF2F6B3D) else Color(0xFFD64541)
            val y = if (punto.PuntajeDia >= 0) mitad - alturaBarra else mitad

            drawRect(
                color = colorBarra,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(anchoReal, alturaBarra)
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = colorTexto
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    formatearFechaCorta(punto.Fecha),
                    x + anchoReal / 2,
                    size.height - 4,
                    paint
                )
            }
        }
    }
}

@Composable
fun TarjetaProductoEmpaque(producto: String, dias: List<ProductoEmpaque>) {
    val diasOrdenados = dias.sortedBy { it.Fecha }
    val nEmpleados = dias.firstOrNull()?.NEmpleadosBenchmark
    val ultimoZ = diasOrdenados.lastOrNull()?.ZActividad

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(producto, style = MaterialTheme.typography.titleSmall)
                if (ultimoZ != null) {
                    BadgeZScore(ultimoZ)
                }
            }
            if (nEmpleados != null) {
                Text("Benchmark con $nEmpleados compañeros", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            GraficaBarrasZ(
                fechas = diasOrdenados.map { it.Fecha },
                valores = diasOrdenados.map { it.ZActividad ?: 0.0 }
            )
        }
    }
}