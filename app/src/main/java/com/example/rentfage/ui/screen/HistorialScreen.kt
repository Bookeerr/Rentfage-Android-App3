package com.example.rentfage.ui.screen

import android.content.ContentResolver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rentfage.R
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.ui.viewmodel.EstadoSolicitud
import com.example.rentfage.ui.viewmodel.HistorialViewModel
import com.example.rentfage.ui.viewmodel.Solicitud

@Composable
fun HistorialScreen(
    historialViewModel: HistorialViewModel = viewModel()
) {
    val state by historialViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.historial_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold
        )

        if (state.solicitudes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.historial_no_solicitudes),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(state.solicitudes) { solicitud ->
                    SolicitudCard(solicitud = solicitud)
                }
            }
        }
    }
}

@Composable
private fun SolicitudCard(solicitud: Solicitud) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = solicitud.casa.imageUri.toUri(),
                contentDescription = "Imagen de la casa",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = solicitud.casa.address,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(solicitud.estado)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = solicitud.casa.price,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = solicitud.fecha,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(estado: EstadoSolicitud) {
    val (backgroundColor, textColor) = when (estado) {
        EstadoSolicitud.Aprobada -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        EstadoSolicitud.Pendiente -> Color(0xFFFEF0C7) to Color(0xFFB54708)
        EstadoSolicitud.Rechazada -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado.name,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun resourceUri(resourceId: Int): String {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://com.example.rentfage/drawable/$resourceId"
}

@Preview(showBackground = true, name = "Historial con solicitudes")
@Composable
fun HistorialScreenPreview() {
    val casaDeEjemplo1 = CasaEntity(id = 1, price = "UF 32.500", address = "Lo Barnechea, sector La Dehesa", details = "4 hab | 3 baños | 580 m²", imageUri = resourceUri(R.drawable.casa1), latitude = 0.0, longitude = 0.0, isFavorite = false)
    val casaDeEjemplo2 = CasaEntity(id = 2, price = "UF 28.900", address = "Vitacura, sector Santa María", details = "3 hab | 2 baños | 420 m²", imageUri = resourceUri(R.drawable.casa2), latitude = 0.0, longitude = 0.0, isFavorite = false)

    val solicitudesDeEjemplo = listOf(
        Solicitud(1, "test@test.com", casaDeEjemplo1, "15/05/2024", EstadoSolicitud.Aprobada),
        Solicitud(2, "test@test.com", casaDeEjemplo2, "14/05/2024", EstadoSolicitud.Pendiente),
        Solicitud(3, "test@test.com", casaDeEjemplo1, "13/05/2024", EstadoSolicitud.Rechazada)
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text = stringResource(R.string.historial_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp), fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(solicitudesDeEjemplo) { solicitud ->
                SolicitudCard(solicitud = solicitud)
            }
        }
    }
}

@Preview(showBackground = true, name = "Historial sin solicitudes")
@Composable
fun HistorialScreenEmptyPreview() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = stringResource(R.string.historial_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.historial_no_solicitudes), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
    }
}
