package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "SelectLocationFragment"
        private const val ZOOM_LEVEL = 16f
        private val defaultLocation = LatLng(-33.852, 151.211) // Sydney
    }

    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var permissionRequestLauncher: ActivityResultLauncher<String?>

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
        registerPermissionRequester()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fab.setOnClickListener {
            selectedMarker?.let {
                onLocationSelected(it.position)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        checkLocationPermission()
        moveToDeviceLocation()

        setMapClickListeners()
        setMapStyle()
    }

    private fun moveMapCamera(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
        updateMapOnLocationEnabled()
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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            permissionRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // using new androidx fragment API to handle permission results
    // no request codes needed anymore, yay!
    private fun registerPermissionRequester() {
        permissionRequestLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    locationPermissionGranted = true
                } else {
                    viewModel.showSnackBar.postValue(getString(R.string.location_required_error))
                }
            }
    }


    private fun onLocationSelected(latLng: LatLng) {
        viewModel.navigationCommand.postValue(NavigationCommand.Back)
        viewModel.showLoading.postValue(true)

        var locationDescription: String? = null
        try {
            locationDescription = Geocoder(requireContext()).getFromLocation(
                latLng.latitude,
                latLng.longitude,
                2
            )[0].locality
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModel.markLocation(latLng, locationDescription)

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
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style, Error: $e")
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
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
