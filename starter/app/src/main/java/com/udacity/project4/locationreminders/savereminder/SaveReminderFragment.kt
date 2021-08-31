package com.udacity.project4.locationreminders.savereminder

//import com.udacity.project4.BuildConfig

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.BuildConfig
import timber.log.Timber
import android.util.Log

class SaveReminderFragment : BaseFragment() {

        //Get the view model this time as a single to be shared with the other fragment
        override val _viewModel: SaveReminderViewModel by inject()
        private lateinit var binding: FragmentSaveReminderBinding
        private lateinit var reminderData: ReminderDataItem

        private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
        private lateinit var geofencingClient: GeofencingClient
        private val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
            intent.action = ACTION_GEOFENCE_EVENT
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? { binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
            setDisplayHomeAsUpEnabled(true)
            binding.viewModel = _viewModel
            geofencingClient = LocationServices.getGeofencingClient(requireContext())
            return binding.root
        }


        /*
        *  Determines whether the app has the appropriate permissions across Android 10+ and all other
        *  Android versions.
        */
        @TargetApi(29)
        private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
            val foregroundLocationApproved = (
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION))
            val backgroundPermissionApproved =
                if (runningQOrLater) {
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    true
                }
            return foregroundLocationApproved && backgroundPermissionApproved
            //true is permissions are granted and false if not.
        }

  override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            Timber.d("onRequestPermissionResult")
            if (
                grantResults.isEmpty() ||
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED))
            {       //use snackbar to inform user of need for permission
                Snackbar.make(
                    this.requireView(),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show() //use snackbar to show error message else return checkDevice.....
            } else {
                checkDeviceLocationSettingsAndStartGeofence()
            }
        }
  
  
    /*
    *  Requests ACCESS_FINE_LOCATION on AndroidQ ACCESS_BACKGROUND_LOCATION.
    */
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())        //if permissions approved, return
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Timber.d("Request foreground only location permission")
       
      requestPermissions(permissionsArray, resultCode)
    }


        private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }

            val locationSettingRequestsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val settingsClient = LocationServices.getSettingsClient(requireContext())     
            val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(locationSettingRequestsBuilder.build())       

            locationSettingsResponseTask.addOnFailureListener { exception ->
                if (exception is ResolvableApiException && resolve){
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                          startIntentSenderForResult(
                                  exception.resolution.intentSender,
                                   REQUEST_TURN_DEVICE_LOCATION_ON, null,
                                  //REQUEST_CODE_LOCATION_SETTING, null,
                                   0,0,0,
                                   null)
                          //  REQUEST_TURN_DEVICE_LOCATION_ON     // is this needed??
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Timber.d("Error getting location settings resolution: %s", sendEx.message)
                    }
                } else {
                    Snackbar.make(
                        this.requireView(),
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }.show()
                }
            }
            locationSettingsResponseTask.addOnCompleteListener {
                if ( it.isSuccessful ) {
                    Timber.i("Granted")
                    //here we know the device location is enabled, now we can move onto adding the Geofence
                    addGeofenceForReminder(reminderData)
                }
            }
        }


         @SuppressLint("MissingPermission")
        private fun addGeofenceForReminder(reminderData: ReminderDataItem) {

          //  if (reminderData != null) {
            val geofence = Geofence.Builder()
                    .setRequestId(reminderData.id)
                    .setCircularRegion(
                         reminderData.latitude!!,
                         reminderData.longitude!!,
                            GEOFENCE_RADIUS_IN_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

            val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                //new code, this is not needed
                //val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
                //intent.action = ACTION_GEOFENCE_EVENT


            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                addOnCompleteListener {
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                        addOnSuccessListener {

                           // Timber.i("%s%s", "added geofences" + latitude + " ", currentreminderData.longitude)
                            _viewModel.showSnackBarInt.value = R.string.geofences_added
                            _viewModel.validateAndSaveReminder(reminderData)
                        }
                        addOnFailureListener {
                            _viewModel.showSnackBarInt.value = R.string.geofences_not_added
                             it.message?.let { message -> Log.w(TAG, message)
                                Timber.w(it.message!!)

                            }
                        }
                    }
                }
            }
        }


           override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)
                if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
                    checkDeviceLocationSettingsAndStartGeofence(false)
                }
            }
            


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.lifecycleOwner = this
            binding.selectLocation.setOnClickListener {     //we navigate to the map to find a POI
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }

      
            //this is the FAB button
            binding.saveReminder.setOnClickListener {
                Timber.i("onClick")

                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value

               //val
                   reminderData = ReminderDataItem(
                    title = title,
                    description = description,
                    location = location,
                    latitude = latitude,
                    longitude = longitude)

                /** this is where the error needs to be resolved */

              //  _viewModel.validateAndSaveReminder(reminderData)  changed 8/8/21
                if (_viewModel.validateEnteredData(reminderData)) {
                  // addGeofenceForReminder(reminderData)   move this to do geofence after checks

                   // checkDeviceLocationSettingsAndStartGeofence() //new code..... added 8.8.21
                    checkPermissionsAndStartGeofencing()

                   // addGeofenceForReminder(reminderData)
                    Timber.i( "Added Reminder to Geofence")
                  //  Toast.makeText(context, "Location Added", Toast.LENGTH_SHORT).show() REMOVE TOAST TO PASS TEST
                }
           
            }
        }



    // Starts the permission check and Geofence process only if the Geofence associated with the
    // current hint isn't yet active.

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

        override fun onDestroy() {
            super.onDestroy()
            //make sure to clear the view model after destroy, as it's a single view model.
            _viewModel.onClear()
        }

        companion object {
            private const val TAG = "SaveReminderFragment"
            private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
            private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
            private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
            private const val LOCATION_PERMISSION_INDEX = 0
            private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
            //private const val REQUEST_CODE_LOCATION_SETTING = 1   //need to check this
            const val GEOFENCE_RADIUS_IN_METERS = 500f
        }
    }

