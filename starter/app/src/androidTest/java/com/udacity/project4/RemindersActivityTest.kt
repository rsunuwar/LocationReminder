package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.ToastMatcher
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

//version2
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() { // Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * We use Koin as a Service Locator for dependency injection and also use it to test our app.
     */
    @Before
    fun init() {
        stopKoin() //ensure all instances are stopped
        appContext = getApplicationContext()        //initialise appContext
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository created above
        repository = get()

        //clear the data to start a fresh. this is a coroutine, use runBlocking
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //get reminder instance for test
    private fun getReminder(): ReminderDTO {
    return ReminderDTO(
            title = "Nice POI",
            description = "A cool place",
            location = "US LA",
            latitude = 1.2345,
            longitude = 2.3456)
    }

    @Test
    fun createReminder_checkReminderList() = runBlocking {
        //GIVEN a reminder save reminder to repository
        val reminder = getReminder()
        repository.saveReminder(reminder)

        //WHEN activity is launched
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //THEN checks that onView items match reminder instance
        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.description)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))

        runBlocking {
            delay(2000)
        }
    }


    @Test
    fun addReminderNoTitle_checkForSnackMessage()      { //: Unit = runBlocking {
        //GIVEN a new reminder
        val reminder1 = getReminder()
        //WHEN  activity if launched
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        //THEN click FAB button and show toast message
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminder1.description))
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())

        //verify the snackbar is displayed on screen
        val snackBar = appContext.getString(R.string.err_enter_title)
        onView(withText(snackBar)).check(matches(isDisplayed()))
        activityScenario.close()

        runBlocking {
            delay(2000)
        }
    }
    
      @Test     //test passes following revised savereminderfragment.
    fun addReminder_saveAndcheckToastMessage() : Unit = runBlocking {

        //When activity is launched
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
       // var activity = getActivity(activityScenario)

        //Given a new reminder
        val reminder2 = getReminder()

        //THEN click FAB button and show toast message
        onView(withId(R.id.addReminderFAB)).perform(click())        //click the fab button, go to the add location screen
        onView(withId(R.id.reminderTitle)).perform(typeText(reminder2.title)) //add the title
        onView(withId(R.id.reminderDescription)).perform(typeText(reminder2.description)) //add the description
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.selectLocation)).perform(longClick())    //go to the maps screen

        //now simulate clicking the map
        onView(withId(R.id.map)).perform(longClick())    //click on the map for POI
         delay(1500)

        onView(withId(R.id.savemap)).perform(click()) //click the save button to save the POI and navigate back to location screen

        onView(withId(R.id.saveReminder)).perform(click())      //click the save fab, this saves the reminder and shows a toast msg

        onView(withText(R.string.reminder_saved)).inRoot(ToastMatcher()).check(matches(isDisplayed()))

        activityScenario.close()

        runBlocking {
           delay(2000)


       }
    }
}
