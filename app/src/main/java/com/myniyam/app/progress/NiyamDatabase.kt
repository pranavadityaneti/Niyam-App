package com.myniyam.app.progress

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(
    tableName = "read_events",
    indices = [Index("mantraId", "epochDay"), Index("epochDay")]
)
data class ReadEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mantraId: String,
    val epochDay: Long,
    val timestampMs: Long
)

@Dao
interface ReadEventDao {
    @Insert
    suspend fun insert(event: ReadEventEntity)

    @Query("SELECT COUNT(DISTINCT epochDay) FROM read_events WHERE mantraId = :mantraId AND epochDay >= :sinceEpochDay")
    suspend fun distinctDaysFor(mantraId: String, sinceEpochDay: Long): Int

    @Query("SELECT DISTINCT epochDay FROM read_events")
    suspend fun allReadDays(): List<Long>

    @Query("SELECT COUNT(*) FROM read_events WHERE epochDay = :epochDay")
    suspend fun countOn(epochDay: Long): Int
}

@Database(entities = [ReadEventEntity::class], version = 1, exportSchema = false)
abstract class NiyamDatabase : RoomDatabase() {
    abstract fun readEventDao(): ReadEventDao

    companion object {
        @Volatile private var instance: NiyamDatabase? = null

        fun get(context: Context): NiyamDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NiyamDatabase::class.java,
                    "niyam.db"
                ).build().also { instance = it }
            }
    }
}
