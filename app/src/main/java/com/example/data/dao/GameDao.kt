package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CodexLoreEntity
import com.example.data.model.PlayerProfileEntity
import com.example.data.model.SectorProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM sector_progress")
    fun getAllProgress(): Flow<List<SectorProgressEntity>>

    @Query("SELECT * FROM sector_progress WHERE sectorId = :sectorId LIMIT 1")
    suspend fun getSectorProgress(sectorId: Int): SectorProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSectorProgress(progress: SectorProgressEntity)

    @Query("SELECT COUNT(*) FROM sector_progress WHERE isCompleted = 1")
    fun getCompletedSectorsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sector_progress WHERE isPuzzleSolved = 1")
    fun getSolvedPuzzlesCount(): Flow<Int>

    @Query("SELECT * FROM codex_lore ORDER BY chapterId ASC")
    fun getAllCodexChapters(): Flow<List<CodexLoreEntity>>

    @Query("SELECT * FROM codex_lore WHERE isUnlocked = 1 ORDER BY chapterId ASC")
    fun getUnlockedCodexChapters(): Flow<List<CodexLoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoreChapters(chapters: List<CodexLoreEntity>)

    @Query("UPDATE codex_lore SET isUnlocked = 1 WHERE chapterId = :chapterId")
    suspend fun unlockChapter(chapterId: Int)

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)
}
