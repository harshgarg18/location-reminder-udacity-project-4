package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import com.udacity.project4.locationreminders.util.testDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun loadReminders_loading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun loadReminders_callErrorToDisplay() = runBlockingTest {
        // Make the data source returns an error
        fakeDataSource.setReturnError(true)

        remindersListViewModel.loadReminders()

        // Then an error message is shown
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun showNoData_isEmpty() = runBlockingTest {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).isEmpty()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isTrue()
    }

    @Test
    fun showNoData_isNotEmpty() = runBlockingTest {
        fakeDataSource.saveReminder(testDTO)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).isNotEmpty()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isFalse()
    }
}
