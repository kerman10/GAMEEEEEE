package com.example.engine.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.engine.physics.PlayerMovementState
import com.example.engine.physics.PlayerState
import com.example.engine.world.BlockType
import com.example.engine.world.DistrictType
import com.example.engine.world.Vector3D
import com.example.engine.world.WorldBlock
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ProjectedPolygon(
    val points: List<Offset>,
    val depth: Float,
    val color: Color,
    val strokeColor: Color? = null,
    val strokeWidth: Float = 1.5f
)

object Cyber3DRenderer {

    fun renderScene(
        drawScope: DrawScope,
        player: PlayerState,
        blocks: List<WorldBlock>,
        district: DistrictType,
        equippedSkinIndex: Int,
        timeSec: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        val fov = 480f * (if (player.currentSpeed > 18f) 1.15f else 1.0f)
        val cx = width / 2f
        val cy = height / 2f + (sin(player.headBobPhase) * 12f)

        // 1. Draw Cyberpunk Skybox Gradient & Distant Aerocars
        drawSkybox(drawScope, width, height, district, player, timeSec)

        // 2. Camera Transformations Setup
        val camYaw = Math.toRadians(player.yaw.toDouble()).toFloat()
        val camPitch = Math.toRadians(player.pitch.toDouble()).toFloat()
        val camRoll = Math.toRadians(player.roll.toDouble()).toFloat()

        val cosY = cos(camYaw)
        val sinY = sin(camYaw)
        val cosP = cos(camPitch)
        val sinP = sin(camPitch)
        val cosR = cos(camRoll)
        val sinR = sin(camRoll)

        val polygons = mutableListOf<ProjectedPolygon>()

        // 3. Project World 3D Blocks
        for (b in blocks) {
            val bx = b.position.x + if (b.moveAxis == 1) b.moveOffset else 0f
            val by = b.position.y + if (b.moveAxis == 2) b.moveOffset else 0f
            val bz = b.position.z + if (b.moveAxis == 3) b.moveOffset else 0f

            // Distance cull
            val dz = bz - player.position.z
            if (dz < -12f || dz > 160f) continue

            val hx = b.size.x / 2f
            val hy = b.size.y / 2f
            val hz = b.size.z / 2f

            // 8 bounding vertices of box
            val rawVerts = listOf(
                Vector3D(bx - hx, by - hy, bz - hz),
                Vector3D(bx + hx, by - hy, bz - hz),
                Vector3D(bx + hx, by + hy, bz - hz),
                Vector3D(bx - hx, by + hy, bz - hz),
                Vector3D(bx - hx, by - hy, bz + hz),
                Vector3D(bx + hx, by - hy, bz + hz),
                Vector3D(bx + hx, by + hy, bz + hz),
                Vector3D(bx - hx, by + hy, bz + hz)
            )

            // Project vertices to camera space
            val camVerts = rawVerts.map { v ->
                // Translate relative to player
                val rx = v.x - player.position.x
                val ry = v.y - player.position.y
                val rz = v.z - player.position.z

                // Yaw rotation (Y axis)
                val x1 = rx * cosY - rz * sinY
                val z1 = rx * sinY + rz * cosY
                val y1 = ry

                // Pitch rotation (X axis)
                val y2 = y1 * cosP - z1 * sinP
                val z2 = y1 * sinP + z1 * cosP
                val x2 = x1

                // Roll rotation (Z axis)
                val x3 = x2 * cosR - y2 * sinR
                val y3 = x2 * sinR + y2 * cosR
                val z3 = z2

                Vector3D(x3, y3, z3)
            }

            // Define 6 faces (Top, Front, Left, Right, Back, Bottom)
            val faceIndices = listOf(
                listOf(3, 2, 6, 7), // TOP
                listOf(0, 1, 2, 3), // FRONT
                listOf(4, 5, 6, 7), // BACK
                listOf(0, 3, 7, 4), // LEFT
                listOf(1, 2, 6, 5)  // RIGHT
            )

            for (face in faceIndices) {
                val v0 = camVerts[face[0]]
                val v1 = camVerts[face[1]]
                val v2 = camVerts[face[2]]
                val v3 = camVerts[face[3]]

                // Clip if all points behind camera
                if (v0.z <= 0.2f && v1.z <= 0.2f && v2.z <= 0.2f && v3.z <= 0.2f) continue

                val avgZ = (v0.z + v1.z + v2.z + v3.z) / 4f
                if (avgZ <= 0.3f) continue

                // 2D Screen projection
                val p0 = projectToScreen(v0, fov, cx, cy)
                val p1 = projectToScreen(v1, fov, cx, cy)
                val p2 = projectToScreen(v2, fov, cx, cy)
                val p3 = projectToScreen(v3, fov, cx, cy)

                // Depth-based fog blending
                val fogFactor = (avgZ / 120f).coerceIn(0f, 1f)
                val faceBaseColor = when (b.type) {
                    BlockType.WALLRUN_SURFACE -> district.primaryColor.copy(alpha = 0.85f)
                    BlockType.SPEED_PAD -> district.secondaryColor
                    BlockType.LASER_BARRIER -> Color(0xFFFF0055)
                    BlockType.PORTAL_EXIT -> Color(0xFF00FFCC)
                    BlockType.GRAPPLE_ANCHOR -> Color(0xFFFFE600)
                    BlockType.PUZZLE_TERMINAL -> Color(0xFF00F0FF)
                    else -> b.color
                }
                val finalColor = blendColors(faceBaseColor, district.fogColor, fogFactor)
                val strokeColor = if (b.glowColor != Color.Transparent && avgZ < 90f) {
                    blendColors(b.glowColor, district.fogColor, fogFactor)
                } else null

                polygons.add(ProjectedPolygon(listOf(p0, p1, p2, p3), avgZ, finalColor, strokeColor))
            }
        }

        // 4. Sort polygons back-to-front (Painter's Algorithm)
        polygons.sortByDescending { it.depth }

        // 5. Draw all 3D polygons
        for (poly in polygons) {
            val path = Path().apply {
                moveTo(poly.points[0].x, poly.points[0].y)
                for (i in 1 until poly.points.size) {
                    lineTo(poly.points[i].x, poly.points[i].y)
                }
                close()
            }
            drawScope.drawPath(path, poly.color, style = Fill)
            if (poly.strokeColor != null) {
                drawScope.drawPath(path, poly.strokeColor, style = Stroke(width = poly.strokeWidth))
            }
        }

        // 6. Draw Grappling Hook Cable if Active
        if (player.isGrappling && player.grappleTarget != null) {
            drawGrappleCable(drawScope, player, camYaw, camPitch, camRoll, fov, cx, cy)
        }

        // 7. Draw First-Person Cybernetic Gloves & HUD Elements
        drawFirstPersonCyberGloves(drawScope, player, equippedSkinIndex, width, height, timeSec)

        // 8. Draw "El Libro Observador" (Sentient Floating Grimoire with Living Animated Eye)
        drawObservingBookCompanion(drawScope, player, district, width, height, timeSec)
    }

