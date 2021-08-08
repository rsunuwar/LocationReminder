package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    //code-1 clean up
    //this screen is used to show the map and save the selected location.

    companion object {
        const val TAG = "SelectLocationFragment"
        private const val REQUEST_LOCATION_PERMISSION = 1
        // private const val LOCATION_PERMISSION_INDEX = 0
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    // request location permission in top level code

    private lateinit var binding: FragmentSelectLocationBinding

    //create a global var for the map
    private lateinit var map: GoogleMap

    private lateinit var pointOfInterest: PointOfInterest

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        TO DO: zoom to the user location after taking his permission
//        TO DO: add style to the map, this is done through the map options
//        TO DO: put a marker to location that the user selected. setmaplongClick
//        TO DO: call this function after the user confirms on the selected location

        // check if POI has been clicked. if user confirms on selected location
        binding.savemap.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }


    //this will check if the user has granted permission for location
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission (
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    //permission request control
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Timber.d("onRequestPermissionResult")

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()        //(map) new code -1
            } else {
                //use a snack bar message to tell user you need permission
                Timber.i("You need permission to use the app")
                Snackbar.make(requireView(),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.settings) {
                            //  requestForeGroundPermissions()
                            startActivity(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }.show()
            }

        }
    }


    //this is added for building the map
    override fun onMapReady(googleMap: GoogleMap) {

        //set up the maps styling.
        map = googleMap

        //Add a marker in the preferred location and move the camera
        //build the map location
        //set up the use of markers, these are the home coordinates
        val latitude = 37.422160      //these are starting setting, they are not needed
        val longitude = -122.084270
        val zoomLevel = 15f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))

        enableMyLocation(/*map*/)
        setPoiClick(map)
        setMapLongClick(map)        //this adds a marker
        setMapStyle(map)        //the base map styling complete
    }


    //this is used for the map types as below
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TO DO: Change the map type based on the user's selection.
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


    private fun setMapStyle(map: GoogleMap) {
        try {
            //Customise the styling of the base map using a JSON pbject defined
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(/*context*/ activity,
                            R.raw.map_style)
            )
            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e("Cant find style. Error: ")}
    }

    private fun setMapLongClick (map:GoogleMap) {
        map.setOnMapLongClickListener {  latLng ->
            //add a market for the selection location
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )
            map.addMarker(
                    MarkerOptions()
                            .position(latLng)   //add these for the snippet
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) //nice color
            )
            binding.savemap.setOnClickListener{
                _viewModel.latitude.value = latLng.latitude
                _viewModel.longitude.value = latLng.longitude
                _viewModel.reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }
        }
    }



    //set the poi click method point of interest
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            pointOfInterest = poi
            val poiMarker = map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
            )

            map.addCircle(
                    CircleOptions()
                            .center(poi.latLng)
                            .radius(500.0)
                            .strokeColor(Color.argb(155, 255, 0, 0))
                            .fillColor(Color.argb(64, 255,0,0)).strokeWidth(2F))
            poiMarker.showInfoWindow()
        }
    }


    private fun onLocationSelected() {

        //check if the POI has been selected
        //        TO DO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

        if (::pointOfInterest.isInitialized) {      //check for poi initialisation
            //if the poi has not been selected, then...
            _viewModel.selectedPOI.value = pointOfInterest
            _viewModel.latitude.value = pointOfInterest.latLng.latitude
            _viewModel.latitude.value = pointOfInterest.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
            _viewModel.navigationCommand.value = NavigationCommand.Back
            Timber.i("${_viewModel.selectedPOI.value}")

            //this fragment is now returned to SaveReminderFragment
        } else {
            Toast.makeText(context, getString(R.string.select_location), Toast.LENGTH_SHORT).show() }
        //  Toast.makeText(context, "Does this work" /*Please select location"*/, Toast.LENGTH_SHORT).show() }
    }


    //Enable my location if fine location permission is granted, this one works better.
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {        //(map: GoogleMap) {  new code - 1
        // if(!::map.isInitialized)) { return
        if (isPermissionGranted()) {


            //
             if (ActivityCompat.checkSelfPermission(requireContext(),      //new code - 1
                              Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                              ActivityCompat.checkSelfPermission(requireContext(),
                              Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                  return
              }    //
            map.isMyLocationEnabled = true
        } else {
            // ActivityCompat.requestPermissions(requireActivity(), arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            /* ActivityCompat.*/requestPermissions(/*requireActivity(),*/ arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
        }
    }
}

















/*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //this screen is used to show the map and save the selected location.

    companion object {
        const val TAG = "SelectLocationFragment"
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val LOCATION_PERMISSION_INDEX = 0
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    // request location permission in top level code

    private lateinit var binding: FragmentSelectLocationBinding

    //create a global var for the map
    private lateinit var map: GoogleMap

    private lateinit var pointOfInterest: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)


        //new code add the viewmodel and lifecycle binding
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

      //  if (isPermissionGranted()) {
          //  zoom to the users location??
          //  map.moveCamera(map)

//        TO DO: zoom to the user location after taking his permission
//        TO DO: add style to the map, this is done through the map options
//        TO DO: put a marker to location that the user selected. setmaplongClick
//        TO DO: call this function after the user confirms on the selected location

        // check if POI has been clicked. if user confirms on selected location
        binding.savemap.setOnClickListener {
            onLocationSelected()
        }
            return binding.root
    }


    //this will check if the user has granted permission for location
    private fun isPermissionGranted() : Boolean {
      return ContextCompat.checkSelfPermission (
             requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    //permission request control
    //this code is not working, the snackbar is not shown
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Timber.d("onRequestPermissionResult")

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation(map)
            } else {
                //use a snack bar message to tell user you need permission
                 Timber.i("You need permission to use the app")
                  Snackbar.make(requireView(),
                          R.string.permission_denied_explanation,
                          Snackbar.LENGTH_LONG)
                          .setAction(R.string.settings) {
                            //   requestForeGroundPermissions()
                              startActivity(Intent().apply {
                                  action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                  data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                  flags = Intent.FLAG_ACTIVITY_NEW_TASK
                              })
                          }.show()
              }

        }
    }


    //this is added for building the map
    override fun onMapReady(googleMap: GoogleMap) {

        //set up the maps styling.
        map = googleMap

        //Add a marker in the preferred location and move the camera
        //build the map location
        //set up the use of markers, these are the home coordinates
      val latitude = 37.422160      //these are starting setting, they are not needed
        val longitude = -122.084270
        val zoomLevel = 15f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))

        enableMyLocation(map)
        setPoiClick(map)
        setMapLongClick(map)        //this adds a marker
        setMapStyle(map)        //the base map styling complete
    }


    //this is used for the map types as below
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TO DO: Change the map type based on the user's selection.
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


    private fun setMapStyle(map: GoogleMap) {
            try {
                //Customise the styling of the base map using a JSON pbject defined
                val success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(/*context*/ activity,
                                R.raw.map_style)
                )
                if (!success) {
                    Timber.e("Style parsing failed.")
                }
            } catch (e: Resources.NotFoundException) {
            Timber.e("Cant find style. Error: ")}
    }

    private fun setMapLongClick (map:GoogleMap) {
        map.setOnMapLongClickListener {  latLng ->
            //add a market for the selection location
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )
        map.addMarker(
                MarkerOptions()
                        .position(latLng)   //add these for the snippet
                        .title(getString(R.string.dropped_pin))
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) //nice color
            )
            binding.savemap.setOnClickListener{
                _viewModel.latitude.value = latLng.latitude
                _viewModel.longitude.value = latLng.longitude
                _viewModel.reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }
        }
    }



    //set the poi click method point of interest
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            pointOfInterest = poi
            val poiMarker = map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
            )

            map.addCircle(
                    CircleOptions()
                    .center(poi.latLng)
                    .radius(500.0)
                    .strokeColor(Color.argb(155, 255, 0, 0))
                            .fillColor(Color.argb(64, 255,0,0)).strokeWidth(2F))
        poiMarker.showInfoWindow()
        }
    }


    private fun onLocationSelected() {

        //check if the POI has been selected
        //        TO DO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

        if (::pointOfInterest.isInitialized) {      //check for poi initialisation
        //if the poi has not been selected, then...
            _viewModel.selectedPOI.value = pointOfInterest
            _viewModel.latitude.value = pointOfInterest.latLng.latitude
            _viewModel.latitude.value = pointOfInterest.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
            _viewModel.navigationCommand.value = NavigationCommand.Back
            Timber.i("${_viewModel.selectedPOI.value}")

            //this fragment is now returned to SaveReminderFragment
        } else {
          Toast.makeText(context, getString(R.string.select_location), Toast.LENGTH_SHORT).show() }
          //  Toast.makeText(context, "Does this work" /*Please select location"*/, Toast.LENGTH_SHORT).show() }
        }


    //Enable my location if fine location permission is granted, this one works better.
    @SuppressLint("MissingPermission")
    private fun enableMyLocation(map: GoogleMap) {
     //  if(!::map.isInitialized)) return
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
        }
    }
}

 */
