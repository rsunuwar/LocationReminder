package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi      //for using runBlocking
@RunWith(AndroidJUnit4::class)  //androidx test libs
//Unit test the DAO
@SmallTest                      //this is a DAO unit test.
class RemindersDaoTest {

//    TO DO: Add testing implementation to the RemindersDao.kt
    //this is added as we test Architecture components
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //use instance of db to set up the Dao
    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {      //create the database inmemory for test
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    //remember to close the database
    @After
    fun closeDb() = database.close()

    @Test
    //add the code for the map
    // fun getReminder(): ReminderDTO { using this, calling saveReminder would need to be a suspend function

    //GIVEN save a reminder
    fun getReminder() = runBlockingTest {       //use runBlockingTest as they are suspend

        var reminder = ReminderDTO(
            title = "title", description = "description", location = "location", latitude = 23.12345, longitude = 2.54567)

        database.reminderDao().saveReminder(reminder)

        //WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        //THEN - the loaded data contains the expected values,
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())  //notNull ensures the reminder actually came back
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }


    @Test
    fun updateReminderAndGetById() = runBlockingTest {

        // GIVEN - insert a reminder into the DAO
        val reminder1 = ReminderDTO("Cinema", "The best seats", "Choc Factory", 15.365, -89.432, "01")
        database.reminderDao().saveReminder(reminder1)

        //WHEN - Get the reminder by id from the database
        val loaded1 = database.reminderDao().getReminderById("01")

        // THEN - Check that when you get the reminder by its ID, it has the updated value.
        assertThat<ReminderDTO>(loaded1 as ReminderDTO, notNullValue())
        assertThat(loaded1.id, `is`(reminder1.id))
        assertThat(loaded1.title, `is`(reminder1.title))
        assertThat(loaded1.description, `is`(reminder1.description))
        assertThat(loaded1.location, `is`(reminder1.location))
        assertThat(loaded1.latitude, `is`(reminder1.latitude))
        assertThat(loaded1.longitude, `is`(reminder1.longitude))
    }


    @Test
    fun saveReminderAndGetId() = runBlockingTest {
        //GIVEN - try to save reminder
        val reminderDTO = ReminderDTO("title", "description", "location", 1.23, -2.34)
        database.reminderDao().saveReminder(reminderDTO)

        //WHEN - Use the ID to get the reminder from database
        val savedReminder = database.reminderDao().getReminderById(reminderDTO.id)

        //THEN - the expected values returned with the loaded data
        assertThat<ReminderDTO>(savedReminder as ReminderDTO, notNullValue())
        assertThat(savedReminder.id, `is`(reminderDTO.id))
        assertThat(savedReminder.title, `is`(reminderDTO.title))
        assertThat(savedReminder.description, `is`(reminderDTO.description))
        assertThat(savedReminder.location, `is`(reminderDTO.location))
        assertThat(savedReminder.latitude, `is`(reminderDTO.latitude))
        assertThat(savedReminder.longitude, `is`(reminderDTO.longitude))
    }
}