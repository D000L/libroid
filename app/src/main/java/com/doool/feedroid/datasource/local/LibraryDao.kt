package com.doool.feedroid.datasource.local

import androidx.room.*

@Dao
interface LibraryDao {
    @Query("SELECT * FROM LibraryEntity")
    suspend fun getAll(): List<LibraryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg library: LibraryEntity)

    @Delete
    suspend fun delete(user: LibraryEntity)
}
