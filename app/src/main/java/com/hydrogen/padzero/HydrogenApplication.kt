package com.hydrogen.padzero

import android.app.Application

class HydrogenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
