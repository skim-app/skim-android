package com.example.skim.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "recording_cache")
data class RecordingCacheEntity(
    @PrimaryKey val id: String,
    val payload: String,
    val createdAt: String,
)

@Dao
interface RecordingCacheDao {
    @Query("SELECT * FROM recording_cache ORDER BY createdAt DESC")
    fun observe(): Flow<List<RecordingCacheEntity>>

    @Query("DELETE FROM recording_cache")
    suspend fun clear()

    @Upsert
    suspend fun upsertAll(values: List<RecordingCacheEntity>)

    @Transaction
    suspend fun replace(values: List<RecordingCacheEntity>) {
        clear()
        upsertAll(values)
    }
}

@Database(entities = [RecordingCacheEntity::class], version = 2, exportSchema = true)
abstract class SkimDatabase : RoomDatabase() {
    abstract fun recordingCacheDao(): RecordingCacheDao

    companion object {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recording_cache ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
