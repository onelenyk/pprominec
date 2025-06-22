package dev.onelenyk.pprominec.presentation.`interface`

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.bussines.AzimuthCalculatorAPI
import dev.onelenyk.pprominec.bussines.AzimuthInputNormalizer
import dev.onelenyk.pprominec.presentation.components.MainComponent

@Composable
fun MainScreen(component: MainComponent) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        InputAndResultScreen(
            modifier = Modifier.padding(innerPadding), component = component
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InputAndResultScreen(
    modifier: Modifier = Modifier, component: MainComponent
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSampleText by remember { mutableStateOf("Оберіть зразок...") }

    // --- Color definitions for cards and text ---
    val cardAColor = Color(0xFFE3F2FD) // Light blue
    val cardBColor = Color(0xFFFFF9C4) // Light yellow
    val cardResultColor = Color(0xFFC8E6C9) // Light green
    val textPrimaryDark = Color(0xFF222222)
    val textSecondaryDark = Color(0xFF444444)
    val titleAColor = Color(0xFF0D47A1) // dark blue
    val titleBColor = Color(0xFFF57C00) // orange
    val titleResultColor = Color(0xFF388E3C) // dark green

    val borderAColor = Color(0xFF90CAF9) // blue border
    val borderBColor = Color(0xFFFFE082) // yellow border
    val borderResultColor = Color(0xFFA5D6A7) // green border

    val state by component.state.collectAsState()

    val pointA = AzimuthInputNormalizer.parseCoordinate(state.latA, state.lonA)
    val azimuthA = AzimuthInputNormalizer.parseAzimuth(state.azimuthFromA)
    val distance = AzimuthInputNormalizer.parseDistance(state.distanceKm)
    val pointB = AzimuthInputNormalizer.parseCoordinate(state.latB, state.lonB)

    val result = if (pointA != null && azimuthA != null && distance != null && pointB != null) {
        AzimuthCalculatorAPI.calculate(pointA, azimuthA, distance, pointB)
    } else null

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Введіть вихідні дані для розрахунку цілі",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .combinedClickable(onDoubleClick = {
                    component.hideSamples()
                }, onClick = {})
        )

        // Add Sample Selector Card
        AnimatedVisibility(visible = !state.hideSamples) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
            ) {
                Column(
                    Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Оберіть зразок розрахунку",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4527A0)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedSampleText,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7E57C2),
                                unfocusedBorderColor = Color(0xFF7E57C2)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded, onDismissRequest = { expanded = false }) {
                            state.samples.forEach { sample ->
                                DropdownMenuItem(text = { Text(sample.name) }, onClick = {
                                    selectedSampleText = sample.name
                                    component.applySample(sample)
                                    expanded = false
                                })
                            }
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardAColor)
        ) {
            Column(
                Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Точка A (позиція спостерігача)",
                    style = MaterialTheme.typography.titleMedium,
                    color = titleAColor
                )
                OutlinedTextField(
                    value = state.latA,
                    onValueChange = { component.onLatAChange(it) },
                    label = { Text("Широта A", color = textPrimaryDark) },
                    supportingText = {
                        Text(
                            "Введіть широту точки A у форматі 50.2040236", color = textSecondaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("🧭") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textPrimaryDark),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderAColor,
                        unfocusedBorderColor = borderAColor,
                        disabledBorderColor = borderAColor,
                        errorBorderColor = Color.Red
                    )
                )
                OutlinedTextField(
                    value = state.lonA,
                    onValueChange = { component.onLonAChange(it) },
                    label = { Text("Довгота A", color = textPrimaryDark) },
                    supportingText = {
                        Text(
                            "Введіть довготу точки A у форматі 24.3845744",
                            color = textSecondaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("🗺️") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textPrimaryDark),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderAColor,
                        unfocusedBorderColor = borderAColor,
                        disabledBorderColor = borderAColor,
                        errorBorderColor = Color.Red
                    )
                )
                Divider(
                    thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                OutlinedTextField(
                    value = state.azimuthFromA,
                    onValueChange = { component.onAzimuthFromAChange(it) },
                    label = { Text("Азимут з A", color = textPrimaryDark) },
                    supportingText = {
                        Text(
                            "Введіть азимут у градусах (від 0 до 360)", color = textSecondaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("↗️") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textPrimaryDark),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderAColor,
                        unfocusedBorderColor = borderAColor,
                        disabledBorderColor = borderAColor,
                        errorBorderColor = Color.Red
                    )
                )
                OutlinedTextField(
                    value = state.distanceKm,
                    onValueChange = { component.onDistanceKmChange(it) },
                    label = { Text("Відстань до цілі (км)", color = textPrimaryDark) },
                    supportingText = {
                        Text(
                            "Введіть відстань у кілометрах", color = textSecondaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("📏") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textPrimaryDark),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderAColor,
                        unfocusedBorderColor = borderAColor,
                        disabledBorderColor = borderAColor,
                        errorBorderColor = Color.Red
                    )
                )
            }
        }
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBColor)
        ) {
            Column(
                Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Точка B (інша позиція)",
                    style = MaterialTheme.typography.titleMedium,
                    color = titleBColor
                )
                OutlinedTextField(
                    value = state.latB,
                    onValueChange = { component.onLatBChange(it) },
                    label = { Text("Широта B", color = textPrimaryDark) },
                    supportingText = {
                        Text(
                            "Введіть широту точки B у форматі 50.1802326", color = textSecondaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("🧭") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textPrimaryDark),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderBColor,
                        unfocusedBorderColor = borderBColor,
                        disabledBorderColor = borderBColor,
                        errorBorderColor = Color.Red
                    )
                )
                OutlinedTextField(
                    value = state.lonB,
                    onValueChange = { component.onLonBChange(it) },
                    label = { Text("Довгота B", color = textPrimaryDark) },
                    supportingText = {
                        Text(
                            "Введіть довготу точки B у форматі 24.4102277",
                            color = textSecondaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    leadingIcon = { Text("🗺️") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textPrimaryDark),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderBColor,
                        unfocusedBorderColor = borderBColor,
                        disabledBorderColor = borderBColor,
                        errorBorderColor = Color.Red
                    )
                )
            }
        }
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardResultColor)
        ) {
            Column(
                Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Результат розрахунку",
                    style = MaterialTheme.typography.titleMedium,
                    color = titleResultColor
                )
                if (result != null) {
                    ResultScreen(result.target.lat, result.target.lon, result.azimuthFromB)
                } else {
                    Text(
                        "Будь ласка, введіть коректні значення у всі поля.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    latTarget: Double, lonTarget: Double, azimuthFromB: Double, modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coordsText = "%.6f, %.6f".format(latTarget, lonTarget)
    val azimuthText = "%.2f°".format(azimuthFromB)
    Column(modifier = modifier) {
        Text(
            text = "Координати цілі:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1B5E20), // dark green
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Column(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(coordsText))
                    Toast.makeText(context, "Координати скопійовано", Toast.LENGTH_SHORT).show()
                }
                .padding(12.dp)) {
            Text(
                text = "Широта: %.6f".format(latTarget),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222)
            )
            Text(
                text = "Довгота: %.6f".format(lonTarget),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(Натисніть, щоб скопіювати)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF388E3C)
            )
        }
        Text(
            text = "Азимут з B на ціль:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF01579B), // dark blue
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Column(
            modifier = Modifier
                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(azimuthText))
                    Toast.makeText(context, "Азимут скопійовано", Toast.LENGTH_SHORT).show()
                }
                .padding(12.dp)) {
            Text(
                text = azimuthText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(Натисніть, щоб скопіювати)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF01579B)
            )
        }
    }
}
