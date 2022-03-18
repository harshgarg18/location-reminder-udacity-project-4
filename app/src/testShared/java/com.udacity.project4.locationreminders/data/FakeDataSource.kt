package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private val reminders: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(isError: Boolean) {
        shouldReturnError = isError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Error occurred while fetching reminders")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders += reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Error occurred while fetching reminder with id = $id")
        }

        val reminder = reminders.find { it.id == id }
        if (reminder != null) {
            return Result.Success(reminder)
        }
        return Result.Error("Reminder with id = $id not found")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}
