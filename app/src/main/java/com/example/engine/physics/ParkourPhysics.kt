package com.example.engine.physics

import com.example.audio.CyberAudioEngine
import com.example.engine.world.BlockType
import com.example.engine.world.Vector3D
import com.example.engine.world.WorldBlock
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

enum class PlayerMovementState {
    IDLE,
    RUNNING,
    SPRINTING,
    SLIDING,
    IN_AIR,
    WALL_RUNNING_LEFT,
    WALL_RUNNING_RIGHT,
    GRAPPLING,
    VAULTING
}

data class PlayerState(
    var position: Vector3D = Vector3D(0f, 2.5f, 0f),
    var velocity: Vector3D = Vector3D(0f, 0f, 0f),
    var yaw: Float = 0f,    // Look horizontal (degrees, 0 = forward +Z)
    var pitch: Float = 0f,  // Look vertical (degrees, -60 to +60)
    var roll: Float = 0f,   // Camera tilt (degrees, e.g. during wallrun)
    var movementState: PlayerMovementState = PlayerMovementState.IDLE,
    var isGrounded: Boolean = false,
    var canDoubleJump: Boolean = true,
    var thrusterEnergy: Float = 1.0f,
    var isSliding: Boolean = false,
    var slideTimer: Float = 0f,
    var wallRunTimer: Float = 0f,
    var isGrappling: Boolean = false,
    var grappleTarget: Vector3D? = null,
    var grappleLength: Float = 0f,
    var comboMultiplier: Int = 1,
    var comboTimer: Float = 0f,
    var currentSpeed: Float = 0f,
    var headBobPhase: Float = 0f,
    var eyeTargetFocus: Vector3D = Vector3D(0f, 0f, 10f),
    var isTouchingLaser: Boolean = false
)

class ParkourPhysicsEngine {
    val player = PlayerState()
    private val gravity = -26.0f
    private val runSpeed = 14.0f
    private val sprintSpeed = 22.0f
    private val jumpPower = 11.5f
    private val wallrunSpeed = 19.0f

    fun resetPlayer(startPos: Vector3D) {
        player.position = Vector3D(startPos.x, startPos.y, startPos.z)
        player.velocity = Vector3D(0f, 0f, 0f)
        player.yaw = 0f
        player.pitch = 0f
        player.roll = 0f
        player.movementState = PlayerMovementState.IDLE
        player.isGrounded = false
        player.canDoubleJump = true
        player.thrusterEnergy = 1.0f
        player.isSliding = false
        player.slideTimer = 0f
        player.wallRunTimer = 0f
        player.isGrappling = false
        player.grappleTarget = null
        player.comboMultiplier = 1
        player.comboTimer = 0f
        player.headBobPhase = 0f
        player.isTouchingLaser = false
    }

