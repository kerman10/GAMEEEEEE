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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberAudioEngine
import com.example.data.model.CodexLoreEntity
import com.example.ui.CurrentScreen
import com.example.ui.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CodexVaultScreen(viewModel: GameViewModel) {
    val chapters by viewModel.allLoreChapters.collectAsStateWithLifecycle()
    var selectedChapter by remember { mutableStateOf<CodexLoreEntity?>(null) }
    var eyeTouchTargetX by remember { mutableFloatStateOf(0f) }
    var eyeTouchTargetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060312))
            .padding(top = 36.dp, start = 16.dp, end = 16.dp)
            .testTag("codex_vault_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(CurrentScreen.MAIN_MENU) },
                modifier = Modifier.testTag("back_from_codex_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color(0xFFFF007F))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "BÓVEDA DEL CÓDICE // EL LIBRO OBSERVADOR",
                    color = Color(0xFFFF007F),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Entidad Sentiente // Registro de Metrópolis Occulta",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive 3D/2D Sentient Eye Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(2.dp, Color(0xFFFF007F).copy(alpha = pulseAlpha), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        eyeTouchTargetX = change.position.x
                        eyeTouchTargetY = change.position.y
                    }
                }
                .testTag("sentient_eye_interactive_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0721)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // Grimoire Hologram Cover Outline
                    drawRoundRect(
                        color = Color(0xFF140D36),
                        topLeft = Offset(cx - 100f, cy - 65f),
                        size = Size(200f, 130f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFF007F),
                        topLeft = Offset(cx - 100f, cy - 65f),
                        size = Size(200f, 130f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                        style = Stroke(width = 2f)
                    )

                    // Cyber Eye Outer Sclera
                    val eyePath = Path().apply {
                        moveTo(cx - 65f, cy)
                        quadraticTo(cx, cy - 45f, cx + 65f, cy)
                        quadraticTo(cx, cy + 45f, cx - 65f, cy)
                        close()
                    }
                    drawPath(eyePath, Color(0xFF04010C))
                    drawPath(eyePath, Color(0xFF00F0FF), style = Stroke(width = 2.5f))

                    // Pupil calculation following touch or hovering
                    val targetX = if (eyeTouchTargetX > 0f) eyeTouchTargetX else cx
                    val targetY = if (eyeTouchTargetY > 0f) eyeTouchTargetY else cy
                    val dx = (targetX - cx) * 0.15f
                    val dy = (targetY - cy) * 0.15f
                    val pupilCenter = Offset((cx + dx).coerceIn(cx - 24f, cx + 24f), (cy + dy).coerceIn(cy - 16f, cy + 16f))

                    // Iris Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFF007F), Color(0xFF00F0FF)),
                            center = pupilCenter,
                            radius = 24f
                        ),
                        radius = 20f,
                        center = pupilCenter
                    )
                    // Pupil Diamond Core
                    drawCircle(color = Color.Black, radius = 9f, center = pupilCenter)
                    drawCircle(color = Color.White, radius = 3f, center = pupilCenter - Offset(3f, 3f))
                }

                Text(
                    text = "Toca para interactuar con la mirada del Códice",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Chapter Detail (if any) or List of Chapters
        if (selectedChapter != null) {
            val ch = selectedChapter!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(14.dp))
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF100A2C)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ch.title,
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { CyberAudioEngine.playBookWhisperSound() }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = Color(0xFFFF007F))
                        }
                    }
                    Text(
                        text = ch.originEra,
                        color = Color(0xFFFFE600),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = ch.contentSpanish,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "← Volver a los capítulos",
                        color = Color(0xFFFF007F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { selectedChapter = null }
                    )
                }
            }
        } else {
            // Chapters List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(chapters) { chapter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (chapter.isUnlocked) Color(0xFF261D4C) else Color.DarkGray.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = chapter.isUnlocked) {
                                selectedChapter = chapter
                                CyberAudioEngine.playBookWhisperSound()
                            }
                            .testTag("lore_chapter_${chapter.chapterId}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (chapter.isUnlocked) Color(0xFF0E0824) else Color(0xFF080514)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (chapter.isUnlocked) Icons.Default.MenuBook else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (chapter.isUnlocked) Color(0xFF00F0FF) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = chapter.title,
                                        color = if (chapter.isUnlocked) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (chapter.isUnlocked) chapter.originEra else "Desbloquea alcanzando Sector #${chapter.triggerSectorId}",
                                        color = if (chapter.isUnlocked) Color(0xFFFFE600) else Color.DarkGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
