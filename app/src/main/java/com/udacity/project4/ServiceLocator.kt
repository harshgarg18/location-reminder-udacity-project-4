package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private val lock = Any()

    private var database: RemindersDatabase? = null

    @Volatile
    var dataSource: ReminderDataSource? = null
        @VisibleForTesting set

    fun provideDataSource(context: Context): ReminderDataSource {
        synchronized(this) {
            return dataSource ?: createDataSource(context)
        }
    }

    private fun createDataSource(context: Context): ReminderDataSource {
        val db = database ?: createDataBase(context)
        val newDataSource = RemindersLocalRepository(db.reminderDao())
        dataSource = newDataSource
        return newDataSource
    }

    private fun createDataBase(context: Context): RemindersDatabase {
        val db = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).build()
        database = db
        return db
    }

    @VisibleForTesting
    fun resetDataSource() {
        synchronized(lock) {
            runBlocking {
                database?.apply {
                    clearAllTables()
                    close()
                }
                database = null
                dataSource = null
            }
        }
    }

}