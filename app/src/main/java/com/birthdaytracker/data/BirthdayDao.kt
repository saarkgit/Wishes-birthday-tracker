package com.birthdaytracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {
    @Query("SELECT * FROM birthdays ORDER BY birthDate ASC")
    fun getAllBirthdays(): Flow<List<Birthday>>
    
    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getBirthdayById(id: Long): Birthday?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: Birthday): Long
    
    @Update
    suspend fun updateBirthday(birthday: Birthday)
    
    @Delete
    suspend fun deleteBirthday(birthday: Birthday)
}

