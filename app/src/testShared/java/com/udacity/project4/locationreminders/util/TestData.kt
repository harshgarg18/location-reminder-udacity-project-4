package com.udacity.project4.locationreminders.util

import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.toDTO

val testDataItem = ReminderDataItem(
    "Test",
    "Testing",
    "Sydney",
    -33.852,
    151.211,
    100.0
)

val nullTitleDataItem = ReminderDataItem(
    null,
    "Testing",
    "Sydney",
    -33.852,
    151.211,
    100.0
)

val nullLocationDataItem = ReminderDataItem(
    "Test",
    "Testing",
    null,
    null,
    null,
    100.0
)

val nullRadiusDataItem = ReminderDataItem(
    "Test",
    "Testing",
    "Sydney",
    -33.852,
    151.211,
    null
)

val testDTO = testDataItem.toDTO()
