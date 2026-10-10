package com.app.relatosfutbolerosnd_20

import android.app.Application
import com.app.relatosfutbolerosnd_20.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FutbolLiveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@FutbolLiveApp)
            modules(appModule)
        }
    }
}