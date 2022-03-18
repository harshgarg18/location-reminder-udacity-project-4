package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.util.testDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    // Subject under test
    private lateinit var dao: RemindersDao

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        dao = database.reminderDao()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderTest() = runBlocking {
        // GIVEN - save a reminder
        dao.saveReminder(testDTO)

        // WHEN - all reminders are fetched from the database
        val loaded = dao.getReminders()

        // THEN - the loaded list is of size 1 and contains the expected reminder
        assertThat(loaded.size).isEqualTo(1)
        assertThat(loaded).contains(testDTO)
    }

    @Test
    fun getReminderByIdTest() = runBlocking {
        // GIVEN - save a reminder
        dao.saveReminder(testDTO)

        // WHEN - get the reminder by id from the database
        val loaded = dao.getReminderById(testDTO.id)

        // THEN - the loaded reminder contains the expected values
        assertThat(loaded).isNotNull()
        assertThat(loaded?.title).isEqualTo(testDTO.title)
        assertThat(loaded?.description).isEqualTo(testDTO.description)
        assertThat(loaded?.location).isEqualTo(testDTO.location)
        assertThat(loaded?.latitude).isEqualTo(testDTO.latitude)
        assertThat(loaded?.longitude).isEqualTo(testDTO.longitude)
        assertThat(loaded?.radius).isEqualTo(testDTO.radius)
    }

    @Test
    fun deleteAllRemindersTest() = runBlocking {
        // GIVEN - save a reminder
        dao.saveReminder(testDTO)

        // WHEN - all reminders are fetched from the database
        val loadedFirst = dao.getReminders()

        // THEN - size of loaded list is 1
        assertThat(loadedFirst.size).isEqualTo(1)

        // WHEN - delete all reminders from the database
        dao.deleteAllReminders()

        // WHEN - all reminders are again fetched from the database
        val loadedAgain = dao.getReminders()

        // THEN - size of loaded list is empty/of size 0
        assertThat(loadedAgain).isEmpty()
        assertThat(loadedAgain.size).isEqualTo(0)
    }
}