    fun update(
        dt: Float,
        moveX: Float, // -1 (left) to 1 (right)
        moveZ: Float, // -1 (backward) to 1 (forward)
        blocks: List<WorldBlock>,
        onComboUp: (Int) -> Unit = {}
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        // Update Moving Platforms
        for (b in blocks) {
            if (b.type == BlockType.MOVING_PLATFORM && b.moveAxis != 0) {
                b.moveOffset = sin(System.currentTimeMillis() * 0.002f * b.moveSpeed) * 4f
            }
        }

        // Combo decay
        if (player.comboTimer > 0f) {
            player.comboTimer -= clampedDt
            if (player.comboTimer <= 0f && player.comboMultiplier > 1) {
                player.comboMultiplier = 1
            }
        }

        // Grappling Hook Physics
        if (player.isGrappling && player.grappleTarget != null) {
            val target = player.grappleTarget!!
            val dx = target.x - player.position.x
            val dy = target.y - player.position.y
            val dz = target.z - player.position.z
            val dist = sqrt(dx * dx + dy * dy + dz * dz)

            if (dist > 1.5f) {
                val pullSpeed = 24.0f
                player.velocity.x = (dx / dist) * pullSpeed
                player.velocity.y = (dy / dist) * pullSpeed + 2f
                player.velocity.z = (dz / dist) * pullSpeed
                player.movementState = PlayerMovementState.GRAPPLING
                player.isGrounded = false
            } else {
                // Released grapple at top
                player.isGrappling = false
                player.grappleTarget = null
                player.velocity.y = 8f
                CyberAudioEngine.playJumpSound(true)
            }
            player.position.x += player.velocity.x * clampedDt
            player.position.y += player.velocity.y * clampedDt
            player.position.z += player.velocity.z * clampedDt
            return
        }

        // Handle Slide Mechanics
        if (player.isSliding) {
            player.slideTimer -= clampedDt
            player.movementState = PlayerMovementState.SLIDING
            // Move forward with slide impulse
            val radYaw = Math.toRadians(player.yaw.toDouble()).toFloat()
            val fwdX = sin(radYaw)
            val fwdZ = cos(radYaw)
            player.velocity.x = fwdX * sprintSpeed * 1.2f
            player.velocity.z = fwdZ * sprintSpeed * 1.2f

            if (player.slideTimer <= 0f) {
                player.isSliding = false
            }
        } else {
            // Standard Ground / Air Movement
            val isSprinting = (moveZ > 0.8f)
            val targetSpeed = if (isSprinting) sprintSpeed else runSpeed

            val radYaw = Math.toRadians(player.yaw.toDouble()).toFloat()
            val forwardX = sin(radYaw)
            val forwardZ = cos(radYaw)
            val rightX = cos(radYaw)
            val rightZ = -sin(radYaw)

            val wishDirX = forwardX * moveZ + rightX * moveX
            val wishDirZ = forwardZ * moveZ + rightZ * moveX
            val wishLen = sqrt(wishDirX * wishDirX + wishDirZ * wishDirZ)

            if (wishLen > 0.05f) {
                val normalizedWishX = wishDirX / wishLen
                val normalizedWishZ = wishDirZ / wishLen
                val accel = if (player.isGrounded) 14.0f else 6.0f
                player.velocity.x += (normalizedWishX * targetSpeed - player.velocity.x) * (accel * clampedDt)
                player.velocity.z += (normalizedWishZ * targetSpeed - player.velocity.z) * (accel * clampedDt)
                if (player.isGrounded) {
                    player.movementState = if (isSprinting) PlayerMovementState.SPRINTING else PlayerMovementState.RUNNING
                    player.headBobPhase += clampedDt * (if (isSprinting) 18f else 12f)
                }
            } else {
                if (player.isGrounded) {
                    player.movementState = PlayerMovementState.IDLE
                    player.velocity.x *= (1.0f - 10.0f * clampedDt).coerceIn(0f, 1f)
                    player.velocity.z *= (1.0f - 10.0f * clampedDt).coerceIn(0f, 1f)
                }
            }
        }

        // Check for Wall Running
        var foundWallrun = false
        if (!player.isGrounded && player.position.y > 0f) {
            for (b in blocks) {
                if (b.isWallrunnable) {
                    val distToWallX = abs(player.position.x - b.position.x)
                    val inZRange = player.position.z >= (b.position.z - b.size.z / 2) && player.position.z <= (b.position.z + b.size.z / 2)
                    val inYRange = abs(player.position.y - b.position.y) < b.size.y / 2 + 1f

                    if (distToWallX < 2.0f && inZRange && inYRange) {
                        foundWallrun = true
                        val isLeft = (player.position.x > b.position.x)
                        player.movementState = if (isLeft) PlayerMovementState.WALL_RUNNING_LEFT else PlayerMovementState.WALL_RUNNING_RIGHT
                        player.roll += ((if (isLeft) -15f else 15f) - player.roll) * (8f * clampedDt)
                        player.velocity.y = (player.velocity.y * 0.5f).coerceAtLeast(-1.5f) // Slow fall on wall
                        player.velocity.z = wallrunSpeed
                        player.canDoubleJump = true
                        player.wallRunTimer += clampedDt
                        if (player.wallRunTimer > 0.1f && (player.wallRunTimer % 0.3f) < clampedDt) {
                            CyberAudioEngine.playWallRunSound()
                        }
                        break
                    }
                }
            }
        }

        if (!foundWallrun) {
            player.roll += (0f - player.roll) * (10f * clampedDt)
            player.wallRunTimer = 0f
            // Apply Gravity
            player.velocity.y += gravity * clampedDt
        }

        // Apply Speed Boost Pads & Check Hazards
        player.isTouchingLaser = false
        for (b in blocks) {
            val inBoxX = abs(player.position.x - (b.position.x + if (b.moveAxis == 1) b.moveOffset else 0f)) <= b.size.x / 2 + 0.5f
            val inBoxZ = abs(player.position.z - (b.position.z + if (b.moveAxis == 3) b.moveOffset else 0f)) <= b.size.z / 2 + 0.5f
            val inBoxY = abs(player.position.y - (b.position.y + if (b.moveAxis == 2) b.moveOffset else 0f)) <= b.size.y / 2 + 1.2f

            if (inBoxX && inBoxZ && inBoxY) {
                if (b.type == BlockType.SPEED_PAD) {
                    player.velocity.z = max(player.velocity.z + 10f, 30f)
                    CyberAudioEngine.playJumpSound(true)
                } else if (b.isCrouchHazard && !player.isSliding) {
                    player.isTouchingLaser = true
                }
            }
        }

        // Integrate Position
        player.position.x += player.velocity.x * clampedDt
        player.position.y += player.velocity.y * clampedDt
        player.position.z += player.velocity.z * clampedDt

        // Ground & Platform Collision Detection
        player.isGrounded = false
        val playerRadius = 0.5f
        val playerHeight = if (player.isSliding) 1.0f else 2.2f

        for (b in blocks) {
            val blockPosX = b.position.x + if (b.moveAxis == 1) b.moveOffset else 0f
            val blockPosY = b.position.y + if (b.moveAxis == 2) b.moveOffset else 0f
            val blockPosZ = b.position.z + if (b.moveAxis == 3) b.moveOffset else 0f

            val minX = blockPosX - b.size.x / 2 - playerRadius
            val maxX = blockPosX + b.size.x / 2 + playerRadius
            val minZ = blockPosZ - b.size.z / 2 - playerRadius
            val maxZ = blockPosZ + b.size.z / 2 + playerRadius
            val topY = blockPosY + b.size.y / 2

            if (player.position.x in minX..maxX && player.position.z in minZ..maxZ) {
                if (player.position.y >= topY - 0.5f && player.position.y <= topY + 0.6f && player.velocity.y <= 0f) {
                    player.position.y = topY
                    player.velocity.y = 0f
                    player.isGrounded = true
                    player.canDoubleJump = true
                    player.thrusterEnergy = (player.thrusterEnergy + clampedDt * 2f).coerceAtMost(1f)
                    if (!player.isSliding && player.movementState == PlayerMovementState.IN_AIR) {
                        player.movementState = PlayerMovementState.IDLE
                    }
                    break
                }
            }
        }

        if (!player.isGrounded && !foundWallrun && !player.isGrappling) {
            player.movementState = PlayerMovementState.IN_AIR
        }

        // Fall into void reset
        if (player.position.y < -15f) {
            resetPlayer(Vector3D(0f, 2.5f, (player.position.z - 25f).coerceAtLeast(0f)))
        }

        player.currentSpeed = sqrt(player.velocity.x * player.velocity.x + player.velocity.z * player.velocity.z)
    }

