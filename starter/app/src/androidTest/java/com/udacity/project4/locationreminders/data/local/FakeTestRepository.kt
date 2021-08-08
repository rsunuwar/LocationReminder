package com.udacity.project4.locationreminders.data.local

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeTestRepository: ReminderDataSource {

    var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //TO DO("Not yet implemented")
        if (shouldReturnError) {        //if the error is true
            return Result.Error("Test Exception")
        }
        return Result.Success(remindersServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //TO DO("Not yet implemented")
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //TO DO("Not yet implemented")
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        else if (remindersServiceData.containsKey(id)) {
            return Result.Success(remindersServiceData[id]!!)
        } else {
            return Result.Error("ID value not valid")
        }
    }

    override suspend fun deleteAllReminders() {
        //TO DO("Not yet implemented")
        remindersServiceData.clear()
    }
}