package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.physics.PlayerMovementState
import com.example.engine.renderer.Cyber3DRenderer
import com.example.engine.world.SectorEngine
import com.example.ui.CurrentScreen
import com.example.ui.GameViewModel
import com.example.ui.components.CyberPuzzleDialog
import kotlin.math.sqrt

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    val playerProfile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val soundEnabled = playerProfile?.soundEnabled ?: true
    val skinIndex = playerProfile?.equippedSkinIndex ?: 0

    var joyThumbX by remember { mutableFloatStateOf(0f) }
    var joyThumbY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. First-Person 3D World Rendering Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cyber_3d_viewport")
        ) {
            val timeSec = activeGame.timeElapsedSec
            Cyber3DRenderer.renderScene(
                drawScope = this,
                player = viewModel.physics.player,
                blocks = activeGame.currentSector.blocks,
                district = activeGame.currentSector.district,
                equippedSkinIndex = skinIndex,
                timeSec = timeSec
            )
        }

        // 2. Left Touch Zone: Virtual Movement Joystick
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.BottomStart)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            joyThumbX = 0f
                            joyThumbY = 0f
                        },
                        onDragEnd = {
                            joyThumbX = 0f
                            joyThumbY = 0f
                            viewModel.setJoystickInput(0f, 0f)
                        },
                        onDragCancel = {
                            joyThumbX = 0f
                            joyThumbY = 0f
                            viewModel.setJoystickInput(0f, 0f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val maxRadius = 90f
                            joyThumbX = (joyThumbX + dragAmount.x).coerceIn(-maxRadius, maxRadius)
                            joyThumbY = (joyThumbY + dragAmount.y).coerceIn(-maxRadius, maxRadius)
                            // Invert Y for forward/back
                            viewModel.setJoystickInput(joyThumbX / maxRadius, -joyThumbY / maxRadius)
                        }
                    )
                }
                .testTag("joystick_touch_area")
        ) {
            // Visual joystick base
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(2.dp, activeGame.currentSector.district.primaryColor.copy(alpha = 0.6f), CircleShape)
            ) {
                // Joystick Thumb Knob
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                        .offset(
                            x = (joyThumbX * 0.3f).dp,
                            y = (joyThumbY * 0.3f).dp
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    activeGame.currentSector.district.primaryColor,
                                    activeGame.currentSector.district.secondaryColor
                                )
                            )
                        )
                )
            }
        }

        // 3. Right Touch Zone: Camera Look & Aim Drag Area
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.BottomEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val sensitivity = 0.22f
                        viewModel.addLookDelta(dragAmount.x * sensitivity, dragAmount.y * sensitivity)
                    }
                }
                .testTag("camera_look_area")
        )

        // 4. Action Buttons (Jump, Slide, Grapple, Hack Terminal)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Hack Terminal button
            Button(
                onClick = { viewModel.onInteractPressed() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(width = 110.dp, height = 44.dp)
                    .testTag("hack_terminal_button")
            ) {
                Icon(Icons.Default.Terminal, contentDescription = "Hackear", tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("HACK", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Grapple button
                Button(
                    onClick = { viewModel.onGrapplePressed() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE600)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("grapple_button")
                ) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = "Gancho", tint = Color.Black)
                }

                // Slide / Crouch button
                Button(
                    onClick = { viewModel.onSlidePressed() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("slide_button")
                ) {
                    Text("SLIDE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                // Jump / Double Jump button
                Button(
                    onClick = { viewModel.onJumpPressed() },
                    colors = ButtonDefaults.buttonColors(containerColor = activeGame.currentSector.district.primaryColor),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("jump_button")
                ) {
                    Text("JUMP", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        // 5. Top Cyberpunk HUD (Sector badge, Speedometer, Combo, Timer, Sound, Menu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sector info & District badge
            Column {
                Text(
                    text = activeGame.currentSector.name,
                    color = activeGame.currentSector.district.primaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = activeGame.currentSector.district.districtName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            // Center: Speed & Combo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(viewModel.physics.player.currentSpeed * 3.6f).toInt()} KM/H",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                if (viewModel.physics.player.comboMultiplier > 1) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF007F), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "COMBO ${viewModel.physics.player.comboMultiplier}x",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Right: Time & Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f s", activeGame.timeElapsedSec),
                    color = Color(0xFF00FFCC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { viewModel.toggleMute(soundEnabled) },
                    modifier = Modifier.testTag("toggle_sound_button")
                ) {
                    Icon(
                        if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Sonido",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { viewModel.navigateTo(CurrentScreen.MAIN_MENU) },
                    modifier = Modifier.testTag("back_to_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                }
            }
        }

        // 6. Subtitle Commentary from "El Libro Observador"
        AnimatedVisibility(
            visible = activeGame.bookSpeechVisible && activeGame.bookSpeechText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080417).copy(alpha = 0.88f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .border(1.dp, activeGame.currentSector.district.secondaryColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .testTag("codex_subtitles_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👁 EL LIBRO OBSERVADOR: ",
                        color = activeGame.currentSector.district.secondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "\"${activeGame.bookSpeechText}\"",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 7. Active Cyber Puzzle Dialog Overlay (if open)
        if (activeGame.isPuzzleOpen && activeGame.activePuzzle != null) {
            CyberPuzzleDialog(
                puzzle = activeGame.activePuzzle!!,
                onRotateNode = { viewModel.rotatePuzzleNode(it) },
                onAdjustFrequency = { idx, v -> viewModel.adjustPuzzleFrequency(idx, v) },
                onInputSequence = { viewModel.inputPuzzleSequence(it) },
                onClose = { viewModel.closePuzzle() }
            )
        }

        // 8. Sector Cleared Victory Dialog
        if (activeGame.isCleared) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .border(2.dp, Color(0xFF00FFCC), RoundedCornerShape(18.dp))
                        .testTag("sector_cleared_dialog"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF08051E)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡SECTOR SUPERADO!",
                            color = Color(0xFF00FFCC),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeGame.currentSector.name,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Star ratings
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrella $i",
                                    tint = if (i <= activeGame.earnedStars) Color(0xFFFFD700) else Color.DarkGray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "TIEMPO: ${String.format("%.2f", activeGame.timeElapsedSec)} s (Par: ${activeGame.currentSector.parTimeSec.toInt()} s)",
                            color = Color(0xFF00F0FF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )

                        if (activeGame.unlockedChapterTitle != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFF007F).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFF007F), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📖 ¡NUEVO CAPÍTULO DESBLOQUEADO!\n${activeGame.unlockedChapterTitle}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.loadSector(activeGame.currentSector.sectorId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22174A)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("retry_sector_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Repetir", tint = Color.White)
                            }

                            Button(
                                onClick = { viewModel.navigateTo(CurrentScreen.SECTOR_SELECT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22174A)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("select_sector_nav_button")
                            ) {
                                Text("SECTORES", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val nextId = (activeGame.currentSector.sectorId + 1).coerceAtMost(SectorEngine.TOTAL_SECTORS)
                                    viewModel.loadSector(nextId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("next_sector_button")
                            ) {
                                Text("SIGUIENTE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
