package com.birthdaytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.repository.BirthdayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

enum class SortOption {
    DATE, NAME, CATEGORY
}

class BirthdayViewModel(private val repository: BirthdayRepository) : ViewModel() {
    val birthdays = repository.getAllBirthdays()
    
    private val _sortOption = MutableStateFlow(SortOption.DATE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
    
    suspend fun getBirthdayById(id: Long): Birthday? {
        return repository.getBirthdayById(id)
    }
    
    fun insertBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.insertBirthday(birthday)
        }
    }
    
    fun updateBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.updateBirthday(birthday)
        }
    }
    
    fun deleteBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.deleteBirthday(birthday)
        }
    }
    
    fun getNextUpcomingBirthday(birthdays: List<Birthday>): Birthday? {
        val today = LocalDate.now()
        return birthdays
            .filter { birthday ->
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear
                upcoming >= today
            }
            .minByOrNull { birthday ->
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear
                Period.between(today, upcoming).days
            }
    }
    
    fun isToday(birthday: Birthday): Boolean {
        val today = LocalDate.now()
        return birthday.birthDate.month == today.month && 
               birthday.birthDate.dayOfMonth == today.dayOfMonth
    }
}

class BirthdayViewModelFactory(private val repository: BirthdayRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BirthdayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BirthdayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

