package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser?>() {

//this class determines if the users has logged in or not

    private val firebaseAuth = FirebaseAuth.getInstance()

    //set the value from FirebaseAuth by hooking up current user
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    override fun onActive(){
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive(){
        firebaseAuth.removeAuthStateListener(authStateListener)
    }




}