    fun jump(): Boolean {
        if (player.movementState == PlayerMovementState.WALL_RUNNING_LEFT || player.movementState == PlayerMovementState.WALL_RUNNING_RIGHT) {
            // Wall Kick Impulse
            val kickDir = if (player.movementState == PlayerMovementState.WALL_RUNNING_LEFT) 1f else -1f
            player.velocity.x = kickDir * 12f
            player.velocity.y = jumpPower * 1.1f
            player.velocity.z += 6f
            player.movementState = PlayerMovementState.IN_AIR
            addCombo()
            CyberAudioEngine.playJumpSound(false)
            return true
        } else if (player.isGrounded) {
            player.velocity.y = jumpPower
            player.isGrounded = false
            player.movementState = PlayerMovementState.IN_AIR
            addCombo()
            CyberAudioEngine.playJumpSound(false)
            return true
        } else if (player.canDoubleJump && player.thrusterEnergy >= 0.3f) {
            // Cyber Double Jump Thruster
            player.velocity.y = jumpPower * 1.05f
            player.canDoubleJump = false
            player.thrusterEnergy -= 0.35f
            addCombo()
            CyberAudioEngine.playJumpSound(true)
            return true
        }
        return false
    }

    fun slide(): Boolean {
        if (player.isGrounded && !player.isSliding) {
            player.isSliding = true
            player.slideTimer = 0.85f
            addCombo()
            CyberAudioEngine.playSlideSound()
            return true
        }
        return false
    }

    fun tryGrapple(blocks: List<WorldBlock>): Boolean {
        val anchor = blocks.firstOrNull {
            it.type == BlockType.GRAPPLE_ANCHOR &&
                    it.position.z > player.position.z &&
                    (it.position.z - player.position.z) < 28f &&
                    abs(it.position.x - player.position.x) < 14f
        }
        if (anchor != null) {
            player.isGrappling = true
            player.grappleTarget = anchor.position
            addCombo()
            CyberAudioEngine.playGrappleSound()
            return true
        }
        return false
    }

    private fun addCombo() {
        player.comboMultiplier = (player.comboMultiplier + 1).coerceAtMost(5)
        player.comboTimer = 3.5f
    }
}
