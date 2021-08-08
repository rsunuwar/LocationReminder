package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: ActivityAuthenticationBinding

    companion object {
        const val TAG = "AuthenticationActivity"        //use for logging
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        //this checks if the user has already logged in, if so go to the RemindersActivity
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if(auth.currentUser != null) {
            Timber.i("Already signed in!!")

            //navigate straight to the reminders section    THIS IS HOW WE GET TO THE REMINDERS SCREEN
            startActivity(Intent(this, RemindersActivity::class.java))
        }
        else {
            //If a user has not logged in then launch sign in flow
            binding.login.setOnClickListener { launchSignInFlow() }
        }
    }

    private fun launchSignInFlow() {
        //this is used to set up the list of loggin providers e.g. via email, Facebook, Google etc.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //Create and launch sign-in intent. We listen to the response of this activity with the SIGN_IN_RESULT_CODE
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(false, true)     //this is used for testing only
                .setAvailableProviders(
                    providers
                ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                //Successfully signed in user
                observeAuthenticationState()
                //using timber
               Log.i(TAG,"Successful sign in! ${FirebaseAuth.getInstance().currentUser?.displayName}")
            } else {
              Log.i(TAG,"Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun observeAuthenticationState() {

        //we use the view model to decide what happens on the login button press
        viewModel.authenticationState.observe(this, androidx.lifecycle.Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    startActivity(Intent(this, RemindersActivity::class.java))
                    finish()
                    //  gotoReminders() //not needed
                    binding.login.text = getString(R.string.logout)
                    AuthUI.getInstance().signOut(applicationContext)
                     Timber.i("User Logged In!!")
                }
                else ->     //if the user is not logged in
                {
                    LoginViewModel.AuthenticationState.UNAUTHENTICATED
                    binding.login.text = getString(R.string.login)
                    binding.login.setOnClickListener { launchSignInFlow() }
                }
            }
        })
    }
}
