package com.example.rentfage.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.rentfage.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onOpenDrawer: () -> Unit,
    onGoToProfile: () -> Unit // Nueva acción para ir al perfil
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(id = R.string.top_bar_menu_icon_description)
                )
            }
        },
        actions = {
            // Añadimos el nuevo icono de perfil como una acción
            IconButton(onClick = onGoToProfile) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = stringResource(id = R.string.top_bar_profile_icon_description)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Color del nuevo icono
        )
    )
}
