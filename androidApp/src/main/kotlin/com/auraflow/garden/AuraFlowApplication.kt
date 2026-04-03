package com.auraflow.garden

import android.app.Application
import com.auraflow.garden.di.initKoinAndroid

class AuraFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
