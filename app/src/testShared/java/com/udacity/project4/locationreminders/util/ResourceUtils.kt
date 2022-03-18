package com.udacity.project4.locationreminders.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider

fun getString(resId: Int): String =
    ApplicationProvider.getApplicationContext<Context>().getString(resId)
