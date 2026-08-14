package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sector_progress")
data class SectorProgressEntity(
    @PrimaryKey val sectorId: Int, // 1 to 2344
    val districtId: Int,          // 1 to 8
    val districtName: String,
    val stars: Int = 0,           // 0 to 3
    val bestTimeSec: Float = 0f,
    val isCompleted: Boolean = false,
    val isPuzzleSolved: Boolean = false,
    val score: Int = 0,
    val completedAt: Long = 0L
)

@Entity(tableName = "codex_lore")
data class CodexLoreEntity(
    @PrimaryKey val chapterId: Int, // 1 to 64
    val title: String,
    val originEra: String,
    val contentSpanish: String,
    val isUnlocked: Boolean = false,
    val triggerSectorId: Int = 1
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val runnerTag: String = "RUNNER-77",
    val totalScore: Int = 0,
    val totalSectorsCleared: Int = 0,
    val totalPuzzlesSolved: Int = 0,
    val maxParkourCombo: Int = 0,
    val equippedSkinIndex: Int = 0,
    val soundEnabled: Boolean = true,
    val fovWarpEnabled: Boolean = true,
    val scanlinesEnabled: Boolean = true
)
