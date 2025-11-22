package com.example.rentfage.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rentfage.data.repository.UserRepository
import com.example.rentfage.ui.viewmodel.AuthViewModel
import com.example.rentfage.ui.viewmodel.ResenaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaMiResena(
    resenaViewModel: ResenaViewModel,
    userRepository: UserRepository 
) {
    val uiState by resenaViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeEmail = AuthViewModel.activeUserEmail

    LaunchedEffect(Unit) {
        if (activeEmail != null) {
            val user = userRepository.getUserByEmail(activeEmail)
            user?.id?.let { resenaViewModel.cargarResenaDeUsuario(it.toInt()) }
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "¡Gracias por tu nueva opinión!", Toast.LENGTH_SHORT).show()
            resenaViewModel.resetSaveStatus()
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                // --- SECCIÓN 1: Escribir nueva reseña ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Deja tu Reseña",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = uiState.comentario,
                            onValueChange = resenaViewModel::onComentarioChange,
                            label = { Text("Escribe aquí tu nueva opinión...") },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            maxLines = 8,
                            supportingText = {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text(
                                        text = "${uiState.comentario.length} / 300",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.comentario.length >= 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (activeEmail != null) {
                                    scope.launch {
                                        val user = userRepository.getUserByEmail(activeEmail)
                                        user?.id?.let { userId ->
                                            resenaViewModel.enviarResena(userId.toInt())
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.comentario.isNotBlank()
                        ) {
                            Text("Enviar Nueva Reseña")
                        }
                    }
                }

                // --- SECCIÓN 2: Ver reseña anterior ---
                if (uiState.reseñaExistente != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column {
                         Text(
                            text = "Tu Última Reseña Enviada",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = uiState.reseñaExistente!!.comentario,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Enviada el: ${formatDate(uiState.reseñaExistente!!.fechaCreacion)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
