package io.neoply.dappsample

import android.app.Application
import timber.log.Timber

class DappApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}