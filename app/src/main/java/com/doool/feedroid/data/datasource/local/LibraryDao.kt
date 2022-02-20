package com.doool.feedroid.data.datasource.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM LibraryEntity")
    fun getAll(): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM LibraryEntity WHERE `group` = :group")
    fun getAllByGroup(group: String): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM LibraryEntity WHERE `name` = :name")
    fun getAllByName(name: String): Flow<List<LibraryEntity>>

    @Query("SELECT `group` FROM LibraryEntity GROUP BY `group`")
    suspend fun getAllGroupName(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg library: LibraryEntity)

    @Delete
    suspend fun delete(user: LibraryEntity)
}
