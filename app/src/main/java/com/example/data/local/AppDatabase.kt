package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_articles ORDER BY timestamp DESC")
    fun getAllNews(): Flow<List<NewsArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<NewsArticleEntity>)

    @Query("DELETE FROM news_articles")
    suspend fun clearNews()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM market_events ORDER BY timestamp ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM market_events")
    suspend fun getEventsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT * FROM market_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Query("DELETE FROM market_events")
    suspend fun clearEvents()
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items")
    fun getWatchlist(): Flow<List<WatchlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_items WHERE id = :id")
    suspend fun deleteWatchlistItem(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE id = :id)")
    fun isWatchlisted(id: String): Flow<Boolean>
}

@Database(
    entities = [NewsArticleEntity::class, EventEntity::class, WatchlistItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
    abstract fun eventDao(): EventDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crypto_news_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
