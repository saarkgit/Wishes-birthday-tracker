package com.birthdaytracker.notification

import com.birthdaytracker.data.Birthday
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class BirthdayNotificationHelper @Inject constructor() {

    fun getBirthdaysToNotify(
        birthdays: List<Birthday>,
        notificationDayOf: Boolean,
        notificationWeekBefore: Boolean,
        today: LocalDate = LocalDate.now()
    ): List<Pair<Birthday, Int>> {
//        val today = LocalDate.now()
        val result = mutableListOf<Pair<Birthday, Int>>()

        birthdays.forEach { birthday ->
            val thisYear = birthday.birthDate.withYear(today.year)
            val nextYear = birthday.birthDate.withYear(today.year + 1)
            val upcoming = if (thisYear >= today) thisYear else nextYear

            val daysUntil = ChronoUnit.DAYS.between(today, upcoming).toInt() // total days

            if ((daysUntil == 0 && notificationDayOf) ||
                (daysUntil == 7 && notificationWeekBefore)) {
                result.add(birthday to daysUntil)
            }
        }

        return result
    }
}