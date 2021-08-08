package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import org.koin.android.ext.koin.ERROR_MSG

//Use FakeDataSource that acts as a test double to the LocalDataSource which is ReminderDataSource
   class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TO DO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false //boolean flag for testing errors

    fun setReturnError(value: Boolean) {    //function for changing flag above.
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
          reminders?.let { return Result.Success(ArrayList(it)) }

        return Result.Error("Reminders not found!")
}

    //NEED TO RESOLVE ---------
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //TO DO("return the reminder with the id")
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
            reminders?.let {
                for(reminder in it) {
                    if (id == reminder.id) {
                    return Result.Success(reminder)
            }}}
            return Result.Error(ERROR_MSG)
    }



    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}