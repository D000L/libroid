package com.doool.libroid.data.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.doool.libroid.data.datasource.entity.LibraryEntity

@Database(entities = [LibraryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(applicationContext: Context): AppDatabase {
            return instance ?: run {
                val newInstance = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "library-database"
                ).build()
                instance = newInstance
                newInstance
            }
        }
    }
}


