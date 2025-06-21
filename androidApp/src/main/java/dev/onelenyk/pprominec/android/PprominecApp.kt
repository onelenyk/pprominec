package dev.onelenyk.pprominec.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PprominecApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PprominecApp)
            // Add your Koin modules here, e.g.:
            // modules(appModule)
        }
    }
}

