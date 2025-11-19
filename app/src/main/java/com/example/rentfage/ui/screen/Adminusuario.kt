package com.example.rentfage.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.ui.viewmodel.UserViewModel

@Composable
fun AdminUsuario(userViewModel: UserViewModel = viewModel()) {
    // Recargar usuarios al entrar a la pantalla
    LaunchedEffect(Unit) {
        userViewModel.loadUsers()
    }

    val users by userViewModel.users.collectAsState()
    AdminUsuarioContent(
        users = users,
        onUpdateUserRole = userViewModel::updateUserRole
    )
}

@Composable
fun AdminUsuarioContent(
    users: List<UserEntity>,
    onUpdateUserRole: (UserEntity, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestionar Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre tarjetas
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = user.role == "ADMIN",
                            onCheckedChange = { isAdmin ->
                                onUpdateUserRole(user, isAdmin)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminUsuarioPreview() {
    val sampleUsers = listOf(
        UserEntity(id = 1, name = "Quint", email = "quint@example.com", phone = "123456789", pass = "pass", role = "ADMIN"),
        UserEntity(id = 2, name = "Usuario de Ejemplo", email = "user@example.com", phone = "987654321", pass = "pass", role = "USER"),
        UserEntity(id = 3, name = "Otro Usuario", email = "otro@example.com", phone = "112233445", pass = "pass", role = "USER")
    )
    AdminUsuarioContent(
        users = sampleUsers,
        onUpdateUserRole = { _, _ -> }
    )
}
