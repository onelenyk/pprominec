package dev.onelenyk.pprominec.presentation.mvi

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

// Sample types for demonstration
sealed class SampleIntent : Intent {
    data object Increment : SampleIntent()
    data object Decrement : SampleIntent()
    data class SetValue(val value: Int) : SampleIntent()
    data object Reset : SampleIntent()
    data object ShowRandomNumber : SampleIntent()
}

data class SampleState(
    val count: Int = 0,
    val isLoading: Boolean = false,
    val lastAction: String = ""
) : State

sealed class SampleEffect : Effect {
    data class ShowToast(val message: String) : SampleEffect()
    data class NavigateToDetails(val count: Int) : SampleEffect()
    data class ShowError(val error: String) : SampleEffect()
}

interface SampleMviComponent

class DefaultSampleMviComponent(
    componentContext: ComponentContext,
    private val initialValue: Int = 0
) : MviComponent<SampleIntent, SampleState, SampleEffect>,SampleMviComponent,  ComponentContext by componentContext {

    override val _state = MutableStateFlow(SampleState(count = initialValue))
    override val _effect = Channel<SampleEffect>(Channel.BUFFERED)

    override suspend fun processIntent(intent: SampleIntent) {
        when (intent) {
            is SampleIntent.Increment -> {
                val currentState = _state.value
                val newCount = currentState.count + 1
                val newState = currentState.copy(
                    count = newCount,
                    lastAction = "Incremented"
                )
                updateState(newState)
            }

            is SampleIntent.Decrement -> {
                val currentState = _state.value
                val newCount = currentState.count - 1
                val newState = currentState.copy(
                    count = newCount,
                    lastAction = "Decremented"
                )
                updateState(newState)
            }

            is SampleIntent.SetValue -> {
                val currentState = _state.value
                val newState = currentState.copy(
                    count = intent.value,
                    lastAction = "Set to ${intent.value}"
                )
                updateState(newState)
            }

            is SampleIntent.Reset -> {
                val currentState = _state.value
                val newState = currentState.copy(
                    count = 0,
                    lastAction = "Reset"
                )
                updateState(newState)
                emitEffect(SampleEffect.ShowToast("Counter reset to 0"))
            }

            is SampleIntent.ShowRandomNumber -> {
                val currentState = _state.value
                // Set loading state first
                updateState(
                    currentState.copy(
                        isLoading = true,
                        lastAction = "Generating random number..."
                    )
                )

                // Simulate a long-running operation
                kotlinx.coroutines.delay(1000)

                val randomNumber = (1..100).random()
                val newState = _state.value.copy(
                    count = randomNumber,
                    lastAction = "Random number generated",
                    isLoading = false
                )
                updateState(newState)
                emitEffect(SampleEffect.ShowToast("Random number: $randomNumber"))
            }
        }
    }
}
