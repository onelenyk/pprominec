package dev.onelenyk.pprominec.presentation.components.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import dev.onelenyk.pprominec.presentation.components.bottomnav.BottomNavComponent
import dev.onelenyk.pprominec.presentation.components.bottomnav.DefaultBottomNavComponent
import dev.onelenyk.pprominec.presentation.components.main.DefaultMapSettingsComponent
import dev.onelenyk.pprominec.presentation.components.main.FileManager
import dev.onelenyk.pprominec.presentation.components.main.MapSettingsComponent
import dev.onelenyk.pprominec.presentation.components.permissions.DefaultPermissionsComponent
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsComponent
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsManager
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.getKoin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    fun showPermissionsScreen()
    fun showMapSettingsScreen()

    sealed class Child {
        data class BottomNav(val component: BottomNavComponent) : Child()

        data class Permissions(val component: PermissionsComponent) : Child()

        data class MapSettings(val component: MapSettingsComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()
    private val permissionsManager: PermissionsManager = getKoin().get()
    private val fileManager: FileManager = getKoin().get()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.BottomNav,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: Config,
        componentContext: ComponentContext,
    ): RootComponent.Child =
        when (config) {
            is Config.BottomNav ->
                RootComponent.Child.BottomNav(
                    DefaultBottomNavComponent(
                        componentContext,
                        onPermissionsClicked = { showPermissionsScreen() },
                        onMapSettingsClicked = { showMapSettingsScreen() },
                    ),
                )

            is Config.Permissions ->
                RootComponent.Child.Permissions(
                    DefaultPermissionsComponent(componentContext, permissionsManager, onBack = {
                        navigation.pop()
                    }),
                )

            is Config.MapSettings ->
                RootComponent.Child.MapSettings(
                    DefaultMapSettingsComponent(
                        componentContext,
                        onBack = { navigation.pop() },
                    ),
                )
        }

    override fun showPermissionsScreen() {
        navigation.bringToFront(Config.Permissions)
    }

    override fun showMapSettingsScreen() {
        navigation.bringToFront(Config.MapSettings)
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object BottomNav : Config()

        @Serializable
        data object Permissions : Config()

        @Serializable
        data object MapSettings : Config()
    }
}
