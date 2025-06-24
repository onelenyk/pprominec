package dev.onelenyk.pprominec.presentation.ui

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsComponent
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsState

@Composable
fun PermissionsScreen(component: PermissionsComponent) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    var permissionText by remember { mutableStateOf(Manifest.permission.READ_EXTERNAL_STORAGE) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Permissions Screen", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = permissionText,
            onValueChange = { permissionText = it },
            label = { Text("Permission String") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { component.checkPermission(permissionText, context) }) {
            Text("Check Permission")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { component.requestPermission(permissionText, context) }) {
            Text("Request Permission")
        }
        Spacer(Modifier.height(16.dp))
        Text("Current State: ${state.javaClass.simpleName}")
    }
} 