package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun saveReminder_validData() = runBlocking {
        val isValid = saveReminderViewModel.validateAndSaveReminder(testDataItem)
        assertThat(isValid).isTrue()
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue())
            .isEqualTo(getString(R.string.reminder_saved))
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue()).isEqualTo(
            NavigationCommand.Back
        )
    }

    @Test
    fun saveReminder_nullTitle() = runBlocking {
        val isValid = saveReminderViewModel.validateAndSaveReminder(nullTitleDataItem)
        assertThat(isValid).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
            .isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun saveReminder_nullLocation() = runBlocking {
        val isValid = saveReminderViewModel.validateAndSaveReminder(nullLocationDataItem)
        assertThat(isValid).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
            .isEqualTo(R.string.err_select_location)
    }

    @Test
    fun saveReminder_nullRadius() = runBlocking {
        val isValid = saveReminderViewModel.validateAndSaveReminder(nullRadiusDataItem)
        assertThat(isValid).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
            .isEqualTo(R.string.err_enter_radius)
    }
}
