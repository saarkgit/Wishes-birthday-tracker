package com.birthdaytracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.repository.BirthdayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class BirthdayViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: BirthdayRepository
    private lateinit var viewModel: BirthdayViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()

        // Default mock behavior
        whenever(repository.getAllBirthdays()).thenReturn(flowOf(emptyList()))

        viewModel = BirthdayViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `validateBirthday returns error for blank name`() {
        val result = viewModel.validateBirthday("", LocalDate.now().minusYears(20))
        assertEquals("Name cannot be empty", result)
    }

    @Test
    fun `validateBirthday returns error for short name`() {
        val result = viewModel.validateBirthday("A", LocalDate.now().minusYears(20))
        assertEquals("Name must be at least 2 characters", result)
    }

    @Test
    fun `validateBirthday returns error for long name`() {
        val longName = "A".repeat(101)
        val result = viewModel.validateBirthday(longName, LocalDate.now().minusYears(20))
        assertEquals("Name must be less than 100 characters", result)
    }

    @Test
    fun `validateBirthday returns error for future date`() {
        val result = viewModel.validateBirthday("John Doe", LocalDate.now().plusDays(1))
        assertEquals("Birth date cannot be in the future", result)
    }

    @Test
    fun `validateBirthday returns error for very old date`() {
        val result = viewModel.validateBirthday("John Doe", LocalDate.now().minusYears(151))
        assertEquals("Birth date seems invalid", result)
    }

    @Test
    fun `validateBirthday returns null for valid input`() {
        val result = viewModel.validateBirthday("John Doe", LocalDate.now().minusYears(25))
        assertNull(result)
    }

    @Test
    fun `isToday returns true for birthday today`() = runTest {
        val today = LocalDate.now()
        val birthday = Birthday(
            id = 1,
            name = "John",
            birthDate = today.minusYears(25),
            category = "Friend"
        )

        val result = viewModel.isToday(birthday)
        assertEquals(true, result)
    }

    @Test
    fun `isToday returns false for birthday not today`() = runTest {
        val birthday = Birthday(
            id = 1,
            name = "John",
            birthDate = LocalDate.now().minusDays(1).minusYears(25),
            category = "Friend"
        )

        val result = viewModel.isToday(birthday)
        assertEquals(false, result)
    }

    @Test
    fun `getNextUpcomingBirthday returns nearest birthday`() = runTest {
        val today = LocalDate.now()
        val birthdays = listOf(
            Birthday(1, "Alice", today.plusDays(10), "Friend"),
            Birthday(2, "Bob", today.plusDays(5), "Family"),
            Birthday(3, "Charlie", today.minusDays(1), "Work")
        )

        val result = viewModel.getNextUpcomingBirthday(birthdays)
        assertEquals("Bob", result?.name)
    }

    @Test
    fun `getNextUpcomingBirthday returns null for empty list`() = runTest {
        val result = viewModel.getNextUpcomingBirthday(emptyList())
        assertNull(result)
    }

    @Test
    fun `insertBirthday calls repository and shows success message`() = runTest {
        val birthday = Birthday(0, "John Doe", LocalDate.now().minusYears(25), "Friend")

        whenever(repository.insertBirthday(any())).thenReturn(1L)

        viewModel.insertBirthday(birthday)
        advanceUntilIdle()

        verify(repository).insertBirthday(birthday)

        viewModel.successMessage.test {
            val message = awaitItem()
            assertNotNull(message)
            assertEquals("Birthday added successfully", message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertBirthday shows error message on failure`() = runTest {
        val birthday = Birthday(0, "John Doe", LocalDate.now().minusYears(25), "Friend")

        whenever(repository.insertBirthday(any())).thenThrow(RuntimeException("Database error"))

        viewModel.insertBirthday(birthday)
        advanceUntilIdle()

        viewModel.errorMessage.test {
            val message = awaitItem()
            assertNotNull(message)
            assertEquals(true, message?.contains("Failed to add birthday"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateBirthday calls repository`() = runTest {
        val birthday = Birthday(1, "John Doe", LocalDate.now().minusYears(25), "Friend")

        viewModel.updateBirthday(birthday)
        advanceUntilIdle()

        verify(repository).updateBirthday(birthday)
    }

    @Test
    fun `deleteBirthday calls repository`() = runTest {
        val birthday = Birthday(1, "John Doe", LocalDate.now().minusYears(25), "Friend")

        viewModel.deleteBirthday(birthday)
        advanceUntilIdle()

        verify(repository).deleteBirthday(birthday)
    }

    @Test
    fun `setSortOption updates sort option`() = runTest {
        viewModel.setSortOption(SortOption.NAME)

        viewModel.sortOption.test {
            assertEquals(SortOption.NAME, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}