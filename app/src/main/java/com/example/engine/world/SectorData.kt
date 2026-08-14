package com.example.engine.world

import androidx.compose.ui.graphics.Color

enum class DistrictType(
    val id: Int,
    val districtName: String,
    val sectorRange: IntRange,
    val primaryColor: Color,
    val secondaryColor: Color,
    val fogColor: Color,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val themeDescription: String
) {
    NEON_ZERO(
        1, "Distrito Neón Cero", 1..293,
        Color(0xFF00F0FF), Color(0xFFFF007F), Color(0xFF0D0624),
        Color(0xFF050014), Color(0xFF1E0038),
        "Azoteas cyberpunk bañadas en neón, saltos entre rascacielos y vallas holográficas."
    ),
    CYBER_ZENITH(
        2, "Torres Ciber-Zenit", 294..586,
        Color(0xFF00E5FF), Color(0xFFFF9100), Color(0xFF061426),
        Color(0xFF010A1A), Color(0xFF0B2545),
        "Muros verticales de cristal reflectante, ideales para carreras acrobáticas por paredes."
    ),
    QUANTUM_CORE(
        3, "Núcleo Cuántico", 587..879,
        Color(0xFF00FF88), Color(0xFF9D00FF), Color(0xFF071C12),
        Color(0xFF020E08), Color(0xFF113824),
        "Reactores de plasma pulsante, campos de fuerza y plataformas de resonancia temporal."
    ),
    MATRIX_ABYSS(
        4, "Abismo Matrix", 880..1172,
        Color(0xFF8A2BE2), Color(0xFF00FFCC), Color(0xFF120324),
        Color(0xFF0A0017), Color(0xFF280747),
        "Pistas de datos suspendidas sobre un abismo infinito donde el gancho es tu único aliado."
    ),
    UNDERGRID_SEWERS(
        5, "Sub-red Subterránea", 1173..1465,
        Color(0xFFFF2A2A), Color(0xFFFFB703), Color(0xFF1F0B0B),
        Color(0xFF120404), Color(0xFF330E0E),
        "Túneles de alta velocidad con láseres de corte bajo donde debes deslizarte a ras de suelo."
    ),
    CHRONO_FOUNDRY(
        6, "Fundición Crono-Téctica", 1466..1758,
        Color(0xFFFFB800), Color(0xFFFF4D00), Color(0xFF241503),
        Color(0xFF140B01), Color(0xFF3B1E05),
        "Pistones térmicos gigantes y plataformas móviles en perfecta sincronía mecánica."
    ),
    ORBITAL_TETHER(
        7, "Atalaya Orbital", 1759..2051,
        Color(0xFF80D8FF), Color(0xFFE040FB), Color(0xFF081226),
        Color(0xFF020717), Color(0xFF1A2744),
        "Estructuras en la estratosfera con gravedad reducida y saltos de distancia extrema."
    ),
    EYE_OF_THE_VOID(
        8, "El Vacío del Ojo Supremo", 2052..2344,
        Color(0xFFFF0055), Color(0xFF00F5D4), Color(0xFF000000),
        Color(0xFF000000), Color(0xFF1B0314),
        "El santuario donde el Libro Observador juzga cada movimiento en la cima de la realidad."
    );

    companion object {
        fun getDistrictForSector(sectorId: Int): DistrictType {
            val clamped = sectorId.coerceIn(1, 2344)
            return values().firstOrNull { clamped in it.sectorRange } ?: NEON_ZERO
        }
    }
}

data class Vector3D(var x: Float, var y: Float, var z: Float) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Float) = Vector3D(x * factor, y * factor, z * factor)
}

enum class BlockType {
    BUILDING_ROOF,
    NEON_PLATFORM,
    WALLRUN_SURFACE,
    MOVING_PLATFORM,
    LASER_BARRIER,
    GRAPPLE_ANCHOR,
    PUZZLE_TERMINAL,
    PORTAL_EXIT,
    SPEED_PAD,
    ENERGY_COLLECTIBLE
}

data class WorldBlock(
    val id: Int,
    val type: BlockType,
    val position: Vector3D,
    val size: Vector3D,
    val color: Color,
    val glowColor: Color = Color.Transparent,
    val isWallrunnable: Boolean = false,
    val hasPuzzle: Boolean = false,
    val puzzleId: Int? = null,
    val isCrouchHazard: Boolean = false,
    var moveOffset: Float = 0f,
    val moveSpeed: Float = 0f,
    val moveAxis: Int = 0 // 0: none, 1: X, 2: Y, 3: Z
)

data class SectorDefinition(
    val sectorId: Int,
    val name: String,
    val district: DistrictType,
    val blocks: List<WorldBlock>,
    val startPosition: Vector3D,
    val exitPosition: Vector3D,
    val parTimeSec: Float,
    val puzzleId: Int?,
    val bookVoiceQuote: String
)
