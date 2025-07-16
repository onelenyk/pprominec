package dev.onelenyk.pprominec.presentation.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Helper composable for MVI screens that handles state collection and effect processing
 */
@Composable
fun <Intent : dev.onelenyk.pprominec.presentation.mvi.Intent, State : dev.onelenyk.pprominec.presentation.mvi.State, Effect : dev.onelenyk.pprominec.presentation.mvi.Effect> MviScreen(
    component: MviComponent<Intent, State, Effect>,
    onEffect: (Effect) -> Unit,
    content: @Composable (state: State, dispatch: (Intent) -> Unit) -> Unit,
) {
    val state by component.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Handle effects using Channel
    LaunchedEffect(Unit) {
        component.effect.collectLatest { effect ->
            onEffect(effect)
        }
    }

    // Wrap the suspend dispatch function to be callable from regular callbacks
    val wrappedDispatch: (Intent) -> Unit = { intent ->
        coroutineScope.launch {
            component.dispatch(intent)
        }
    }

    content(state, wrappedDispatch)
}

/**
 * Simplified version for screens that don't need effect handling
 */
@Composable
fun <Intent : dev.onelenyk.pprominec.presentation.mvi.Intent, State : dev.onelenyk.pprominec.presentation.mvi.State, Effect : dev.onelenyk.pprominec.presentation.mvi.Effect> MviScreen(
    component: MviComponent<Intent, State, Effect>,
    content: @Composable (state: State, dispatch: (Intent) -> Unit) -> Unit,
) {
    val state by component.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Wrap the suspend dispatch function to be callable from regular callbacks
    val wrappedDispatch: (Intent) -> Unit = { intent ->
        coroutineScope.launch {
            component.dispatch(intent)
        }
    }

    content(state, wrappedDispatch)
}
