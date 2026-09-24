package com.moodtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Insert
    suspend fun insertRecord(record: MoodRecord): Long

    @Update
    suspend fun updateRecord(record: MoodRecord)

    @Delete
    suspend fun deleteRecord(record: MoodRecord)

    @Query("DELETE FROM mood_records")
    suspend fun deleteAllRecords()

    @Query("SELECT * FROM mood_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MoodRecord>>

    @Query("SELECT * FROM mood_records WHERE type = :type ORDER BY timestamp DESC")
    fun getRecordsByType(type: String): Flow<List<MoodRecord>>

    @Query("SELECT * FROM mood_records WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    fun getRecordsBetween(start: Long, end: Long): Flow<List<MoodRecord>>

    @Query("SELECT * FROM mood_records WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    suspend fun getRecordsBetweenList(start: Long, end: Long): List<MoodRecord>

    @Query("SELECT * FROM mood_records WHERE id = :id")
    suspend fun getRecordById(id: Long): MoodRecord?

    @Query("SELECT * FROM mood_records ORDER BY timestamp DESC")
    suspend fun getAllRecordsList(): List<MoodRecord>

    @Query("SELECT COUNT(*) FROM mood_records WHERE timestamp >= :start AND timestamp < :end")
    suspend fun getRecordCountBetween(start: Long, end: Long): Int
}
