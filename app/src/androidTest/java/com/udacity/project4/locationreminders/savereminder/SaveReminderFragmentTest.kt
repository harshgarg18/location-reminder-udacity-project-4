package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import com.udacity.project4.locationreminders.util.getString
import com.udacity.project4.locationreminders.util.testDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

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
                SaveReminderViewModel(
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
    }

    @Test
    fun saveReminder_nullLocation() {
        lateinit var saveReminderViewModel: SaveReminderViewModel
        // GIVEN - Save Reminder Fragment launched
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            dataBindingIdlingResource.monitorFragment(it)
            saveReminderViewModel = it.viewModel
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Entered title and description but not location
        // AND Clicked on the "Save" button
        onView(withId(R.id.reminderTitle)).perform(typeText(testDTO.title))
        onView(withId(R.id.reminderDescription)).perform(typeText(testDTO.description))
        closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - verify that SnackBar is shown with error as missing location
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
            .isEqualTo(R.string.err_select_location)
    }

    @Test
    fun saveReminder_validData() {
        lateinit var saveReminderViewModel: SaveReminderViewModel
        // GIVEN - Save Reminder Fragment launched
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            dataBindingIdlingResource.monitorFragment(it)
            saveReminderViewModel = it.viewModel
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Entered all details and Clicked on the "Save" button
        onView(withId(R.id.reminderTitle)).perform(typeText(testDTO.title))
        onView(withId(R.id.reminderDescription)).perform(typeText(testDTO.description))
        closeSoftKeyboard()
        saveReminderViewModel.markLocation(
            LatLng(testDTO.latitude!!, testDTO.longitude!!),
            testDTO.location!!
        )
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - verify that toast is shown with message that reminder is saved
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue())
            .isEqualTo(getString(R.string.reminder_saved))
    }
}
