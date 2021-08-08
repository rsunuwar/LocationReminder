package com.udacity.project4.loggin

import android.app.Application
import timber.log.Timber


class TimberLogging: Application() {

        //create application class for Logging
        //ensure application is added to Manifest

        override fun onCreate() {
            super.onCreate()
            Timber.plant(Timber.DebugTree())
        }

    }
