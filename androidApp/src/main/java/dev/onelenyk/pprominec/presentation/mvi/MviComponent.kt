package dev.onelenyk.pprominec.presentation.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Core MVI interfaces for the application.
 *
 * MVI (Model-View-Intent) architecture consists of:
 * - Intent: User actions/events that come from the UI
 * - State: The current state of the screen/component
 * - Effect: One-time events that should be handled (navigation, toasts, etc.)
 */

/**
 * Represents a user action or system event that can change the state
 */
interface Intent

/**
 * Represents the current state of a screen/component
 */
interface State

/**
 * Represents one-time events that should be handled (navigation, toasts, etc.)
 */
interface Effect

/**
 * MVI component interface with default implementations
 */
interface MviComponent<Intent : dev.onelenyk.pprominec.presentation.mvi.Intent, State : dev.onelenyk.pprominec.presentation.mvi.State, Effect : dev.onelenyk.pprominec.presentation.mvi.Effect> {
    val _state: MutableStateFlow<State>
    val state: StateFlow<State>
        get() = _state.asStateFlow()

    val _effect: Channel<Effect>
    val effect: Flow<Effect>
        get() = _effect.receiveAsFlow()

    // Default implementation for dispatch
    suspend fun dispatch(intent: Intent) {
        processIntent(intent)
    }

    // Helper functions with default implementations
    suspend fun processIntent(intent: Intent)

    fun updateState(newState: State) {
        _state.value = newState
    }

    suspend fun emitEffect(effect: Effect) {
        _effect.send(effect)
    }
}
