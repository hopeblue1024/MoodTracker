package com.moodtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    // ════════ 心情记录 ════════

    @Insert
    suspend fun insertRecord(record: MoodRecord): Long

    @Update
    suspend fun updateRecord(record: MoodRecord)

    @Delete
    suspend fun deleteRecord(record: MoodRecord)

    @Query("SELECT * FROM mood_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MoodRecord>>

    @Query("SELECT * FROM mood_records WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    fun getRecordsBetween(start: Long, end: Long): Flow<List<MoodRecord>>

    @Query("SELECT * FROM mood_records WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    suspend fun getRecordsBetweenList(start: Long, end: Long): List<MoodRecord>

    @Query("SELECT * FROM mood_records WHERE id = :id")
    suspend fun getRecordById(id: Long): MoodRecord?

    @Query("SELECT * FROM mood_records ORDER BY timestamp DESC")
    suspend fun getAllRecordsList(): List<MoodRecord>

    // ════════ 自定义标签 ════════

    @Insert
    suspend fun insertTag(tag: CustomTag): Long

    @Delete
    suspend fun deleteTag(tag: CustomTag)

    @Query("SELECT * FROM custom_tags ORDER BY id")
    fun getAllTags(): Flow<List<CustomTag>>

    @Query("SELECT * FROM custom_tags ORDER BY id")
    suspend fun getAllTagsList(): List<CustomTag>
}
