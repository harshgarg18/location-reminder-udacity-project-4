package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        val authProviders = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
    }

    private lateinit var firebaseAuthLauncher: ActivityResultLauncher<Intent>
    private lateinit var authMethodPickerLayout: AuthMethodPickerLayout
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityAuthenticationBinding>(
            this,
            R.layout.activity_authentication
        )

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            navigateToRemindersScreen()
            return
        }

        registerFirebaseAuthResultLauncher()
        binding.loginButton.setOnClickListener {
            launchAuthFlow()
        }

        setupAuthMethodPickerLayout()
    }


    // using new androidx activity API to handle permission results
    // no request codes needed anymore, yay!
    private fun registerFirebaseAuthResultLauncher() {
        firebaseAuthLauncher =
            registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
                val response = it.idpResponse
                if (it.resultCode == Activity.RESULT_OK) {
                    // Successfully signed in user.
                    Log.i(
                        TAG,
                        "Successfully signed in user " +
                                "${firebaseAuth.currentUser?.displayName}!"
                    )
                    navigateToRemindersScreen()
                } else {
                    // Sign in failed. If response is null the user canceled the sign-in flow using
                    // the back button. Otherwise check response.getError().getErrorCode() and handle
                    // the error.
                    when {
                        response == null -> {
                            Log.i(TAG, "Sign In Cancelled")
                        }
                        response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
                            Log.i(TAG, "No Internet")
                        }
                        else -> {
                            Log.i(TAG, "Sign in unsuccessful ${response.error}")
                        }
                    }
                }
            }
    }

    private fun setupAuthMethodPickerLayout() {
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    private fun launchAuthFlow() {
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.AppTheme)
            .setAvailableProviders(authProviders)
//            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .build()
        firebaseAuthLauncher.launch(intent)
    }

    private fun navigateToRemindersScreen() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }
}
