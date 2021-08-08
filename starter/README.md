# Location_App


This app is login enabled and uses maps and allows the users to drop a 'Point of Interest' marker on the map, then save that location along with a title and description of the saved location. The latitude and longitude of the location are stored for the POI reminder location, they are saved to a local database using a repository.

This app uses Firebase to manage user login authentication. Google maps api for geofencing. Room (DAO) for data storage.

Prerequisites

* Koin, Firebase, GoogleMaps Api, Geofencing...
* Android SDK v30 Android Build Tools v29.0.3

## Built With
* [Koin](https://github.com/InsertKoinIO/koin) - A pragmatic lightweight dependency injection framework for Kotlin.
* [FirebaseUI Authentication](https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md) - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing

* Permission denied fixed to enable uses to generate Geofence.
