package dev.onelenyk.pprominec.presentation.components.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.onelenyk.pprominec.presentation.components.bottom_nav.BottomNavComponent
import dev.onelenyk.pprominec.presentation.components.bottom_nav.DefaultBottomNavComponent
import dev.onelenyk.pprominec.presentation.components.permissions.DefaultPermissionsComponent
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsComponent
import kotlinx.serialization.Serializable

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    fun showPermissionsScreen()

    sealed class Child {
        data class BottomNav(val component: BottomNavComponent) : Child()
        data class Permissions(val component: PermissionsComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.BottomNav,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.BottomNav -> RootComponent.Child.BottomNav(
                DefaultBottomNavComponent(
                    componentContext
                )
            )
            is Config.Permissions -> RootComponent.Child.Permissions(
                DefaultPermissionsComponent(componentContext)
            )
        }

    override fun showPermissionsScreen() {
        navigation.bringToFront(Config.Permissions)
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object BottomNav : Config()
        @Serializable
        data object Permissions : Config()
    }
} 