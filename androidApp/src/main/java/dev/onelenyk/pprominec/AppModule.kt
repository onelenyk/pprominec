package dev.onelenyk.pprominec

import MapFilesRepository
import android.app.Application
import dev.onelenyk.pprominec.presentation.components.main.MapFileStorage
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val appModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { MapFilesRepository(androidContext()) }
    single { MapFileStorage(androidContext()) }
    single { PermissionsManager() }
} 