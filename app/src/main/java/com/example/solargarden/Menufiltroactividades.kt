package com.example.solargarden

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lista de actividades disponibles para filtrar el ranking.
 * "Todas las Labores" siempre va primero y representa "sin filtro".
 * (Se quitó "Descabezar planta": no existe como actividad real en la BD.)
 */
val ACTIVIDADES_FILTRO = listOf(
    "Todas las Labores",
    "Desbrotar",
    "Desbrotar bajo",
    "Enredar",
    "Deshojar manual (5+ hojas)",
    "Ralear",
    "Poner clip",
    "Quitar Raquis",
    "Colocar ganchos",
    "Barrer entrada surco",
    "Bajar planta",
    "Cosechar"
)

/**
 * Mismo mapeo que ACTIVIDADES_MENU en cache_db.py, pero solo para saber
 * a qué endpoint pegarle desde el cliente (con-meta-filtrado o
 * sin-meta-filtrado). El nombre exacto que se manda al backend es el
 * texto de este mapa tal cual (el backend hace la traducción final al
 * nombre real de la BD, ej. "Poner clip" -> "Poner clips").
 */
val ACTIVIDADES_TIPO = mapOf(
    "Desbrotar" to "con_meta",
    "Desbrotar bajo" to "con_meta",
    "Enredar" to "con_meta",
    "Deshojar manual (5+ hojas)" to "sin_meta",
    "Ralear" to "con_meta",
    "Poner clip" to "con_meta",
    "Quitar Raquis" to "sin_meta",
    "Colocar ganchos" to "sin_meta",
    "Barrer entrada surco" to "sin_meta",
    "Bajar planta" to "con_meta",
    "Cosechar" to "con_meta"
)

/**
 * Menú desplegable de actividades, estilo píldora con ícono de filtro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFiltroActividades(
    actividadSeleccionada: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    val colorTexto = Color(0xFF2F6B3D)
    val colorFondoSeleccionado = Color(0xFF2F6B3D).copy(alpha = 0.10f)

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF3F5F1))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.List,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = actividadSeleccionada,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF20321F),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF6B7A6D),
                modifier = Modifier.size(20.dp)
            )
        }

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            ACTIVIDADES_FILTRO.forEach { actividad ->
                val seleccionada = actividad == actividadSeleccionada
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (seleccionada) colorTexto else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = actividad,
                                color = if (seleccionada) colorTexto else Color(0xFF20321F),
                                style = if (seleccionada)
                                    MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                else
                                    MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    onClick = {
                        onSeleccion(actividad)
                        expandido = false
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (seleccionada) colorFondoSeleccionado else Color.Transparent)
                )
            }
        }
    }
}