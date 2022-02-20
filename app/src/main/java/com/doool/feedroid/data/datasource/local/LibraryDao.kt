package com.doool.feedroid.data.datasource.local

import androidx.room.*

@Dao
interface LibraryDao {
    @Query("SELECT * FROM LibraryEntity")
    suspend fun getAll(): List<LibraryEntity>

    @Query("SELECT * FROM LibraryEntity WHERE `group` = :group")
    suspend fun getAllByGroup(group: String): List<LibraryEntity>

    @Query("SELECT * FROM LibraryEntity WHERE `name` = :name")
    suspend fun getAllByName(name: String): List<LibraryEntity>

    @Query("SELECT `group` FROM LibraryEntity GROUP BY `group`")
    suspend fun getAllGroupName(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg library: LibraryEntity)

    @Delete
    suspend fun delete(user: LibraryEntity)
}
