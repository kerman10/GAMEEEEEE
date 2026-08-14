package com.example.engine.world

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object SectorEngine {
    const val TOTAL_SECTORS = 2344

    private val BOOK_QUOTES = listOf(
        "Mis páginas sienten la aceleración de tus pasos. No titubees.",
        "El vacío bajo tus pies ansía un error... demuéstrale que puedes volar.",
        "Un salto impecable. Mis runas brillan con tu destreza cinética.",
        "El tiempo fluye más despacio cuando te deslizas sobre el neón.",
        "¿Escuchas el zumbido de los reactores? La ciudad te observa a través de mí.",
        "Cada segundo que ganas en este sector queda grabado en el Códice eterno.",
        "El muro no es un límite, es un trampolín hacia la cumbre.",
        "Alinea los nodos cuánticos, Corredor. La verdad del Códice aguarda.",
        "Tu reflejo en las ventanas holográficas ya no teme a la caída.",
        "He visto a mil caer aquí... tú sigues en pie. Prosigue."
    )

    fun generateSector(sectorId: Int): SectorDefinition {
        val clampedId = sectorId.coerceIn(1, TOTAL_SECTORS)
        val district = DistrictType.getDistrictForSector(clampedId)
        val rng = Random(clampedId.toLong() * 104729L)

        val blocks = mutableListOf<WorldBlock>()
        var blockId = 1

        // Starting rooftop platform
        val startZ = 0f
        blocks.add(
            WorldBlock(
                id = blockId++,
                type = BlockType.BUILDING_ROOF,
                position = Vector3D(0f, -1f, startZ),
                size = Vector3D(10f, 2f, 16f),
                color = Color(0xFF16152B),
                glowColor = district.primaryColor.copy(alpha = 0.6f)
            )
        )

        var currentZ = startZ + 12f
        var currentY = 0f
        var currentX = 0f

        // Procedurally generate 8 to 16 parkour segments along the Z axis
        val numSegments = 7 + (clampedId % 7) // 7 to 13 segments per sector

        val hasPuzzleTerminal = (clampedId <= 1557) || (clampedId % 2 == 0)
        val puzzleId = if (hasPuzzleTerminal) ((clampedId - 1) % 1557) + 1 else null
        val puzzleSegmentIndex = numSegments / 2

        for (i in 0 until numSegments) {
            val gap = 6f + rng.nextFloat() * 7f
            val elevationDelta = (rng.nextFloat() * 4f - 1.5f)
            currentY += elevationDelta
            currentZ += gap

            val segmentTypeRoll = rng.nextInt(6)

            when (segmentTypeRoll) {
                0 -> {
                    // Standard Neon Rooftop Jump with Speed Pad
                    val width = 8f + rng.nextFloat() * 4f
                    val length = 12f + rng.nextFloat() * 6f
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.BUILDING_ROOF,
                            position = Vector3D(currentX, currentY - 1f, currentZ + length / 2),
                            size = Vector3D(width, 2f, length),
                            color = Color(0xFF101226),
                            glowColor = district.primaryColor
                        )
                    )
                    // Speed booster in the middle
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.SPEED_PAD,
                            position = Vector3D(currentX, currentY + 0.1f, currentZ + length * 0.4f),
                            size = Vector3D(3f, 0.2f, 3f),
                            color = district.secondaryColor,
                            glowColor = district.secondaryColor
                        )
                    )
                    currentZ += length
                }
                1 -> {
                    // Wallrun Corridor: Wall on left or right with floating run surface
                    val isLeftWall = rng.nextBoolean()
                    val wallX = if (isLeftWall) currentX - 5.5f else currentX + 5.5f
                    val wallLength = 22f
                    // Wallrun surface
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.WALLRUN_SURFACE,
                            position = Vector3D(wallX, currentY + 3f, currentZ + wallLength / 2),
                            size = Vector3D(0.8f, 8f, wallLength),
                            color = district.primaryColor.copy(alpha = 0.85f),
                            glowColor = district.primaryColor,
                            isWallrunnable = true
                        )
                    )
                    // Landing platform at end of wall
                    val landX = currentX + (if (isLeftWall) -1f else 1f) * 2f
                    currentZ += wallLength + 3f
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.NEON_PLATFORM,
                            position = Vector3D(landX, currentY - 1f, currentZ + 4f),
                            size = Vector3D(8f, 1.5f, 8f),
                            color = Color(0xFF181032),
                            glowColor = district.secondaryColor
                        )
                    )
                    currentX = landX
                    currentZ += 8f
                }
                2 -> {
                    // Laser Grid Obstacle (Requires Crouch/Slide!)
                    val platLength = 16f
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.BUILDING_ROOF,
                            position = Vector3D(currentX, currentY - 1f, currentZ + platLength / 2),
                            size = Vector3D(9f, 2f, platLength),
                            color = Color(0xFF0D0A20),
                            glowColor = district.primaryColor
                        )
                    )
                    // Horizontal Laser Barrier at chest height (crouch to pass)
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.LASER_BARRIER,
                            position = Vector3D(currentX, currentY + 1.6f, currentZ + platLength * 0.5f),
                            size = Vector3D(8.5f, 0.4f, 0.4f),
                            color = Color(0xFFFF0055),
                            glowColor = Color(0xFFFF0055),
                            isCrouchHazard = true
                        )
                    )
                    currentZ += platLength
                }
                3 -> {
                    // Moving Platform across chasm
                    val moveAxis = if (rng.nextBoolean()) 1 else 2 // 1: X (horizontal), 2: Y (vertical)
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.MOVING_PLATFORM,
                            position = Vector3D(currentX, currentY, currentZ + 6f),
                            size = Vector3D(5f, 1f, 5f),
                            color = district.secondaryColor,
                            glowColor = district.secondaryColor,
                            moveSpeed = 2.5f + rng.nextFloat() * 1.5f,
                            moveAxis = moveAxis
                        )
                    )
                    currentZ += 12f
                    // Next landing
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.NEON_PLATFORM,
                            position = Vector3D(currentX, currentY, currentZ + 5f),
                            size = Vector3D(7f, 1.5f, 7f),
                            color = Color(0xFF141938),
                            glowColor = district.primaryColor
                        )
                    )
                    currentZ += 8f
                }
                4 -> {
                    // Grapple Anchor Point in mid-air over huge gap
                    val grappleX = currentX + (rng.nextFloat() * 4f - 2f)
                    val grappleY = currentY + 6f
                    val grappleZ = currentZ + 10f
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.GRAPPLE_ANCHOR,
                            position = Vector3D(grappleX, grappleY, grappleZ),
                            size = Vector3D(1.5f, 1.5f, 1.5f),
                            color = Color(0xFFFFE600),
                            glowColor = Color(0xFFFFE600)
                        )
                    )
                    // Far landing ledge
                    currentZ += 20f
                    blocks.add(
                        WorldBlock(
                            id = blockId++,
                            type = BlockType.BUILDING_ROOF,
                            position = Vector3D(currentX, currentY - 0.5f, currentZ + 6f),
                            size = Vector3D(8f, 2f, 10f),
                            color = Color(0xFF12142B),
                            glowColor = district.primaryColor
                        )
                    )
                    currentZ += 10f
                }
                else -> {
                    // Stepping Neon Energy Pillars
                    for (step in 0..2) {
                        val stepX = currentX + (if (step % 2 == 0) -2.5f else 2.5f)
                        val stepZ = currentZ + step * 6f
                        val stepY = currentY + step * 1f
                        blocks.add(
                            WorldBlock(
                                id = blockId++,
                                type = BlockType.NEON_PLATFORM,
                                position = Vector3D(stepX, stepY, stepZ),
                                size = Vector3D(3.5f, 1f, 3.5f),
                                color = district.primaryColor,
                                glowColor = district.primaryColor
                            )
                        )
                    }
                    currentY += 2f
                    currentZ += 18f
                }
            }

            // Insert Puzzle Terminal if on designated segment
            if (hasPuzzleTerminal && i == puzzleSegmentIndex) {
                blocks.add(
                    WorldBlock(
                        id = blockId++,
                        type = BlockType.PUZZLE_TERMINAL,
                        position = Vector3D(currentX, currentY + 1.2f, currentZ - 3f),
                        size = Vector3D(1.4f, 2.2f, 1.4f),
                        color = Color(0xFF00F0FF),
                        glowColor = Color(0xFF00F0FF),
                        hasPuzzle = true,
                        puzzleId = puzzleId
                    )
                )
            }
        }

        // Final Apex Rooftop & Quantum Portal Exit
        val finalLength = 14f
        val finalPos = Vector3D(currentX, currentY - 1f, currentZ + finalLength / 2)
        blocks.add(
            WorldBlock(
                id = blockId++,
                type = BlockType.BUILDING_ROOF,
                position = finalPos,
                size = Vector3D(10f, 2f, finalLength),
                color = Color(0xFF1C1338),
                glowColor = district.secondaryColor
            )
        )

        val exitPos = Vector3D(currentX, currentY + 2f, currentZ + finalLength * 0.8f)
        blocks.add(
            WorldBlock(
                id = blockId++,
                type = BlockType.PORTAL_EXIT,
                position = exitPos,
                size = Vector3D(3f, 4f, 1f),
                color = Color(0xFF00FFCC),
                glowColor = Color(0xFF00FFCC)
            )
        )

        // Background decorative skyscrapers to give AAA skyline depth
        for (bg in 0 until 14) {
            val bgRng = Random((clampedId * 31 + bg * 97).toLong())
            val side = if (bg % 2 == 0) 1f else -1f
            val bgX = currentX + side * (24f + bgRng.nextFloat() * 40f)
            val bgZ = startZ + bg * 22f + bgRng.nextFloat() * 15f
            val bgHeight = 40f + bgRng.nextFloat() * 60f
            val bgWidth = 14f + bgRng.nextFloat() * 18f
            blocks.add(
                WorldBlock(
                    id = blockId++,
                    type = BlockType.BUILDING_ROOF,
                    position = Vector3D(bgX, bgHeight / 2 - 20f, bgZ),
                    size = Vector3D(bgWidth, bgHeight, bgWidth),
                    color = Color(0xFF0A0D1F),
                    glowColor = district.primaryColor.copy(alpha = 0.25f)
                )
            )
        }

        val parTime = 18f + (numSegments * 2.8f)
        val quote = BOOK_QUOTES[(clampedId * 7) % BOOK_QUOTES.size]
        val sectorName = "Sector ${clampedId.toString().padStart(4, '0')} // ${district.districtName.substringBefore(' ')}"

        return SectorDefinition(
            sectorId = clampedId,
            name = sectorName,
            district = district,
            blocks = blocks,
            startPosition = Vector3D(0f, 2.5f, startZ + 2f),
            exitPosition = exitPos,
            parTimeSec = parTime,
            puzzleId = puzzleId,
            bookVoiceQuote = quote
        )
    }
}
