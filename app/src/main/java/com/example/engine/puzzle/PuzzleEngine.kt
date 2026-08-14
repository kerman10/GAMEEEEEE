package com.example.engine.puzzle

import kotlin.random.Random

enum class PuzzleType(val titleSpanish: String, val instructionSpanish: String) {
    QUANTUM_CIRCUITS("Resonador de Nodos Cuánticos", "Gira los conectores hasta que todos los circuitos se alineen y transmitan energía."),
    HARMONIC_FREQUENCY("Sintonizador del Ojo", "Ajusta las 3 frecuencias cuánticas hasta igualar la onda objetivo del Códice."),
    LASER_REFLECTION("Desvío de Haz Fotónico", "Orienta los espejos para dirigir el haz láser hacia el sensor central."),
    CIPHER_SEQUENCE("Cripto-Secuencia del Libro", "Memoriza y reproduce la secuencia de runas de neón en orden."),
    LOGIC_MATRIX("Matriz de Compuertas Lógicas", "Activa los conmutadores binarios para desbloquear el flujo positivo.")
}

data class PuzzleState(
    val puzzleId: Int, // 1 to 1557
    val type: PuzzleType,
    val nodes: List<PuzzleNode>,
    val targetValues: List<Float> = emptyList(),
    val currentValues: List<Float> = emptyList(),
    val sequenceTarget: List<Int> = emptyList(),
    val currentSequence: List<Int> = emptyList(),
    val isSolved: Boolean = false
)

data class PuzzleNode(
    val id: Int,
    val rotationAngle: Float = 0f, // 0, 90, 180, 270
    val targetAngle: Float = 0f,
    val isConnected: Boolean = false,
    val label: String = ""
)

object PuzzleEngine {
    const val TOTAL_PUZZLES = 1557

    fun generatePuzzle(puzzleId: Int): PuzzleState {
        val clampedId = puzzleId.coerceIn(1, TOTAL_PUZZLES)
        val rng = Random(clampedId.toLong() * 93847L)
        val typeIndex = (clampedId - 1) % PuzzleType.values().size
        val type = PuzzleType.values()[typeIndex]

        return when (type) {
            PuzzleType.QUANTUM_CIRCUITS -> {
                val nodeCount = 4 + (clampedId % 4) // 4 to 7 nodes
                val nodes = (0 until nodeCount).map { i ->
                    val correctAngle = (rng.nextInt(4)) * 90f
                    val initialOffset = ((rng.nextInt(3) + 1) * 90f) % 360f
                    val currentAngle = (correctAngle + initialOffset) % 360f
                    PuzzleNode(
                        id = i,
                        rotationAngle = currentAngle,
                        targetAngle = correctAngle,
                        isConnected = (currentAngle == correctAngle),
                        label = "NODE-0$i"
                    )
                }
                PuzzleState(
                    puzzleId = clampedId,
                    type = type,
                    nodes = nodes,
                    isSolved = nodes.all { it.rotationAngle == it.targetAngle }
                )
            }
            PuzzleType.HARMONIC_FREQUENCY -> {
                val targetFrequencies = listOf(
                    (rng.nextInt(8) + 2) * 10f,
                    (rng.nextInt(8) + 2) * 10f,
                    (rng.nextInt(8) + 2) * 10f
                )
                val initialValues = targetFrequencies.map { (it + (rng.nextInt(5) + 1) * 10f).coerceIn(10f, 100f) }
                PuzzleState(
                    puzzleId = clampedId,
                    type = type,
                    nodes = emptyList(),
                    targetValues = targetFrequencies,
                    currentValues = initialValues,
                    isSolved = false
                )
            }
            PuzzleType.LASER_REFLECTION -> {
                val mirrorCount = 4
                val nodes = (0 until mirrorCount).map { i ->
                    val targetAngle = (rng.nextInt(4)) * 90f
                    val initialAngle = ((targetAngle + 90f)) % 360f
                    PuzzleNode(
                        id = i,
                        rotationAngle = initialAngle,
                        targetAngle = targetAngle,
                        label = "ESPEJO-${i + 1}"
                    )
                }
                PuzzleState(
                    puzzleId = clampedId,
                    type = type,
                    nodes = nodes,
                    isSolved = false
                )
            }
            PuzzleType.CIPHER_SEQUENCE -> {
                val seqLength = 3 + (clampedId % 3) // 3 to 5 sequence
                val sequence = (0 until seqLength).map { rng.nextInt(4) }
                PuzzleState(
                    puzzleId = clampedId,
                    type = type,
                    nodes = emptyList(),
                    sequenceTarget = sequence,
                    currentSequence = emptyList(),
                    isSolved = false
                )
            }
            PuzzleType.LOGIC_MATRIX -> {
                val gateCount = 4
                val nodes = (0 until gateCount).map { i ->
                    val targetState = if (rng.nextBoolean()) 180f else 0f
                    val initial = if (targetState == 0f) 180f else 0f
                    PuzzleNode(
                        id = i,
                        rotationAngle = initial,
                        targetAngle = targetState,
                        label = "GATE-${(65 + i).toChar()}"
                    )
                }
                PuzzleState(
                    puzzleId = clampedId,
                    type = type,
                    nodes = nodes,
                    isSolved = false
                )
            }
        }
    }

    fun rotateNode(state: PuzzleState, nodeId: Int): PuzzleState {
        val newNodes = state.nodes.map { node ->
            if (node.id == nodeId) {
                val newAngle = (node.rotationAngle + 90f) % 360f
                node.copy(rotationAngle = newAngle, isConnected = (newAngle == node.targetAngle))
            } else node
        }
        val isSolved = newNodes.all { it.rotationAngle == it.targetAngle }
        return state.copy(nodes = newNodes, isSolved = isSolved)
    }

    fun adjustFrequency(state: PuzzleState, index: Int, newValue: Float): PuzzleState {
        val newValues = state.currentValues.toMutableList()
        if (index in newValues.indices) {
            newValues[index] = newValue
        }
        val isSolved = newValues.indices.all { i ->
            kotlin.math.abs(newValues[i] - state.targetValues[i]) < 5f
        }
        return state.copy(currentValues = newValues, isSolved = isSolved)
    }

    fun inputSequenceItem(state: PuzzleState, symbolIndex: Int): PuzzleState {
        val newSeq = state.currentSequence + symbolIndex
        if (newSeq.size > state.sequenceTarget.size) {
            return state.copy(currentSequence = listOf(symbolIndex))
        }
        val matchesSoFar = newSeq.indices.all { i -> newSeq[i] == state.sequenceTarget[i] }
        if (!matchesSoFar) {
            return state.copy(currentSequence = emptyList()) // Reset on error
        }
        val isSolved = newSeq.size == state.sequenceTarget.size
        return state.copy(currentSequence = newSeq, isSolved = isSolved)
    }
}
