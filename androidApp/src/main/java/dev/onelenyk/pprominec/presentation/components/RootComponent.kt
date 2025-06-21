package dev.onelenyk.pprominec.presentation.components


import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.onelenyk.pprominec.presentation.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface RootComponent {
    val menuNavigationState: Value<ChildStack<RootConfig, RootEntry>>


    sealed class RootEntry {
        data class Main(val component: MainComponent) : RootEntry()
    }

    @Serializable
    sealed class RootConfig {
        @Serializable
        data object Main : RootConfig()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val menuNavigation = StackNavigation<RootComponent.RootConfig>()
    private val _menuNavigationState: Value<ChildStack<RootComponent.RootConfig, RootComponent.RootEntry>> =
        childStack(
            source = menuNavigation,
            serializer = RootComponent.RootConfig.serializer(),
            initialConfiguration = RootComponent.RootConfig.Main,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override val menuNavigationState: Value<ChildStack<RootComponent.RootConfig, RootComponent.RootEntry>> =
        _menuNavigationState

    private fun createChild(
        config: RootComponent.RootConfig,
        context: ComponentContext
    ): RootComponent.RootEntry {
        return when (config) {
            RootComponent.RootConfig.Main -> {
                val component = DefaultMainComponent(context)

                return RootComponent.RootEntry.Main(component)
            }
        }
    }

    init {
        coroutineScope.launch {
            //  menuNavigation.pushCatch(RootComponent.RootConfig.Hello)
        }
    }
}