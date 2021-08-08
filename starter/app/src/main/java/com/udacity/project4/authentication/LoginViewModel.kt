package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class LoginViewModel : ViewModel() {


    //set up the authentication state
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    //we use the authentication state to determine if the user logging in has been authenticated
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }




}