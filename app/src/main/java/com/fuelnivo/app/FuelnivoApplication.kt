package com.fuelnivo.app

import android.app.Application
import com.fuelnivo.app.data.FuelnivoRepository

/**
 * Application class that owns the single local repository instance. No network,
 * background work, or third-party SDK initialization happens here.
 */
class FuelnivoApplication : Application() {

    val repository: FuelnivoRepository by lazy { FuelnivoRepository(this) }
}
