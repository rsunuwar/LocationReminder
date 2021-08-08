package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    //    TO DO: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var localDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before //set up the test
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()
        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
                title = "Title",
                description = "Description",
                location = "location",
                latitude = 1.2345,
                longitude = -2.3456)
    }

    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        // GIVEN a new reminder is saved to the database
        val reminder = getReminder()
        localDataSource.saveReminder(reminder)

        //when - reminder is retrieved by id
        val result = localDataSource.getReminder(reminder.id)

        //Then - the reminder is returned
        Assert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.title, `is`(reminder.title))
        Assert.assertThat(result.data.title, `is`(reminder.title))
        Assert.assertThat(result.data.location, `is`(reminder.location))
        Assert.assertThat(result.data.latitude, `is`(reminder.latitude))
        Assert.assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteReminders_getReminder() = runBlocking {
        //GIVEN
        val reminder = getReminder()
        localDataSource.saveReminder(reminder)
        localDataSource.deleteAllReminders()

        //WHEN
        val result = localDataSource.getReminder(reminder.id)

        //THEN
        Assert.assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        Assert.assertThat(result.message, `is`("Reminder not found!"))

    }
}
