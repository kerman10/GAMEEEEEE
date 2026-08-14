package com.example.data.repository

import com.example.data.dao.GameDao
import com.example.data.model.CodexLoreEntity
import com.example.data.model.PlayerProfileEntity
import com.example.data.model.SectorProgressEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allProgress: Flow<List<SectorProgressEntity>> = gameDao.getAllProgress()
    val completedSectorsCount: Flow<Int> = gameDao.getCompletedSectorsCount()
    val solvedPuzzlesCount: Flow<Int> = gameDao.getSolvedPuzzlesCount()
    val allLoreChapters: Flow<List<CodexLoreEntity>> = gameDao.getAllCodexChapters()
    val playerProfile: Flow<PlayerProfileEntity?> = gameDao.getPlayerProfile()

    suspend fun getSectorProgress(sectorId: Int): SectorProgressEntity? {
        return gameDao.getSectorProgress(sectorId)
    }

    suspend fun saveSectorProgress(progress: SectorProgressEntity) {
        gameDao.saveSectorProgress(progress)
    }

    suspend fun unlockChapter(chapterId: Int) {
        gameDao.unlockChapter(chapterId)
    }

    suspend fun savePlayerProfile(profile: PlayerProfileEntity) {
        gameDao.savePlayerProfile(profile)
    }
}
