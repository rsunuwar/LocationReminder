package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O]) //need to run from P else Robolectric warn sdk 29 ....

class SaveReminderViewModelTest {

    //added
    val testDispatcher: TestCoroutineDispatcher= TestCoroutineDispatcher()

    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule        //gives us access to MainCoroutineRule
    var mainCoroutinesRule = MainCoroutineRule()

    private val reminder1DTO = ReminderDTO("Title1", "desc1", "location1", 1.23, 123.235)
    private val reminder2DTO = ReminderDTO("Title2", "desc2", "location2", 2.34, 234.567)
    private val reminder3DataItem = ReminderDataItem("Title3", "desc1", "location1", 1.234, 34.456)
    private val reminderNoTitle = ReminderDataItem("", "desc3", "location3", 2.345, -34.456)
    private val blankReminder = ReminderDataItem("", "","", 0.0, 0.0)
    //private val reminderList = listOf(reminder1DTO, reminder2DTO).sortedBy { it.id }

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var remindersDataSource: FakeDataSource
    private val applicationContext: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setUpViewModelTest() {
        remindersDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersDataSource) //FakeDataSource(reminderList.toMutableList()))
    }


    @Test   //test fails due to using Java 8 instead of 9
    fun validateAndSaveReminder_saveNewReminder() = mainCoroutinesRule.runBlockingTest {
        //GIVEN a reminder is validated
        saveReminderViewModel.validateAndSaveReminder(reminder3DataItem) //this takes in a dataItem

        //WHEN the reminder is saved
        saveReminderViewModel.saveReminder(reminder3DataItem)
        //THEN show the loading reminder toast
        //val value = saveReminderViewModel.showToast.value

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is` (applicationContext.getString(R.string.reminder_saved)))
    }

    @Test
    fun show_loading() = runBlockingTest {
       val reminder = reminder3DataItem

        mainCoroutinesRule.pauseDispatcher()

        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))

        mainCoroutinesRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
     }

    @Test
    fun validateAndSave_rejectEmptyTitle() {
        //GIVEN the reminder title is not set
        val badReminder1DataItem = ReminderDataItem("", "description", "", 1.234, 2.345)
        //WHEN Validate is called
        val result = saveReminderViewModel.validateEnteredData(badReminder1DataItem)
        //THEN snackbar tells users to enter title
        //saveReminderViewModel.validateAndSaveReminder(badReminder1DataItem)
        //val value = saveReminderViewModel.showSnackBarInt.value
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
        Assert.assertFalse(result)
    }

    @Test
    fun saveReminder_validateCorrectData() {
        //GIVEN when all data is correct
        val result = saveReminderViewModel.validateEnteredData(reminder3DataItem)
        //True is returned
        Assert.assertTrue(result)
    }


    @Test
    fun saveReminder_clearLiveData() {
        //When a reminder is cleared
        saveReminderViewModel.onClear()
        //livedata is reset to null
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), nullValue())
    }

    @Test
    fun saveReminderViewModel_showSuccessToastMsg() = runBlockingTest {
        saveReminderViewModel.saveReminder(reminder3DataItem)

        val showToast = saveReminderViewModel.showToast.getOrAwaitValue()
        Assert.assertEquals(showToast, applicationContext.resources.getString(R.string.reminder_saved))
    }

    //create test for toast (test passes)

    @Test
    fun saveReminder_showToast() {
        //GIVEN we create a reminder
        val reminder = reminder3DataItem

        //WHEN the reminder is  saved
        saveReminderViewModel.saveReminder(reminder)

        //THEN we get a Toast message that shows success
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test //snackbar test pass
    fun saveReminder_showSnackBar() = runBlockingTest {
        //GIVEN we create a reminder, missing a title
        val reminder = reminderNoTitle

        //WHEN the reminder is validated, if title is null show snackbar
        saveReminderViewModel.validateEnteredData(reminder)

        Truth.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }


    @After
    fun tearDown() {
        stopKoin()
    }


}
