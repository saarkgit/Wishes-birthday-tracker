package com.birthdaytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {
    @Query("SELECT * FROM birthdays ORDER BY birthMonth ASC, birthDay ASC")
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
