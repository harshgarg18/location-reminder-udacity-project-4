package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.util.testDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanup() = database.close()


    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        // GIVEN - a new reminder is saved in the database
        repository.saveReminder(testDTO)

        // WHEN - Reminder retrieved by ID.
        val result = repository.getReminder(testDTO.id)

        // THEN - Same reminder is returned.
        assertThat(result.succeeded).isTrue()
        result as Result.Success
        assertThat(result.data).isNotNull()
        assertThat(result.data.title).isEqualTo(testDTO.title)
        assertThat(result.data.description).isEqualTo(testDTO.description)
        assertThat(result.data.location).isEqualTo(testDTO.location)
        assertThat(result.data.latitude).isEqualTo(testDTO.latitude)
        assertThat(result.data.longitude).isEqualTo(testDTO.longitude)
        assertThat(result.data.radius).isEqualTo(testDTO.radius)
    }

    @Test
    fun retrieveInvalidReminder() = runBlocking {
        // WHEN - Reminder retrieved by ID which is never inserted.
        val result = repository.getReminder(testDTO.id)

        // THEN - error is returned by the repository
        assertThat(result.succeeded).isFalse()
        result as Result.Error
        assertThat(result.message).isEqualTo("Reminder not found!")
    }

    @Test
    fun retrieveAllReminders_deleteReminders() = runBlocking {
        // GIVEN - a new reminder is saved in the database
        repository.saveReminder(testDTO)

        // WHEN - Fetched all reminders.
        val result = repository.getReminders()

        // THEN - list contains added reminder
        assertThat(result.succeeded).isTrue()
        result as Result.Success
        assertThat(result.data).isNotEmpty()
        assertThat(result.data).contains(testDTO)

        // GIVEN - delete all reminders from the database
        repository.deleteAllReminders()

        // WHEN - Fetched all reminders.
        val newResult = repository.getReminders()

        // THEN - list is empty
        assertThat(newResult.succeeded).isTrue()
        newResult as Result.Success
        assertThat(newResult.data).isEmpty()
    }
}
