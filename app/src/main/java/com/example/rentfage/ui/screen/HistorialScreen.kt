package com.example.rentfage.ui.screen

import android.content.ContentResolver
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rentfage.R
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.ui.viewmodel.EstadoSolicitud
import com.example.rentfage.ui.viewmodel.HistorialViewModel
import com.example.rentfage.ui.viewmodel.SolicitudUi
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HistorialScreen(historialViewModel: HistorialViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        historialViewModel.messageFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val state by historialViewModel.uiState.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Historial de Solicitudes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (state.solicitudes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No has realizado ninguna solicitud aún.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.solicitudes) { solicitud ->
                        SolicitudCard(solicitud)
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudCard(solicitud: SolicitudUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la propiedad
            val imageUri = solicitud.casa?.imageUri
            if (imageUri != null && imageUri.isNotEmpty()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen Casa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = painterResource(R.drawable.casa1),
                    error = painterResource(R.drawable.casa1)
                )
            } else {
                 Image(
                    painter = painterResource(id = R.drawable.casa1),
                    contentDescription = "Imagen por defecto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = solicitud.nombreCasa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = solicitud.casa?.price ?: "Precio no disponible",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Fecha: ${solicitud.fecha}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Estado: ${solicitud.estado}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (solicitud.estado) {
                        EstadoSolicitud.Aprobada -> Color(0xFF4CAF50)
                        EstadoSolicitud.Rechazada -> Color(0xFFF44336)
                        else -> Color(0xFFFF9800)
                    }
                )
            }
        }
    }
}

private fun resourceUri(resourceId: Int): String {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://com.example.rentfage/drawable/$resourceId"
}

@Preview(showBackground = true, name = "Historial con datos")
@Composable
fun HistorialScreenPreview() {
    val casaDeEjemplo = CasaEntity(id = 1, price = "UF 32.500", address = "Lo Barnechea, sector La Dehesa", details = "4 hab | 3 baños | 580 m²", imageUri = resourceUri(R.drawable.casa1), latitude = 0.0, longitude = 0.0, isFavorite = false)
    val solicitudesDeEjemplo = listOf(
        SolicitudUi(1, "cliente1@test.com", casaDeEjemplo, "Casa en La Dehesa", "15/05/2024", EstadoSolicitud.Pendiente),
        SolicitudUi(2, "cliente1@test.com", casaDeEjemplo, "Casa en La Dehesa", "14/05/2024", EstadoSolicitud.Aprobada)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Historial de Solicitudes",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(solicitudesDeEjemplo) { solicitud ->
                SolicitudCard(solicitud)
            }
        }
    }
}