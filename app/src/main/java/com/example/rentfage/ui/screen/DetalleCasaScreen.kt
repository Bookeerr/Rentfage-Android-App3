package com.example.rentfage.ui.screen

import android.content.ContentResolver
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.rentfage.R
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.ui.viewmodel.CasasViewModel
import com.example.rentfage.ui.viewmodel.HistorialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DetalleCasaScreenVm(
    casaId: Int,
    onGoHome: () -> Unit,
    historialViewModel: HistorialViewModel,
    casasViewModel: CasasViewModel
) {
    val uiState by casasViewModel.uiState.collectAsStateWithLifecycle()
    val casa = remember(uiState, casaId) { uiState.casas.find { it.id == casaId } }

    if (casa != null) {
        DetalleCasaContent(
            casa = casa,
            onGoHome = onGoHome,
            onAddSolicitud = { historialViewModel.addSolicitud(casa) }
        )
    }
}

@Composable
private fun DetalleCasaContent(
    casa: CasaEntity,
    onGoHome: () -> Unit,
    onAddSolicitud: () -> Unit
) {
    var showPurchaseSummary by rememberSaveable { mutableStateOf(false) }
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            if (!showPurchaseSummary) {
                // Eliminamos tonalElevation para quitar el tinte rojo del fondo.
                // Forzamos el color a Surface (blanco/tema claro) y mantenemos la sombra.
                Surface(
                    tonalElevation = 0.dp, 
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = { showConfirmationDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Comprar esta propiedad", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = showPurchaseSummary, 
            label = "PurchaseScreenAnimation",
            modifier = Modifier.padding(innerPadding)
        ) { isSummaryVisible ->
            if (isSummaryVisible) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ElevatedCard(elevation = CardDefaults.cardElevation(8.dp)) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, "Éxito", Modifier.size(80.dp), tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("¡Solicitud Enviada!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Un asesor se comunicará contigo.", style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(32.dp))
                            OutlinedButton(onClick = onGoHome, Modifier.fillMaxWidth()) { Text("Volver a propiedades") }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = casa.imageUri.toUri(),
                            contentDescription = "Imagen de la casa",
                            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Text(casa.price, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Text(casa.address, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                            Icon(Icons.Default.LocationOn, "Ubicación", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ubicación excelente", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sobre la propiedad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        
                        Text(casa.details, style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(80.dp)) 
                    }
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirmación de Compra") },
            text = { Text("¿Estás seguro de que quieres enviar una solicitud de compra para esta propiedad?") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        onAddSolicitud()
                        showConfirmationDialog = false
                        delay(300)
                        showPurchaseSummary = true
                    }
                }) { Text("Sí, comprar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun resourceUri(resourceId: Int): String {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://com.example.rentfage/drawable/$resourceId"
}

@Preview(showBackground = true)
@Composable
fun DetalleCasaScreenPreview() {
    val casaDeEjemplo = CasaEntity(1, "UF 28.500", "Av. Vitacura, Vitacura, Santiago", "Acogedora casa con amplio jardín.", resourceUri(R.drawable.casa1), -33.4130, -70.5947)
    DetalleCasaContent(casa = casaDeEjemplo, onGoHome = {}, onAddSolicitud = {})
}
