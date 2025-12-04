package com.birthdaytracker

import com.birthdaytracker.data.Birthday
import java.time.LocalDate
import kotlin.random.Random

/**
 * Utility functions for testing
 * Location: src/androidTest/java/com/birthdaytracker/TestUtils.kt
 */
object TestUtils {

    /**
     * Creates a sample birthday for testing
     */
    fun createTestBirthday(
        id: Long = 0,
        name: String = "Test Person",
        birthDate: LocalDate = LocalDate.now().minusYears(25),
        category: String = "Friend"
    ): Birthday {
        return Birthday(
            id = id,
            name = name,
            birthDate = birthDate,
            category = category
        )
    }

    /**
     * Creates a list of test birthdays with various dates
     */
    fun createTestBirthdays(count: Int): List<Birthday> {
        return (1..count).map { i ->
            Birthday(
                id = i.toLong(),
                name = "Person $i",
                birthDate = LocalDate.now().minusYears((20 + i % 50).toLong())
                    .plusDays((i * 10 % 365).toLong()),
                category = listOf("Friend", "Family", "Work", "Other")[i % 4]
            )
        }
    }

    /**
     * Creates a birthday that occurs today
     */
    fun createBirthdayToday(
        name: String = "Birthday Today",
        yearsAgo: Long = 25,
        category: String = "Friend"
    ): Birthday {
        val today = LocalDate.now()
        return Birthday(
            id = 0,
            name = name,
            birthDate = today.minusYears(yearsAgo),
            category = category
        )
    }

    /**
     * Creates a birthday that occurs in N days
     */
    fun createBirthdayInDays(
        daysFromNow: Long,
        name: String = "Future Birthday",
        yearsAgo: Long = 30,
        category: String = "Family"
    ): Birthday {
        val futureDate = LocalDate.now().plusDays(daysFromNow)
        return Birthday(
            id = 0,
            name = name,
            birthDate = futureDate.minusYears(yearsAgo),
            category = category
        )
    }

    /**
     * Creates a birthday that occurred N days ago
     */
    fun createBirthdayDaysAgo(
        daysAgo: Long,
        name: String = "Past Birthday",
        yearsAgo: Long = 28,
        category: String = "Work"
    ): Birthday {
        val pastDate = LocalDate.now().minusDays(daysAgo)
        return Birthday(
            id = 0,
            name = name,
            birthDate = pastDate.minusYears(yearsAgo),
            category = category
        )
    }

    /**
     * Creates a birthday on a specific month and day
     */
    fun createBirthdayOnDate(
        month: Int,
        day: Int,
        name: String = "Specific Date Birthday",
        year: Int = 1990,
        category: String = "Friend"
    ): Birthday {
        return Birthday(
            id = 0,
            name = name,
            birthDate = LocalDate.of(year, month, day),
            category = category
        )
    }

    /**
     * Creates a birthday with random data for stress testing
     */
    fun createRandomBirthday(id: Long = 0): Birthday {
        val names = listOf(
            "Alice", "Bob", "Charlie", "Diana", "Emma", "Frank",
            "Grace", "Henry", "Ivy", "Jack", "Kate", "Leo"
        )
        val categories = listOf("Friend", "Family", "Work", "Other", "")

        val randomYear = Random.nextInt(1920, 2020)
        val randomMonth = Random.nextInt(1, 13)
        val randomDay = Random.nextInt(1, 29) // Safe for all months

        return Birthday(
            id = id,
            name = names.random() + " " + Random.nextInt(1, 100),
            birthDate = LocalDate.of(randomYear, randomMonth, randomDay),
            category = categories.random()
        )
    }

    /**
     * Creates a birthday with special characters in the name
     */
    fun createBirthdayWithSpecialChars(
        name: String = "O'Brien-François",
        category: String = "Friend"
    ): Birthday {
        return Birthday(
            id = 0,
            name = name,
            birthDate = LocalDate.now().minusYears(30),
            category = category
        )
    }

    /**
     * Creates a birthday with a very long name (for testing limits)
     */
    fun createBirthdayWithLongName(): Birthday {
        val longName = "A".repeat(100)
        return Birthday(
            id = 0,
            name = longName,
            birthDate = LocalDate.now().minusYears(25),
            category = "Test"
        )
    }

    /**
     * Creates a birthday on leap day (Feb 29)
     */
    fun createLeapDayBirthday(
        name: String = "Leap Day Baby",
        year: Int = 2000
    ): Birthday {
        return Birthday(
            id = 0,
            name = name,
            birthDate = LocalDate.of(year, 2, 29),
            category = "Special"
        )
    }

    /**
     * Creates birthdays that all occur in the current month
     */
    fun createBirthdaysThisMonth(count: Int): List<Birthday> {
        val today = LocalDate.now()
        return (1..count).map { i ->
            Birthday(
                id = i.toLong(),
                name = "Person $i",
                birthDate = LocalDate.of(
                    1990,
                    today.monthValue,
                    (i % 28) + 1 // Safe day for all months
                ),
                category = "Friend"
            )
        }
    }

    /**
     * Asserts that two birthdays are equal (ignoring ID)
     */
    fun assertBirthdaysEqual(expected: Birthday, actual: Birthday, message: String = "") {
        assert(expected.name == actual.name) { "$message: Names don't match" }
        assert(expected.birthDate == actual.birthDate) { "$message: Birth dates don't match" }
        assert(expected.category == actual.category) { "$message: Categories don't match" }
    }

    /**
     * Waits for a condition to be true (useful for async operations)
     */
    suspend fun waitUntil(
        timeoutMs: Long = 5000,
        intervalMs: Long = 100,
        condition: suspend () -> Boolean
    ) {
        val startTime = System.currentTimeMillis()
        while (!condition()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw AssertionError("Condition not met within timeout")
            }
            kotlinx.coroutines.delay(intervalMs)
        }
    }
}

/**
 * Extension function to quickly create test data
 */
fun Int.testBirthdays(): List<Birthday> {
    return TestUtils.createTestBirthdays(this)
}

/**
 * Extension function to check if a birthday is today
 */
fun Birthday.isToday(): Boolean {
    val today = LocalDate.now()
    return this.birthDate.month == today.month &&
            this.birthDate.dayOfMonth == today.dayOfMonth
}

/**
 * Extension function to get days until next birthday
 */
fun Birthday.daysUntilNext(): Long {
    val today = LocalDate.now()
    val thisYear = this.birthDate.withYear(today.year)
    val nextYear = this.birthDate.withYear(today.year + 1)
    val nextOccurrence = if (thisYear >= today) thisYear else nextYear
    return java.time.temporal.ChronoUnit.DAYS.between(today, nextOccurrence)
}