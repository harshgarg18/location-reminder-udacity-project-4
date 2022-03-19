package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.util.testDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

    private lateinit var dataSource: ReminderDataSource

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

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }

        // declare a new koin module
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

    @Test
    fun clickAddReminder_navigateToSaveReminderFragment() {
        // GIVEN - Reminder List Fragment launched
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            dataBindingIdlingResource.monitorFragment(it)
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Clicked on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to SaveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun addReminder_DisplayedInUI() {
        // GIVEN - Add reminder to the DB
        runBlocking {
            dataSource.saveReminder(testDTO)
        }

        // WHEN  - Reminder List Fragment launched to display reminders
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            dataBindingIdlingResource.monitorFragment(it)
        }

        // THEN - Reminder details are display on screen
        onView(withText(testDTO.title)).check(matches(isDisplayed()))
        onView(withText(testDTO.description)).check(matches(isDisplayed()))
        onView(withText(testDTO.location)).check(matches(isDisplayed()))

        // AND - "No Data" view is hidden
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun noReminder_DisplayedInUI() {
        // GIVEN - No reminder in the DB

        // WHEN  - Reminder List Fragment launched to display reminders
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            dataBindingIdlingResource.monitorFragment(it)
        }

        // THEN - "No Data" view is displayed
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.noDataTextView)).check(matches(withText(R.string.no_data)))
    }
}
