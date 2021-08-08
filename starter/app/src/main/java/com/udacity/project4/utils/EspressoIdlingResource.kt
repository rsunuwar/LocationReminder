package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

//singleton class that contains an idling resource. and can track long running classes if they are still working.

object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }

    //use this to prevent you from forgetting to add when needed
    inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
        EspressoIdlingResource.increment() //Set app as busy.
        return try {
            function()
        } finally {
            EspressoIdlingResource.decrement() //Set app as idle.
        }
    }

}