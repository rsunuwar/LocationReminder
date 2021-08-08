package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.project4.locationreminders.data.ReminderDataSource

class SaveReminderViewModelFactory(
        private val application: Application,
        private val dataSource: ReminderDataSource/*RemindersDao*/) : ViewModelProvider.Factory {


    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SaveReminderViewModel::class.java)) {
            return SaveReminderViewModel(application, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}