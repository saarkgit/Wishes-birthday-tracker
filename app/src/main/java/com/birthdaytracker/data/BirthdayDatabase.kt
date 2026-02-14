package com.birthdaytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Birthday::class], version = 2, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class BirthdayDatabase : RoomDatabase() {
    abstract fun birthdayDao(): BirthdayDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE birthdays_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        birthMonthDay TEXT NOT NULL,
                        birthYear INTEGER,
                        category TEXT NOT NULL
                    )
                """
                )

                db.execSQL(
                    """
                    INSERT INTO birthdays_new (id, name, birthMonthDay, birthYear, category)
                    SELECT id, name,
                        '--' || substr('0' || CAST(strftime('%m', datetime(birthDate / 1000, 'unixepoch')) AS TEXT), -2) || 
                        '-' || substr('0' || CAST(strftime('%d', datetime(birthDate / 1000, 'unixepoch')) AS TEXT), -2),
                        CAST(strftime('%Y', datetime(birthDate / 1000, 'unixepoch')) AS INTEGER),
                        category
                    FROM birthdays
                """
                )

                db.execSQL("DROP TABLE birthdays")
                db.execSQL("ALTER TABLE birthdays_new RENAME TO birthdays")
            }
        }

        @Volatile
        private var INSTANCE: BirthdayDatabase? = null

        fun getDatabase(context: Context): BirthdayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BirthdayDatabase::class.java,
                    "birthday_database"
                ).addMigrations(MIGRATION_1_2)  // Add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

