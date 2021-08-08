package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

//All tests pass
//To make this test work, as we get the robolectric error. go to Run edit config and change test
@RunWith(AndroidJUnit4::class)
@Config(sdk=[Build.VERSION_CODES.P])

@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutinesRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //TO DO: provide testing to the RemindersListViewModel and its live data objects
    private val reminder1DTO = ReminderDTO("title", "description", "location", 1.23, 3.45)
    private val reminder2DTO = ReminderDTO("title", "description", "location", 1.23, 3.45)
    private val reminderDTO3 = ReminderDTO("title", "description", "location", 1.23, 3.45)

    //subject under test
    private val reminderList = listOf(reminder1DTO, reminder2DTO).sortedBy { it.id }


    //use a fakje data source to be injected into the view model
    private lateinit var reminderDataSource: FakeDataSource
    private val applicationContext: Application = ApplicationProvider.getApplicationContext()
    //var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var reminderListViewModel: RemindersListViewModel
   // private lateinit var fakeDataSource: FakeDataSource

    //set up the viewmodel item to test before
    @Before
    fun setup() {
        reminderDataSource = FakeDataSource(reminderList.toMutableList())
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)

    }

    @Test
    fun loadReminders_getRemindersFromDataSourceShowSuccess() {
        //GIVEN when reminders are loaded, test successful result
        reminderListViewModel.loadReminders()
        //val value: List<ReminderDataItem>? = reminderListViewModel.remindersList.value
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue()?.size, `is`(2)) //2 DTO items declared above
    }


    //test with error
    @ExperimentalCoroutinesApi
    @Test
    fun loadReminders_getRemindersFromDataSourceWithError() = runBlockingTest {
        //GIVEN the fake is set to return an error
        reminderDataSource.setReturnError(true)
        //WHEN subject under test loads the reminders
        reminderListViewModel.loadReminders()
        //THEN show the error
        assertThat(reminderListViewModel.showSnackBar.value, `is`("Test Exception")) //snackbar shows a test error

    }

  @ExperimentalCoroutinesApi
    @Test
    fun loadreminders_showloadingIndicator() = runBlockingTest {
        mainCoroutinesRule.pauseDispatcher()
        //load reminders
        reminderListViewModel.loadReminders()
        // show progress indicator
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        //execute pending coroutine action
        mainCoroutinesRule.resumeDispatcher()

        //hide progress indicator
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @After
    fun tearDownKoin() {
        stopKoin()
    }

}


