package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled

class SaveReminderFragment : BaseFragment() {

    companion object {
        private const val TAG = "SaveReminderFragment"
    }

    override val viewModel: SaveReminderViewModel by viewModels(ownerProducer = {
        requireActivity()
    }) {
        val app = requireContext().applicationContext as MyApp
        SaveReminderViewModel.Factory(app, app.dataSource)
    }

    private var isExitingBack = true
    private var hasBackgroundLocationPermission = false
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            viewModel.navigationCommand.value =
                NavigationCommand.To(
                    SaveReminderFragmentDirections
                        .actionSaveReminderFragmentToSelectLocationFragment()
                )
            isExitingBack = false
        }

        LocationData.locationInfo?.let {
            viewModel.markLocation(it.latLng, it.locationDescription)
        }
        if (LocationData.fromMap) {
            isExitingBack = true
        }

        hasBackgroundLocationPermission = hasBackgroundLocationPermission()

        binding.saveReminder.setOnClickListener {
            if (!hasBackgroundLocationPermission) {
                requestBackgroundLocationPermission()
                return@setOnClickListener
            }
            val title = viewModel.reminderTitle.value
            val description = viewModel.reminderDescription.value
            val location = viewModel.reminderSelectedLocationStr.value
            val latitude = viewModel.latitude.value
            val longitude = viewModel.longitude.value
            val radius = viewModel.reminderRadius.value?.toDouble()

            val reminderDataItem =
                ReminderDataItem(title, description, location, latitude, longitude, radius)
            if (viewModel.validateAndSaveReminder(reminderDataItem)) {
                addGeofence(reminderDataItem)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun requestBackgroundLocationPermission() {
        if (runningQOrLater) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.background_permission_explanation)
                .setPositiveButton(R.string.settings) { _, _ ->
                    requestBackgroundLocationPermission {
                        if (it.granted) {
                            hasBackgroundLocationPermission = true
                        } else if (it.showRationale) {
                            showPermissionDeniedInfo()
                        }
                    }
                }
                .setNegativeButton(R.string.deny) { dialog, _ ->
                    dialog.dismiss()
                    showPermissionDeniedInfo()
                }
                .create()
                .show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderData: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                reminderData.radius?.toFloat()!!
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val geofencingClient = LocationServices.getGeofencingClient(requireContext())

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
            action = Constants.ACTION_GEOFENCE_EVENT
        }

        val intentFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, intentFlag)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent).run {
            addOnFailureListener {
                viewModel.showErrorMessage.postValue(getString(R.string.error_adding_geofence))
                Log.wtf(TAG, it)
            }
            addOnSuccessListener { _: Void? ->
                Log.d(TAG, "Geofence for reminder $reminderData added successfully")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isExitingBack) {
            viewModel.onClear()
        }
    }
}
