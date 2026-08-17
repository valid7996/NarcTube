package com.narctube.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.narctube.app.data.model.DownloadItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: Long): DownloadItem?

    @Insert
    suspend fun insert(item: DownloadItem): Long

    @Update
    suspend fun update(item: DownloadItem)

    @Delete
    suspend fun delete(item: DownloadItem)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)
}
