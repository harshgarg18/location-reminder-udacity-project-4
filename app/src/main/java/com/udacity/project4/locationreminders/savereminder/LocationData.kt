package com.udacity.project4.locationreminders.savereminder

class LocationData {
    companion object {
        var locationInfo: LocationInfo? = null
        var fromMap: Boolean = false

        fun reset() {
            locationInfo = null
            fromMap = false
        }
    }
}
