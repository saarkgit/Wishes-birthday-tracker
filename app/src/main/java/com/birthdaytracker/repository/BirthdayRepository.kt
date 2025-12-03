package com.birthdaytracker.repository

import com.birthdaytracker.data.Birthday
import com.birthdaytracker.data.BirthdayDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BirthdayRepository @Inject constructor(private val birthdayDao: BirthdayDao) {
    fun getAllBirthdays(): Flow<List<Birthday>> = birthdayDao.getAllBirthdays()
    
    suspend fun getBirthdayById(id: Long): Birthday? = birthdayDao.getBirthdayById(id)
    
    suspend fun insertBirthday(birthday: Birthday): Long = birthdayDao.insertBirthday(birthday)
    
    suspend fun updateBirthday(birthday: Birthday) = birthdayDao.updateBirthday(birthday)
    
    suspend fun deleteBirthday(birthday: Birthday) = birthdayDao.deleteBirthday(birthday)
}

