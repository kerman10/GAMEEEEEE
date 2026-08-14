package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CurrentScreen
import com.example.ui.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    val completedCount by viewModel.completedSectorsCount.collectAsStateWithLifecycle()
    val solvedCount by viewModel.solvedPuzzlesCount.collectAsStateWithLifecycle()
    val profile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val soundEnabled = profile?.soundEnabled ?: true

    val infiniteTransition = rememberInfiniteTransition(label = "menuAnim")
    val eyeGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyeGlow"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060312))
            .testTag("main_menu_screen")
    ) {
        // Cyber Neon City Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Radial cyber horizon
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF04000C), Color(0xFF13082B), Color(0xFF060312)),
                    startY = 0f,
                    endY = h
                ),
                size = Size(w, h)
            )

            // Neon perspective grid lines on bottom
            val horizonY = h * 0.65f
            for (i in -8..8) {
                val startX = w / 2f + i * 20f
                val endX = w / 2f + i * 140f
                drawLine(
                    color = Color(0xFF00F0FF).copy(alpha = 0.18f),
                    start = Offset(startX, horizonY),
                    end = Offset(endX, h),
                    strokeWidth = 1.2f
                )
            }
            for (j in 1..6) {
                val gridY = horizonY + (h - horizonY) * (j * j / 36f)
                drawLine(
                    color = Color(0xFFFF007F).copy(alpha = 0.22f),
                    start = Offset(0f, gridY),
                    end = Offset(w, gridY),
                    strokeWidth = 1.2f
                )
            }
        }

        // Sound Toggle at Top Right
        IconButton(
            onClick = { viewModel.toggleMute(soundEnabled) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .testTag("menu_sound_toggle")
        ) {
            Icon(
                if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                contentDescription = "Sonido",
                tint = Color(0xFF00F0FF)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Animated Omniscient Eye Book
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(10.dp))

                // The Observing Book Emblem
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .offset(y = floatY.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Outer diamond grimoire
                        val frame = Path().apply {
                            moveTo(cx, 10f)
                            lineTo(size.width - 10f, cy)
                            lineTo(cx, size.height - 10f)
                            lineTo(10f, cy)
                            close()
                        }
                        drawPath(frame, Color(0xFF140D36))
                        drawPath(
                            frame,
                            Color(0xFFFF007F).copy(alpha = eyeGlow),
                            style = Stroke(width = 3f)
                        )

                        // Eye Sclera
                        val eye = Path().apply {
                            moveTo(22f, cy)
                            quadraticTo(cx, cy - 26f, size.width - 22f, cy)
                            quadraticTo(cx, cy + 26f, 22f, cy)
                            close()
                        }
                        drawPath(eye, Color(0xFF050114))
                        drawPath(eye, Color(0xFF00F0FF), style = Stroke(width = 2f))

                        // Eye Iris
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFFFF007F)),
                                center = Offset(cx, cy),
                                radius = 16f
                            ),
                            radius = 14f,
                            center = Offset(cx, cy)
                        )
                        // Core pupil
                        drawCircle(Color.Black, radius = 6f, center = Offset(cx, cy))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CYBERPARKOUR 3D",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "EL LIBRO OBSERVADOR // METRÓPOLIS OCCULTA",
                    color = Color(0xFF00F0FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
            }

            // Stats Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26184C), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0722)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SECTORES", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("$completedCount / 2344", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ENIGMAS", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("$solvedCount / 1557", color = Color(0xFFFF007F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISTRICTOS", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("8 ACTIVOS", color = Color(0xFFFFE600), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / Continue Button
                Button(
                    onClick = {
                        val nextSector = (completedCount + 1).coerceIn(1, 2344)
                        viewModel.loadSector(nextSector)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("play_campaign_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (completedCount == 0) "INICIAR PARKOUR 3D" else "CONTINUAR (SECTOR ${completedCount + 1})",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // 2344 Sectors Browser Button
                Button(
                    onClick = { viewModel.navigateTo(CurrentScreen.SECTOR_SELECT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF140D36)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("open_sector_browser_button")
                ) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFF00F0FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MAPA DE 2344 SECTORES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Codex Lore Vault Button
                Button(
                    onClick = { viewModel.navigateTo(CurrentScreen.CODEX_VAULT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF140D36)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("open_codex_vault_button")
                ) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = Color(0xFFFF007F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BÓVEDA DEL CÓDICE (EL LIBRO)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Cyber-Glove Customizer Button
                Button(
                    onClick = { viewModel.navigateTo(CurrentScreen.GLOVE_CUSTOMIZER) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF140D36)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFFFFE600).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("open_customizer_button")
                ) {
                    Icon(Icons.Default.Workspaces, contentDescription = null, tint = Color(0xFFFFE600))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TALLER DE GUANTES CIBERNÉTICOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Footer
            Text(
                text = "Motor 3D en primera persona • Parkour de alta velocidad • Gráficos Cyberpunk",
                color = Color.DarkGray,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
