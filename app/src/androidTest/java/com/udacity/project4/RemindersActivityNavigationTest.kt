package com.udacity.project4

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.selectreminderlocation.SelectLocationViewModel
import com.udacity.project4.locationreminders.util.testDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityNavigationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

    private lateinit var dataSource: ReminderDataSource

    @Before
    fun init() {
        stopKoin() // stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            viewModel { SelectLocationViewModel(appContext) }

            viewModel {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }

        // Get our real repository
        dataSource = GlobalContext.get().koin.get()

        // clear the data to start fresh
        runBlocking {
            dataSource.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance()
            .register(EspressoIdlingResource.countingIdlingResource, dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance()
            .unregister(EspressoIdlingResource.countingIdlingResource, dataBindingIdlingResource)
    }

    @Test
    // For some reason, this test is not working alongside other tests in RemindersActivityTest
    fun launchActivity_addReminder() {
        // WHEN - Start up Reminders Activity.
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // THEN - verify that "No Data" view is displayed
        onView(withId(R.id.refreshLayout)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.noDataTextView)).check(matches(withText(R.string.no_data)))
        onView(withId(R.id.addReminderFAB)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))


        // WHEN - Clicked on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Save Reminder Fragment is displayed
        onView(withId(R.id.save_reminder_view)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        // WHEN - clicked on the "Select Location" view
        onView(withId(R.id.selectLocation)).perform(click())

        // THEN - Select Location Fragment is displayed
        onView(withId(R.id.select_location_view)).check(matches(isDisplayed()))


        // WHEN - selected a location from map fragment
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.fab)).perform(click())

        // THEN - Save Reminder Fragment is displayed again
        onView(withId(R.id.save_reminder_view)).check(matches(isDisplayed()))

        // WHEN - filled title and description, and saved
        onView(withId(R.id.reminderTitle)).perform(
            typeText(testDTO.title),
            closeSoftKeyboard()
        )
        onView(withId(R.id.reminderDescription)).perform(
            typeText(testDTO.description),
            closeSoftKeyboard()
        )
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - navigated back to Reminder List Screen
        // AND - Reminder details are display on screen
        onView(withId(R.id.refreshLayout)).check(matches(isDisplayed()))
        onView(withId(R.id.refreshLayout)).perform(swipeDown())
        onView(withText(testDTO.title)).check(matches(isDisplayed()))
        onView(withText(testDTO.description)).check(matches(isDisplayed()))
        // AND - "No Data" view is hidden
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        scenario.close()
    }
}
