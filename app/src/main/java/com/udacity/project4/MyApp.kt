package com.udacity.project4

import android.app.Application
import com.udacity.project4.locationreminders.data.ReminderDataSource

class MyApp : Application() {

    val dataSource: ReminderDataSource
        get() = ServiceLocator.provideDataSource(this)

    override fun onCreate() {
        super.onCreate()
    }
}