    private fun projectToScreen(v: Vector3D, fov: Float, cx: Float, cy: Float): Offset {
        val safeZ = v.z.coerceAtLeast(0.1f)
        val px = cx + (v.x / safeZ) * fov
        val py = cy - (v.y / safeZ) * fov
        return Offset(px, py)
    }

    private fun drawSkybox(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        district: DistrictType,
        player: PlayerState,
        timeSec: Float
    ) {
        // Sky gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(district.skyTopColor, district.skyBottomColor, district.fogColor),
                startY = 0f,
                endY = height
            ),
            size = Size(width, height)
        )

        // Distant Flying Aerocars (Cyberpunk Skyline traffic)
        val carPhase = (timeSec * 40f + player.yaw * 2f) % (width + 200f) - 100f
        val carY = height * 0.35f + sin(timeSec * 2f) * 15f
        drawScope.drawCircle(
            color = district.primaryColor.copy(alpha = 0.8f),
            radius = 3.5f,
            center = Offset(carPhase, carY)
        )
        drawScope.drawLine(
            color = district.primaryColor.copy(alpha = 0.4f),
            start = Offset(carPhase - 25f, carY),
            end = Offset(carPhase, carY),
            strokeWidth = 2f
        )
    }

    private fun drawGrappleCable(
        drawScope: DrawScope,
        player: PlayerState,
        camYaw: Float,
        camPitch: Float,
        camRoll: Float,
        fov: Float,
        cx: Float,
        cy: Float
    ) {
        val target = player.grappleTarget ?: return
        val rx = target.x - player.position.x
        val ry = target.y - player.position.y
        val rz = target.z - player.position.z

        val x1 = rx * cos(camYaw) - rz * sin(camYaw)
        val z1 = rx * sin(camYaw) + rz * cos(camYaw)
        val y1 = ry

        val y2 = y1 * cos(camPitch) - z1 * sin(camPitch)
        val z2 = y1 * sin(camPitch) + z1 * cos(camPitch)
        val x2 = x1

        val x3 = x2 * cos(camRoll) - y2 * sin(camRoll)
        val y3 = x2 * sin(camRoll) + y2 * cos(camRoll)
        val z3 = z2

        if (z3 > 0.2f) {
            val targetScreen = projectToScreen(Vector3D(x3, y3, z3), fov, cx, cy)
            val handOrigin = Offset(drawScope.size.width * 0.72f, drawScope.size.height * 0.85f)
            drawScope.drawLine(
                color = Color(0xFF00FFFF),
                start = handOrigin,
                end = targetScreen,
                strokeWidth = 4f
            )
            drawScope.drawCircle(
                color = Color(0xFFFFE600),
                radius = 8f,
                center = targetScreen
            )
        }
    }

    private fun drawFirstPersonCyberGloves(
        drawScope: DrawScope,
        player: PlayerState,
        skinIndex: Int,
        width: Float,
        height: Float,
        timeSec: Float
    ) {
        val baseGloveColor = when (skinIndex) {
            1 -> Color(0xFFFF007F) // Synthwave Magenta
            2 -> Color(0xFFFFD700) // Golden Apex
            3 -> Color(0xFF00FF66) // Matrix Emerald
            4 -> Color(0xFF7B2CBF) // Void Obsidian
            else -> Color(0xFF00F0FF) // Default Cyan Runner
        }

        val bobOffset = sin(player.headBobPhase) * 14f
        val isSliding = player.isSliding
        val isWallrunning = player.movementState == PlayerMovementState.WALL_RUNNING_LEFT || player.movementState == PlayerMovementState.WALL_RUNNING_RIGHT

        // Left Cyber-Hand
        val leftHandX = width * 0.22f + (if (player.movementState == PlayerMovementState.WALL_RUNNING_LEFT) -30f else 0f)
        val leftHandY = height * 0.84f + bobOffset + (if (isSliding) 50f else 0f)

        // Left Forearm / Glove Body
        val leftPath = Path().apply {
            moveTo(leftHandX - 70f, height)
            lineTo(leftHandX - 20f, leftHandY)
            lineTo(leftHandX + 50f, leftHandY + 20f)
            lineTo(leftHandX + 20f, height)
            close()
        }
        drawScope.drawPath(leftPath, Color(0xFF0C0E1A))
        drawScope.drawPath(leftPath, baseGloveColor, style = Stroke(width = 2.5f))

        // Left Knuckle Circuit LEDs
        for (k in 0..3) {
            drawScope.drawCircle(
                color = baseGloveColor,
                radius = 4f,
                center = Offset(leftHandX - 10f + k * 16f, leftHandY + 6f)
            )
        }

        // Right Cyber-Hand (With Grapple Conduit)
        val rightHandX = width * 0.78f + (if (player.movementState == PlayerMovementState.WALL_RUNNING_RIGHT) 30f else 0f)
        val rightHandY = height * 0.84f - bobOffset + (if (isSliding) 50f else 0f)

        val rightPath = Path().apply {
            moveTo(rightHandX - 20f, height)
            lineTo(rightHandX - 50f, rightHandY + 20f)
            lineTo(rightHandX + 20f, rightHandY)
            lineTo(rightHandX + 70f, height)
            close()
        }
        drawScope.drawPath(rightPath, Color(0xFF0C0E1A))
        drawScope.drawPath(rightPath, baseGloveColor, style = Stroke(width = 2.5f))

        // Right Wrist Energy Core
        drawScope.drawCircle(
            color = if (player.canDoubleJump) baseGloveColor else Color.Gray,
            radius = 12f,
            center = Offset(rightHandX, rightHandY + 40f)
        )
    }

    private fun drawObservingBookCompanion(
        drawScope: DrawScope,
        player: PlayerState,
        district: DistrictType,
        width: Float,
        height: Float,
        timeSec: Float
    ) {
        // Floating sentient cyber grimoire in upper right corner with animated observing eye
        val bookX = width - 110f
        val bookY = 160f + sin(timeSec * 2.5f) * 8f

        // Holographic floating runes around the book
        val runePulse = (sin(timeSec * 4f) * 0.3f + 0.7f)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(district.primaryColor.copy(alpha = 0.4f * runePulse), Color.Transparent),
                center = Offset(bookX, bookY),
                radius = 70f
            ),
            radius = 70f,
            center = Offset(bookX, bookY)
        )

        // Book Outer Cyber Cover
        val bookWidth = 64f
        val bookHeight = 76f
        drawScope.drawRoundRect(
            color = Color(0xFF0F0A24),
            topLeft = Offset(bookX - bookWidth / 2f, bookY - bookHeight / 2f),
            size = Size(bookWidth, bookHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        drawScope.drawRoundRect(
            color = district.primaryColor,
            topLeft = Offset(bookX - bookWidth / 2f, bookY - bookHeight / 2f),
            size = Size(bookWidth, bookHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
            style = Stroke(width = 2.5f)
        )

        // The Sentient Eye Sclera (Mechanical Diamond Eye)
        val eyeEyePath = Path().apply {
            moveTo(bookX - 22f, bookY)
            quadraticTo(bookX, bookY - 18f, bookX + 22f, bookY)
            quadraticTo(bookX, bookY + 18f, bookX - 22f, bookY)
            close()
        }
        drawScope.drawPath(eyeEyePath, Color(0xFF050114))
        drawScope.drawPath(eyeEyePath, district.secondaryColor, style = Stroke(width = 2f))

        // Pupil tracking player velocity / action
        val irisColor = when {
            player.isTouchingLaser -> Color(0xFFFF0055)
            player.comboMultiplier >= 3 -> Color(0xFFFF007F)
            player.isGrappling -> Color(0xFFFFE600)
            else -> district.primaryColor
        }

        val pupilOffsetX = (player.velocity.x * 0.2f).coerceIn(-6f, 6f)
        val pupilOffsetY = (-player.pitch * 0.1f).coerceIn(-4f, 4f)
        val pupilCenter = Offset(bookX + pupilOffsetX, bookY + pupilOffsetY)

        // Iris
        drawScope.drawCircle(
            color = irisColor,
            radius = 8f,
            center = pupilCenter
        )
        // Diamond Core
        drawScope.drawCircle(
            color = Color.Black,
            radius = 3.5f,
            center = pupilCenter
        )
    }

    private fun blendColors(c1: Color, c2: Color, ratio: Float): Color {
        val r = c1.red + (c2.red - c1.red) * ratio
        val g = c1.green + (c2.green - c1.green) * ratio
        val b = c1.blue + (c2.blue - c1.blue) * ratio
        val a = c1.alpha + (c2.alpha - c1.alpha) * ratio
        return Color(r, g, b, a)
    }
}
