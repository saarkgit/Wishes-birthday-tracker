package com.birthdaytracker.notification

import com.birthdaytracker.data.Birthday
import java.time.DateTimeException
import java.time.LocalDate
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
            val thisYear = try {
                birthday.birthMonthDay.atYear(today.year)
            } catch (e: DateTimeException) {
                LocalDate.of(today.year, 2, 28)
            }

            val nextYear = try {
                birthday.birthMonthDay.atYear(today.year + 1)
            } catch (e: DateTimeException) {
                LocalDate.of(today.year + 1, 2, 28)
            }

            val upcoming = if (thisYear >= today) thisYear else nextYear
            val daysUntil = ChronoUnit.DAYS.between(today, upcoming).toInt()

            if ((daysUntil == 0 && notificationDayOf) ||
                (daysUntil == 7 && notificationWeekBefore)) {
                result.add(birthday to daysUntil)
            }
        }

        return result
    }
}