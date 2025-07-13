@file:OptIn(ExperimentalMaterial3Api::class)

package dev.onelenyk.pprominec.presentation.ui.screens.main

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.onelenyk.pprominec.bussines.AzimuthCalculationResult
import dev.onelenyk.pprominec.bussines.AzimuthCalculatorAPI
import dev.onelenyk.pprominec.bussines.AzimuthInputNormalizer
import dev.onelenyk.pprominec.presentation.components.main.MainComponent
import dev.onelenyk.pprominec.presentation.components.main.MainState
import dev.onelenyk.pprominec.presentation.components.main.Sample
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.ui.components.AppTextField
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar
import dev.onelenyk.pprominec.presentation.components.main.OutputData


@Composable
fun MainScreen(component: MainComponent) {
    AppScreen(
        showInnerPadding = true,
        toolbar = { AppToolbar(title = "Калькулятор") },
        content = {
            InputAndResultScreen(
                modifier = Modifier,
                component = component,
            )
        },
    )
}

@Composable
fun ProminchykCalculatorCard(
    result: OutputData?,
    onCalculate: () -> Unit,
    onEdit: () -> Unit,
    isResultMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("🎯", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "Prominchyk Calculator — Target Azimuth Finder",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!isResultMode) {
                Text(
                    text = "Дано:\n- Координати точки A\n- Азимут з A на ціль\n- Відстань від A до цілі\n- Координати точки B\n\nЗавдання:\n1. Обчислити координати цілі з точки A.\n2. Знайти азимут з точки B на ціль.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.Button(
                    onClick = { onCalculate(); onModeChange(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Обчислити координати")
                }
            } else {
                if (result != null) {
                    Text(
                        text = "Координати цілі:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Широта: %.6f".format(result.targetPosition?.lat ?: 0.0),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF222222)
                    )
                    Text(
                        text = "Довгота: %.6f".format(result.targetPosition?.lon ?: 0.0),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF222222)
                    )
                } else {
                    Text(
                        text = "Будь ласка, введіть коректні значення у всі поля.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = { onEdit(); onModeChange(false) }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Редагувати вхідні дані")
                }
            }
        }
    }
}

