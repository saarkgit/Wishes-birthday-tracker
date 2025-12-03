package com.birthdaytracker.repository

import app.cash.turbine.test
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.data.BirthdayDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import kotlin.test.assertEquals

class BirthdayRepositoryTest {

    private lateinit var dao: BirthdayDao
    private lateinit var repository: BirthdayRepository

    @Before
    fun setup() {
        dao = mock()
        repository = BirthdayRepository(dao)
    }

    @Test
    fun `getAllBirthdays returns flow from dao`() = runTest {
        val birthdays = listOf(
            Birthday(1, "Alice", LocalDate.now().minusYears(25), "Friend"),
            Birthday(2, "Bob", LocalDate.now().minusYears(30), "Family")
        )

        whenever(dao.getAllBirthdays()).thenReturn(flowOf(birthdays))

        repository.getAllBirthdays().test {
            assertEquals(birthdays, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getBirthdayById calls dao`() = runTest {
        val birthday = Birthday(1, "Alice", LocalDate.now().minusYears(25), "Friend")

        whenever(dao.getBirthdayById(1)).thenReturn(birthday)

        val result = repository.getBirthdayById(1)

        assertEquals(birthday, result)
        verify(dao).getBirthdayById(1)
    }

    @Test
    fun `insertBirthday calls dao`() = runTest {
        val birthday = Birthday(0, "Alice", LocalDate.now().minusYears(25), "Friend")

        whenever(dao.insertBirthday(birthday)).thenReturn(1L)

        val result = repository.insertBirthday(birthday)

        assertEquals(1L, result)
        verify(dao).insertBirthday(birthday)
    }

    @Test
    fun `updateBirthday calls dao`() = runTest {
        val birthday = Birthday(1, "Alice", LocalDate.now().minusYears(25), "Friend")

        repository.updateBirthday(birthday)

        verify(dao).updateBirthday(birthday)
    }

    @Test
    fun `deleteBirthday calls dao`() = runTest {
        val birthday = Birthday(1, "Alice", LocalDate.now().minusYears(25), "Friend")

        repository.deleteBirthday(birthday)

        verify(dao).deleteBirthday(birthday)
    }
}