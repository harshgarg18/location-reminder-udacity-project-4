package com.udacity.project4.locationreminders.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.base.SnackBarAction
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.LocationData
import com.udacity.project4.locationreminders.savereminder.LocationInfo
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "SelectLocationFragment"
        private const val ZOOM_LEVEL = 16f
        private const val GOOGLE_MAP_ZOOM_IN_BUTTON = "GoogleMapZoomInButton"
        private val defaultLocation = LatLng(-33.852, 151.211) // Sydney
    }

    override val viewModel: SelectLocationViewModel by viewModels(ownerProducer = { this.requireActivity() }) {
        val app = requireContext().applicationContext as MyApp
        SelectLocationViewModel.Factory(app)
    }

    private lateinit var binding: FragmentSelectLocationBinding


    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var mapView: View? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false

    private var selectedMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_location,
            container,
            false
        )

        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fab.setOnClickListener {
            selectedMarker?.let {
                onLocationSelected(it.position)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapView = mapFragment.view

        setMapClickListeners()
        setMapStyle()

        locationPermissionGranted = hasFineLocationPermission()
        if (!locationPermissionGranted) {
            requestLocationPermissions()
        }
        checkDeviceLocationSettings()
    }

    private fun moveMapCamera(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
        updateMapOnLocationEnabled()
    }

    private fun requestLocationPermissions() {
        requestFineLocationPermission {
            if (it.granted) {
                locationPermissionGranted = true
                checkDeviceLocationSettings()
            } else if (it.showRationale) {
                showPermissionDeniedInfo()
            }
        }
    }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener {
            viewModel.showSnackBarWithAction.postValue(
                SnackBarAction(
                    getString(R.string.location_required_error),
                    getString(android.R.string.ok)
                ) {
                    checkDeviceLocationSettings()
                }
            )
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                registerLocationUpdates()
            }
        }
    }

    private fun registerLocationUpdates() {
        updateMapOnLocationEnabled()
        val locationRequest = LocationRequest.create().apply {
//            interval = 120000 // two minute interval
//            fastestInterval = 120000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                Log.wtf(TAG, "onLocationResult: $location")
                moveMapCamera(LatLng(location.latitude, location.longitude))
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Looper.myLooper()?.let {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, it)
            moveToDeviceLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveToDeviceLocation() {
        if (locationPermissionGranted) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                Log.d(TAG, "onComplete: $it")
                val location = it.result
                if (it.isSuccessful && location != null) {
                    Log.d(TAG, "onComplete success: ${it.result}")
                    moveMapCamera(LatLng(location.latitude, location.longitude))
                } else {
                    Log.d(TAG, "onComplete failure: ${it.exception}")
                    moveMapCamera(defaultLocation)
                }
            }
        } else {
            moveMapCamera(defaultLocation)
        }
    }

    private fun setMapClickListeners() {
        map.setOnMapClickListener { latLng: LatLng ->
            selectedMarker?.remove()
            selectedMarker?.position = latLng
            map.addMarker(getMarker(latLng)).also {
                selectedMarker = it
            }
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        map.setOnPoiClickListener { poi: PointOfInterest ->
            selectedMarker?.remove()
            selectedMarker?.position = poi.latLng
            map.addMarker(getMarker(poi.latLng).title(poi.name)).also {
                selectedMarker = it
            }
            map.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }

    private fun getMarker(latLng: LatLng): MarkerOptions {
        return MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
    }

    private fun onLocationSelected(latLng: LatLng) {
        viewModel.showLoading.postValue(true)

        val locationDescription: String =
            try {
                Geocoder(requireContext()).getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    2
                )[0].locality
            } catch (e: Exception) {
                String.format(
                    Locale.getDefault(),
                    getString(R.string.lat_long_snippet),
                    latLng.latitude,
                    latLng.longitude
                )
            }

        LocationData.locationInfo = LocationInfo(latLng, locationDescription)
        LocationData.fromMap = true

        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                SelectLocationFragmentDirections.saveLocation()
            )
        )
        viewModel.showLoading.postValue(false)
    }

    private fun setMapStyle() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_styles)
            )
            if (!success) {
                Log.e(TAG, "Styling parse failed")
            }
            configureZoomControls()
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style, Error: $e")
        }
    }

    private fun configureZoomControls() {
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isZoomControlsEnabled = true
        mapView?.let {
            val zoomIn: View = it.findViewWithTag(GOOGLE_MAP_ZOOM_IN_BUTTON)
            val zoomParent = zoomIn.parent as View
            val layoutParams = zoomParent.layoutParams as RelativeLayout.LayoutParams
            layoutParams.bottomMargin = 400
            zoomParent.layoutParams = layoutParams
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateMapOnLocationEnabled() {
        map.isMyLocationEnabled = locationPermissionGranted
        map.uiSettings.isMyLocationButtonEnabled = locationPermissionGranted
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> setMapType(GoogleMap.MAP_TYPE_NORMAL)
        R.id.hybrid_map -> setMapType(GoogleMap.MAP_TYPE_HYBRID)
        R.id.satellite_map -> setMapType(GoogleMap.MAP_TYPE_SATELLITE)
        R.id.terrain_map -> setMapType(GoogleMap.MAP_TYPE_TERRAIN)
        else -> super.onOptionsItemSelected(item)
    }

    private val setMapType: (Int) -> Boolean = {
        map.mapType = it
        true
    }
}
