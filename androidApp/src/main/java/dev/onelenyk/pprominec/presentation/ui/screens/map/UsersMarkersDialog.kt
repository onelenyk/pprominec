package dev.onelenyk.pprominec.presentation.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.ui.MapMarker


@Composable
fun UsersMarkersDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    markers: List<MapMarker>,
    onMarkerEdit: (MapMarker) -> Unit,
    onMarkerDelete: (String) -> Unit,
    onMarkerAdd: () -> Unit
) {
    if (!isVisible) return

    var editingMarkerId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Markers") },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(markers) { index, marker ->
                        MarkerItem(
                            index = index + 1,
                            marker = marker,
                            isEditing = editingMarkerId == marker.id,
                            onEditClick = { editingMarkerId = marker.id },
                            onSaveClick = {
                                onMarkerEdit(marker)
                                editingMarkerId = null
                            },
                            onDeleteClick = { onMarkerDelete(marker.id) }
                        )
                        if (index < markers.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
                Button(
                    onClick = onMarkerAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add marker")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Marker")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun MarkerItem(
    index: Int,
    marker: MapMarker,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var title by remember { mutableStateOf(marker.title) }
    var position by remember { mutableStateOf("${marker.latitude}, ${marker.longitude}") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            modifier = Modifier.width(32.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = title)
                Text(
                    text = position,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Row {
            IconButton(
                onClick = if (isEditing) onSaveClick else onEditClick
            ) {
                Icon(
                    if (isEditing) Icons.Default.AddCircle else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit"
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