@Composable
fun TargetInputCard(
    latA: String,
    lonA: String,
    azimuth: String,
    distance: String,
    onLatAChange: (String) -> Unit,
    onLonAChange: (String) -> Unit,
    onAzimuthChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "🧮",
                    modifier = Modifier.padding(end = 8.dp),
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
                Text(
                    text = "Обчислити координати цілі",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(Color(0xFF1976D2), RoundedCornerShape(12.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                        contentDescription = "Налаштування",
                        tint = Color.White
                    )
                }
            }
            // Поля у два рядки по дві колонки
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = latA,
                        onValueChange = onLatAChange,
                        label = { Text("Широта A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lonA,
                        onValueChange = onLonAChange,
                        label = { Text("Довгота A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = azimuth,
                        onValueChange = onAzimuthChange,
                        label = { Text("Азимут") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = distance,
                        onValueChange = onDistanceChange,
                        label = { Text("Відстань") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TargetInputCard_Variant2(
    latA: String,
    lonA: String,
    azimuth: String,
    distance: String,
    onLatAChange: (String) -> Unit,
    onLonAChange: (String) -> Unit,
    onAzimuthChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    text = "Обчислити координати цілі",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF222B45)
                )
                androidx.compose.material3.IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(Color(0xFF1976D2), RoundedCornerShape(14.dp))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Налаштування",
                        tint = Color.White
                    )
                }
            }
            // Поля у два рядки по дві колонки з покращеним стилем
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = latA,
                        onValueChange = onLatAChange,
                        label = { Text("Широта A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lonA,
                        onValueChange = onLonAChange,
                        label = { Text("Довгота A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = azimuth,
                        onValueChange = onAzimuthChange,
                        label = { Text("Азимут") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = distance,
                        onValueChange = onDistanceChange,
                        label = { Text("Відстань") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TargetInputCard_Variant3(
    latA: String,
    lonA: String,
    azimuth: String,
    distance: String,
    onLatAChange: (String) -> Unit,
    onLonAChange: (String) -> Unit,
    onAzimuthChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = null,
                    tint = Color(0xFF512DA8),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    text = "Обчислити координати цілі",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF222B45)
                )
                androidx.compose.material3.IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(Color(0xFF1976D2), RoundedCornerShape(16.dp))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Налаштування",
                        tint = Color.White
                    )
                }
            }
            // Поля у два рядки по дві колонки з ще більшим spacing, тінню, підписами
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = latA,
                        onValueChange = onLatAChange,
                        label = { Text("Широта A", color = Color(0xFF512DA8)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF512DA8),
                            unfocusedBorderColor = Color(0xFFB39DDB)
                        )
                    )
                    OutlinedTextField(
                        value = lonA,
                        onValueChange = onLonAChange,
                        label = { Text("Довгота A", color = Color(0xFF512DA8)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF512DA8),
                            unfocusedBorderColor = Color(0xFFB39DDB)
                        )
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = azimuth,
                        onValueChange = onAzimuthChange,
                        label = { Text("Азимут", color = Color(0xFF1976D2)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFF90CAF9)
                        )
                    )
                    OutlinedTextField(
                        value = distance,
                        onValueChange = onDistanceChange,
                        label = { Text("Відстань", color = Color(0xFF1976D2)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFF90CAF9)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InputAndResultScreen(
    modifier: Modifier = Modifier,
    component: MainComponent,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSampleText by remember { mutableStateOf("Оберіть зразок...") }
    val state by component.state.collectAsState()

    var isResultMode by remember { mutableStateOf(false) }
    var isRenderModeA by remember { mutableStateOf(false) }
    var isRenderModeB by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IntroCard()
        Spacer(modifier = Modifier.height(8.dp))

        TargetCalculationCard(
            latA = state.inputData.latA,
            lonA = state.inputData.lonA,
            azimuth = state.inputData.azimuthFromA,
            distance = state.inputData.distanceKm,
            onLatAChange = component::onLatAChange,
            onLonAChange = component::onLonAChange,
            onAzimuthChange = component::onAzimuthFromAChange,
            onDistanceChange = component::onDistanceKmChange,
            onSettingsClick = { /* TODO: handle settings click */ },
            isRenderMode = isRenderModeA,
            result = state.outputData,
            onModeChange = { isRenderModeA = it })

        Spacer(modifier = Modifier.height(8.dp))

        // Point B input card using Variant 4
        ObservationPointCard(
            latB = state.inputData.latB,
            lonB = state.inputData.lonB,
            onLatBChange = component::onLatBChange,
            onLonBChange = component::onLonBChange,
            onSettingsClick = { /* TODO: handle settings click */ },
            isRenderMode = isRenderModeB,
            result = state.outputData,
            onModeChange = { isRenderModeB = it })

        Spacer(modifier = Modifier.height(8.dp))
//        SampleSelectorCard(
//            expanded = expanded,
//            onExpandedChange = { expanded = it },
//            selectedSampleText = selectedSampleText,
//            onSampleSelected = { sample ->
//                selectedSampleText = sample.name
//                component.applySample(sample)
//                expanded = false
//            },
//            samples = state.samples,
//            hideSamples = state.hideSamples,
//            onHideSamples = component::hideSamples,
//        )
        // PointACard(state = state, component = component) // Hidden
        // PointBCard(state = state, component = component) // Hidden
        ResultCard(result = state.outputData)
    }
}

@Composable
fun IntroCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier,
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column {
                Text(
                    text = "Введіть вихідні дані для розрахунку цілі",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Вкажіть координати спостерігача, напрямок (азимут) та відстань до цілі. Це дозволить точно розрахувати положення цілі на місцевості.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun SampleSelectorCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedSampleText: String,
    onSampleSelected: (sample: Sample) -> Unit,
    samples: List<Sample>,
    hideSamples: Boolean,
    onHideSamples: () -> Unit,
) {
    AnimatedVisibility(visible = !hideSamples) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Оберіть зразок розрахунку",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4527A0),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.fillMaxWidth(),
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
                            unfocusedBorderColor = Color(0xFF7E57C2),
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { onExpandedChange(false) },
                    ) {
                        samples.forEach { sample ->
                            DropdownMenuItem(text = { Text(sample.name) }, onClick = {
                                onSampleSelected(sample)
                            })
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ResultCard(result: OutputData?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Результат розрахунку",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (result != null) {
                ResultScreen(result.targetPosition?.lat ?: 0.0, result.targetPosition?.lon ?: 0.0, result.azimuthFromB)
            } else {
                Text(
                    "Будь ласка, введіть коректні значення у всі поля.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun ResultScreen(
    latTarget: Double,
    lonTarget: Double,
    azimuthFromB: Double?,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coordsText = "%.6f, %.6f".format(latTarget, lonTarget)
    val azimuthText = "%.2f°".format(azimuthFromB ?: 0.0)
    Column(modifier = modifier) {
        Text(
            text = "Координати цілі:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1B5E20), // dark green
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Column(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(coordsText))
                    Toast.makeText(
                        context,
                        "Координати скопійовано",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                .padding(12.dp),
        ) {
            Text(
                text = "Широта: %.6f".format(latTarget),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
            )
            Text(
                text = "Довгота: %.6f".format(lonTarget),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(Натисніть, щоб скопіювати)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF388E3C),
            )
        }
        Text(
            text = "Азимут з B на ціль:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF01579B), // dark blue
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Column(
            modifier = Modifier
                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(azimuthText))
                    Toast.makeText(context, "Азимут скопійовано", Toast.LENGTH_SHORT).show()
                }
                .padding(12.dp),
        ) {
            Text(
                text = azimuthText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(Натисніть, щоб скопіювати)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF01579B),
            )
        }
    }
}

@Composable
fun TargetCalculationCard(
    latA: String,
    lonA: String,
    azimuth: String,
    distance: String,
    onLatAChange: (String) -> Unit,
    onLonAChange: (String) -> Unit,
    onAzimuthChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSettingsClick: () -> Unit = {}, // not used anymore, but keep for compatibility
    isRenderMode: Boolean,
    result: OutputData?,
    onModeChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Обчислити координати цілі",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                androidx.compose.material3.IconButton(
                    onClick = { onModeChange(!isRenderMode) },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (!isRenderMode) Icons.Filled.Build else Icons.Default.Face,
                        contentDescription = if (!isRenderMode) "Переглянути результат" else "Редагувати вхідні дані",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
            if (!isRenderMode) {
                // Use AppTextField instead of OutlinedTextField
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Coordinates section with border and icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Coordinate fields
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = latA,
                                    onValueChange = onLatAChange,
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Широта A:",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDD2D") } // 🧭
                                )
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = lonA,
                                    onValueChange = onLonAChange,
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Довгота А:",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDDFA\uFE0F") } // 🗺️
                                )
                            }

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )
                            // Small icon on the right
                            LocationButton(
                                onClick = { /* TODO: Open map screen with location pin */ },
                                modifier = Modifier
                            )
                        }
                    }
                    // Azimuth and distance fields
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppTextField(
                            modifier = Modifier.weight(1f),
                            value = azimuth,
                            onValueChange = onAzimuthChange,
                            maxLines = 1,
                            isRequired = true,
                            label = "Азимут:",
                            placeholder = "80",
                            leftContent = { Text("\u2197\uFE0F") } // ↗️
                        )
                        AppTextField(
                            modifier = Modifier.weight(1f),
                            value = distance,
                            onValueChange = onDistanceChange,
                            maxLines = 1,
                            isRequired = false,
                            label = "Відстань:",
                            placeholder = "50",
                            leftContent = { Text("\uD83D\uDCCF") } // 📏
                        )
                    }
                }
            } else {
                // Render mode: show result in the same card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Результат розрахунку",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (result != null) {
                        Text(
                            "Широта: %.6f".format(result.targetPosition?.lat ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Довгота: %.6f".format(result.targetPosition?.lon ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Азимут з B на ціль: %.2f°".format(result.azimuthFromB ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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
}

@Composable
fun ObservationPointCard(
    latB: String,
    lonB: String,
    onLatBChange: (String) -> Unit,
    onLonBChange: (String) -> Unit,
    onSettingsClick: () -> Unit = {}, // not used anymore, but keep for compatibility
    isRenderMode: Boolean,
    result: OutputData?,
    onModeChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Точка B (інша позиція)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                androidx.compose.material3.IconButton(
                    onClick = { onModeChange(!isRenderMode) },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (!isRenderMode) Icons.Filled.Build else Icons.Default.Face,
                        contentDescription = if (!isRenderMode) "Переглянути результат" else "Редагувати вхідні дані",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
            if (!isRenderMode) {
                // Use AppTextField instead of OutlinedTextField
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Coordinates section with border and icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Coordinate fields
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = latB,
                                    onValueChange = onLatBChange,
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Широта B:",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDD2D") } // 🧭
                                )
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = lonB,
                                    onValueChange = onLonBChange,
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Довгота B:",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDDFA\uFE0F") } // 🗺️
                                )
                            }

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )
                            // Small icon on the right
                            LocationButton(
                                onClick = { /* TODO: Open map screen with location pin */ },
                                modifier = Modifier
                            )
                        }
                    }
                }
            } else {
                // Render mode: show result in the same card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Координати точки B",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (result != null) {
                        Text(
                            "Широта: %.6f".format(result.targetPosition?.lat ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Довгота: %.6f".format(result.targetPosition?.lon ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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
}

@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.onPrimary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    onClick: () -> Unit,
) {
    Box(


        modifier = modifier
            //  .size(48.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .background(backgroundColor, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Відкрити карту з локацією",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}




