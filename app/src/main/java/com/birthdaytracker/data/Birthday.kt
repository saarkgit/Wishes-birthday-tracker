package com.birthdaytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.MonthDay

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int? = null,
    val category: String = ""
) {
    // Helper to get full date when year is known
    val birthDate: LocalDate?
        get() = birthYear?.let { LocalDate.of(it, birthMonth, birthDay) }

    val birthMonthDay: MonthDay
        get() = MonthDay.of(birthMonth, birthDay)
}


