package dev.onelenyk.pprominec.presentation.ui.screens.sample

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.mvi.MviScreen
import dev.onelenyk.pprominec.presentation.mvi.SampleEffect
import dev.onelenyk.pprominec.presentation.mvi.SampleIntent
import dev.onelenyk.pprominec.presentation.mvi.DefaultSampleMviComponent
import dev.onelenyk.pprominec.presentation.mvi.SampleState
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar

/**
 * Sample screen demonstrating MVI pattern usage
 */
@Composable
fun SampleMviScreen(component: DefaultSampleMviComponent) {
    val context = LocalContext.current
    
    MviScreen(
        component = component,
        onEffect = { effect ->
            when (effect) {
                is SampleEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SampleEffect.NavigateToDetails -> {
                    Toast.makeText(context, "Navigate to details: ${effect.count}", Toast.LENGTH_SHORT).show()
                }
                is SampleEffect.ShowError -> {
                    Toast.makeText(context, "Error: ${effect.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    ) { state, dispatch ->
        AppScreen(
            toolbar = { AppToolbar(title = "Sample MVI Counter") },
            content = {
                SampleMviContent(
                    state = state,
                    dispatch = dispatch
                )
            }
        )
    }
}

@Composable
fun SampleMviContent(
    state: SampleState,
    dispatch: (SampleIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Counter display
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Counter",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = state.count.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
                
                if (state.lastAction.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last action: ${state.lastAction}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Control buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Controls",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { dispatch(SampleIntent.Decrement) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-")
                    }
                    
                    Button(
                        onClick = { dispatch(SampleIntent.Increment) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+")
                    }
                }
                
                Button(
                    onClick = { dispatch(SampleIntent.Reset) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset")
                }
                
                Button(
                    onClick = { dispatch(SampleIntent.ShowRandomNumber) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Random Number")
                }
                
                // Example of setting a specific value
                Button(
                    onClick = { dispatch(SampleIntent.SetValue(42)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set to 42")
                }
            }
        }
        
        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "MVI Pattern Demo",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This demonstrates the MVI pattern with:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "• Intents: User actions (buttons, etc.)",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "• State: Current UI state (count, loading, etc.)",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "• Effects: One-time events (toasts, navigation)",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "• Long-running operations (random number generation)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 