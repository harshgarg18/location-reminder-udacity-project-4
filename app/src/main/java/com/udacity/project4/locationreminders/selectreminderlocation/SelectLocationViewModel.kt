package com.udacity.project4.locationreminders.selectreminderlocation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.project4.base.BaseViewModel

class SelectLocationViewModel(val app: Application): BaseViewModel(app) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val app: Application) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            (SelectLocationViewModel(app) as T)
    }
}
