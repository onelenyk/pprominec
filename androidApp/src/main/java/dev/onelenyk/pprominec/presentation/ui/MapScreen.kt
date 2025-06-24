package dev.onelenyk.pprominec.presentation.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.components.main.MapComponent
import java.io.File

@Composable
fun MapScreen(component: MapComponent) {
    val state by component.state.collectAsState()

    val mapFilePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                component.onAddMapUri(it.toString())
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(onClick = {
                    mapFilePicker.launch(
                        arrayOf(
                            "application/octet-stream",
                            "application/x-map",
                            "*/*"
                        )
                    )
                }, modifier = Modifier.padding(16.dp)) {
                    Text("Вибрати .map файл")
                }

                MapFilesCard(
                    mapUris = state.mapUris,
                    selectedMapUri = state.selectedMapUri,
                    onSelect = { component.onSelectMapUri(it) },
                    onDelete = { component.onRemoveMapUri(it) }
                )

                CacheFilesCard(
                    cachedFiles = state.storedFiles,
                    onDelete = { component.onRemoveFromStorage(it) },
                    onClear = { component.onClearStorage() }
                )
            }

            // Placeholder for map area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (state.selectedMapFile != null) {
                    Text("Map display is not available.", color = Color.DarkGray)
                } else {
                    Text("Оберіть .map файл для відображення карти", color = Color.DarkGray)
                }
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MapFilesCard(
    mapUris: Set<String>,
    selectedMapUri: String?,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (mapUris.isNotEmpty()) "Доступні .map файли:" else "Файли не додано",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            mapUris.forEach { uriString ->
                val isSelected = uriString == selectedMapUri
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        Uri.parse(uriString).lastPathSegment ?: uriString,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isSelected) {
                        Button(onClick = { onSelect(uriString) }) {
                            Text("Вибрати")
                        }
                    } else {
                        Text("(Вибрано)", modifier = Modifier.padding(start = 8.dp))
                    }
                    Button(
                        onClick = { onDelete(uriString) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Видалити")
                    }
                }
            }
        }
    }
}

@Composable
fun CacheFilesCard(
    cachedFiles: List<File>,
    onDelete: (File) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Збережені файли:", modifier = Modifier.padding(bottom = 8.dp))
            if (cachedFiles.isEmpty()) {
                Text("Немає збережених файлів")
            } else {
                cachedFiles.forEach { file ->
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(file.name, modifier = Modifier.weight(1f))
                        Text("${file.length()} байт", modifier = Modifier.padding(start = 8.dp))
                        Button(
                            onClick = { onDelete(file) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Видалити")
                        }
                    }
                }
                Button(onClick = onClear, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Видалити всі")
                }
            }
        }
    }
}
