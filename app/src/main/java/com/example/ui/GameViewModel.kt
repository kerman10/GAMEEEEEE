package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CyberAudioEngine
import com.example.data.database.AppDatabase
import com.example.data.model.CodexLoreEntity
import com.example.data.model.PlayerProfileEntity
import com.example.data.model.SectorProgressEntity
import com.example.data.repository.GameRepository
import com.example.engine.physics.ParkourPhysicsEngine
import com.example.engine.physics.PlayerState
import com.example.engine.puzzle.PuzzleEngine
import com.example.engine.puzzle.PuzzleState
import com.example.engine.world.BlockType
import com.example.engine.world.DistrictType
import com.example.engine.world.SectorDefinition
import com.example.engine.world.SectorEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class CurrentScreen {
    MAIN_MENU,
    IN_GAME,
    SECTOR_SELECT,
    CODEX_VAULT,
    GLOVE_CUSTOMIZER
}

data class ActiveGameState(
    val currentSector: SectorDefinition,
    val timeElapsedSec: Float = 0f,
    val isPaused: Boolean = false,
    val isCleared: Boolean = false,
    val activePuzzle: PuzzleState? = null,
    val isPuzzleOpen: Boolean = false,
    val bookSpeechText: String = "",
    val bookSpeechVisible: Boolean = true,
    val earnedStars: Int = 0,
    val unlockedChapterTitle: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    val physics = ParkourPhysicsEngine()

    private val _currentScreen = MutableStateFlow(CurrentScreen.MAIN_MENU)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    private val _activeGame = MutableStateFlow(
        ActiveGameState(
            currentSector = SectorEngine.generateSector(1),
            bookSpeechText = SectorEngine.generateSector(1).bookVoiceQuote
        )
    )
    val activeGame: StateFlow<ActiveGameState> = _activeGame.asStateFlow()

    val allProgress: StateFlow<List<SectorProgressEntity>>
    val allLoreChapters: StateFlow<List<CodexLoreEntity>>
    val playerProfile: StateFlow<PlayerProfileEntity?>
    val completedSectorsCount: StateFlow<Int>
    val solvedPuzzlesCount: StateFlow<Int>

    private var gameLoopJob: Job? = null
    private var joystickX = 0f
    private var joystickZ = 0f

    init {
        val database = AppDatabase.getInstance(application)
        repository = GameRepository(database.gameDao())

        allProgress = repository.allProgress.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allLoreChapters = repository.allLoreChapters.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        playerProfile = repository.playerProfile.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        completedSectorsCount = repository.completedSectorsCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        solvedPuzzlesCount = repository.solvedPuzzlesCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
    }

    fun navigateTo(screen: CurrentScreen) {
        _currentScreen.value = screen
        if (screen == CurrentScreen.IN_GAME) {
            startGameLoop()
        } else {
            stopGameLoop()
        }
    }

    fun loadSector(sectorId: Int) {
        val def = SectorEngine.generateSector(sectorId)
        physics.resetPlayer(def.startPosition)
        _activeGame.value = ActiveGameState(
            currentSector = def,
            bookSpeechText = def.bookVoiceQuote,
            bookSpeechVisible = true
        )
        CyberAudioEngine.playBookWhisperSound()
        navigateTo(CurrentScreen.IN_GAME)
    }

    fun setJoystickInput(x: Float, z: Float) {
        joystickX = x.coerceIn(-1f, 1f)
        joystickZ = z.coerceIn(-1f, 1f)
    }

    fun addLookDelta(deltaYaw: Float, deltaPitch: Float) {
        physics.player.yaw = (physics.player.yaw + deltaYaw) % 360f
        physics.player.pitch = (physics.player.pitch - deltaPitch).coerceIn(-50f, 50f)
    }

    fun onJumpPressed() {
        physics.jump()
    }

    fun onSlidePressed() {
        physics.slide()
    }

    fun onGrapplePressed() {
        physics.tryGrapple(_activeGame.value.currentSector.blocks)
    }

    fun onInteractPressed() {
        val blocks = _activeGame.value.currentSector.blocks
        val playerPos = physics.player.position

        // Check if near puzzle terminal
        val terminal = blocks.firstOrNull {
            it.type == BlockType.PUZZLE_TERMINAL &&
                    abs(it.position.z - playerPos.z) < 4.5f &&
                    abs(it.position.x - playerPos.x) < 3.5f
        }

        if (terminal != null && terminal.puzzleId != null) {
            val puzzle = PuzzleEngine.generatePuzzle(terminal.puzzleId)
            _activeGame.value = _activeGame.value.copy(
                activePuzzle = puzzle,
                isPuzzleOpen = true
            )
            CyberAudioEngine.playTerminalClick()
            triggerBookDialogue("Examinando fragmento de memoria #${terminal.puzzleId}... Descodifica el nodo.")
        }
    }

    fun rotatePuzzleNode(nodeId: Int) {
        val current = _activeGame.value.activePuzzle ?: return
        val updated = PuzzleEngine.rotateNode(current, nodeId)
        _activeGame.value = _activeGame.value.copy(activePuzzle = updated)
        CyberAudioEngine.playTerminalClick()
        if (updated.isSolved) {
            onPuzzleSolved(updated.puzzleId)
        }
    }

    fun adjustPuzzleFrequency(index: Int, value: Float) {
        val current = _activeGame.value.activePuzzle ?: return
        val updated = PuzzleEngine.adjustFrequency(current, index, value)
        _activeGame.value = _activeGame.value.copy(activePuzzle = updated)
        if (updated.isSolved) {
            onPuzzleSolved(updated.puzzleId)
        }
    }

    fun inputPuzzleSequence(symbolIndex: Int) {
        val current = _activeGame.value.activePuzzle ?: return
        val updated = PuzzleEngine.inputSequenceItem(current, symbolIndex)
        _activeGame.value = _activeGame.value.copy(activePuzzle = updated)
        CyberAudioEngine.playTerminalClick()
        if (updated.isSolved) {
            onPuzzleSolved(updated.puzzleId)
        }
    }

    fun closePuzzle() {
        _activeGame.value = _activeGame.value.copy(isPuzzleOpen = false)
    }

    private fun onPuzzleSolved(puzzleId: Int) {
        CyberAudioEngine.playSuccessChord()
        triggerBookDialogue("¡Enigma #$puzzleId descifrado! Mis circuitos resuenan con tu inteligencia.")
        viewModelScope.launch {
            delay(1200)
            _activeGame.value = _activeGame.value.copy(isPuzzleOpen = false)
        }
    }

    fun triggerBookDialogue(text: String) {
        _activeGame.value = _activeGame.value.copy(
            bookSpeechText = text,
            bookSpeechVisible = true
        )
        CyberAudioEngine.playBookWhisperSound()
    }

    fun equipGloveSkin(skinIndex: Int) {
        viewModelScope.launch {
            val current = playerProfile.value ?: PlayerProfileEntity()
            repository.savePlayerProfile(current.copy(equippedSkinIndex = skinIndex))
        }
    }

    fun toggleMute(muted: Boolean) {
        CyberAudioEngine.setMuted(muted)
        viewModelScope.launch {
            val current = playerProfile.value ?: PlayerProfileEntity()
            repository.savePlayerProfile(current.copy(soundEnabled = !muted))
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                val game = _activeGame.value
                if (!game.isPaused && !game.isCleared && !game.isPuzzleOpen) {
                    physics.update(
                        dt = dt,
                        moveX = joystickX,
                        moveZ = joystickZ,
                        blocks = game.currentSector.blocks
                    )

                    val newTime = game.timeElapsedSec + dt
                    _activeGame.value = game.copy(timeElapsedSec = newTime)

                    // Check Exit Portal Collision
                    val exitPos = game.currentSector.exitPosition
                    val pPos = physics.player.position
                    val distToExit = abs(exitPos.z - pPos.z) < 3.5f && abs(exitPos.x - pPos.x) < 3f && abs(exitPos.y - pPos.y) < 4f

                    if (distToExit) {
                        handleSectorCleared(game.currentSector, newTime)
                    }
                }
                delay(16) // ~60 FPS update
            }
        }
    }

    private fun handleSectorCleared(sector: SectorDefinition, timeTaken: Float) {
        val stars = when {
            timeTaken <= sector.parTimeSec * 0.8f -> 3
            timeTaken <= sector.parTimeSec * 1.2f -> 2
            else -> 1
        }
        val isPuzzleSolved = (_activeGame.value.activePuzzle?.isSolved == true) || (sector.puzzleId == null)
        val score = (stars * 1000) + ((sector.parTimeSec - timeTaken).coerceAtLeast(0f) * 50).toInt()

        _activeGame.value = _activeGame.value.copy(
            isCleared = true,
            earnedStars = stars
        )
        CyberAudioEngine.playSuccessChord()
        triggerBookDialogue("Sector ${sector.sectorId} superado con $stars estrellas. Has demostrado valía.")

        viewModelScope.launch {
            // Save to Room
            repository.saveSectorProgress(
                SectorProgressEntity(
                    sectorId = sector.sectorId,
                    districtId = sector.district.id,
                    districtName = sector.district.districtName,
                    stars = stars,
                    bestTimeSec = timeTaken,
                    isCompleted = true,
                    isPuzzleSolved = isPuzzleSolved,
                    score = score,
                    completedAt = System.currentTimeMillis()
                )
            )

            // Unlock next lore chapter if reached milestone
            val chapters = repository.allLoreChapters.stateIn(viewModelScope).value
            val toUnlock = chapters.firstOrNull { !it.isUnlocked && it.triggerSectorId <= sector.sectorId }
            if (toUnlock != null) {
                repository.unlockChapter(toUnlock.chapterId)
                _activeGame.value = _activeGame.value.copy(unlockedChapterTitle = toUnlock.title)
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
