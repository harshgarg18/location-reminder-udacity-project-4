package com.udacity.project4.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val viewModel: BaseViewModel

    companion object {
        val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        private const val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION

        @RequiresApi(Build.VERSION_CODES.Q)
        private const val backgroundLocationPermission =
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }

    private lateinit var fineLocationPermissionRequestLauncher: ActivityResultLauncher<String>
    private lateinit var backgroundLocationPermissionRequestLauncher: ActivityResultLauncher<String>
    private lateinit var permissionResultHandler: (PermissionResult) -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissionRequestLauncher()
    }

    override fun onStart() {
        super.onStart()
        viewModel.showErrorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        viewModel.showToast.observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        viewModel.showSnackBar.observe(viewLifecycleOwner) {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        }
        viewModel.showSnackBarInt.observe(viewLifecycleOwner) {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        }
        viewModel.showSnackBarWithAction.observe(viewLifecycleOwner) {
            Snackbar
                .make(this.requireView(), it.message, Snackbar.LENGTH_INDEFINITE)
                .setAction(it.actionText, it.listener)
                .show()
        }

        viewModel.navigationCommand.observe(viewLifecycleOwner) { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        }
    }

    protected fun hasFineLocationPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(),
            fineLocationPermission
        )
    }

    protected fun hasBackgroundLocationPermission(): Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                backgroundLocationPermission
            )
        } else {
            true
        }
    }

    @SuppressLint("InlinedApi")
    private fun registerPermissionRequestLauncher() {
        fineLocationPermissionRequestLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                permissionResultHandler(
                    PermissionResult(
                        it,
                        !it && shouldShowRequestPermissionRationale(fineLocationPermission)
                    )
                )
            }
        backgroundLocationPermissionRequestLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                permissionResultHandler(
                    PermissionResult(
                        it,
                        !it && shouldShowRequestPermissionRationale(backgroundLocationPermission)
                    )
                )
            }
    }

    protected fun requestFineLocationPermission(handler: (PermissionResult) -> Unit) {
        permissionResultHandler = handler
        fineLocationPermissionRequestLauncher.launch(fineLocationPermission)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun requestBackgroundLocationPermission(handler: (PermissionResult) -> Unit) {
        permissionResultHandler = handler
        backgroundLocationPermissionRequestLauncher.launch(backgroundLocationPermission)
    }

    protected fun showPermissionDeniedInfo() {
        viewModel.showSnackBarWithAction.postValue(
            SnackBarAction(
                getString(R.string.permission_denied_explanation),
                getString(R.string.settings)
            ) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        )
    }


    data class PermissionResult(
        val granted: Boolean,
        val showRationale: Boolean
    )
}
