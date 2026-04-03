package com.auraflow.garden.di

import android.content.Context

fun initKoinAndroid(context: Context) {
    initKoin(platformModules = listOf(androidModule(context)))
}
