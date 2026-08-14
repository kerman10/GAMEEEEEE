package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.puzzle.PuzzleState
import com.example.engine.puzzle.PuzzleType

@Composable
fun CyberPuzzleDialog(
    puzzle: PuzzleState,
    onRotateNode: (Int) -> Unit,
    onAdjustFrequency: (Int, Float) -> Unit,
    onInputSequence: (Int) -> Unit,
    onClose: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00F0FF).copy(alpha = borderGlow),
                                Color(0xFFFF007F).copy(alpha = borderGlow)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("cyber_puzzle_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A071E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Puzzle ID and Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ENIGMA CUÁNTICO #${puzzle.puzzleId} / 1557",
                                color = Color(0xFF00F0FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = puzzle.type.titleSpanish,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("close_puzzle_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = puzzle.type.instructionSpanish,
                        color = Color(0xFFB0B3C6),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Puzzle Body based on Type
                    when (puzzle.type) {
                        PuzzleType.QUANTUM_CIRCUITS, PuzzleType.LASER_REFLECTION, PuzzleType.LOGIC_MATRIX -> {
                            // Node Grid
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                puzzle.nodes.forEach { node ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (node.isConnected) Color(0xFF00382B) else Color(0xFF191238)
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = if (node.isConnected) Color(0xFF00FF88) else Color(0xFFFF007F),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onRotateNode(node.id) }
                                                .testTag("puzzle_node_${node.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Girar ${node.label}",
                                                tint = if (node.isConnected) Color(0xFF00FF88) else Color(0xFF00F0FF),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .rotate(node.rotationAngle)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${node.rotationAngle.toInt()}°",
                                            color = if (node.isConnected) Color(0xFF00FF88) else Color.Gray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        PuzzleType.HARMONIC_FREQUENCY -> {
                            // 3 Sliders
                            Column(modifier = Modifier.fillMaxWidth()) {
                                puzzle.currentValues.forEachIndexed { index, value ->
                                    val target = puzzle.targetValues.getOrNull(index) ?: 50f
                                    val isMatched = kotlin.math.abs(value - target) < 5f

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "ARMÓNICO 0${index + 1}: ${value.toInt()} Hz",
                                            color = if (isMatched) Color(0xFF00FF88) else Color(0xFF00F0FF),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "OBJETIVO: ${target.toInt()} Hz",
                                            color = Color(0xFFFF007F),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Slider(
                                        value = value,
                                        onValueChange = { onAdjustFrequency(index, it) },
                                        valueRange = 10f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = if (isMatched) Color(0xFF00FF88) else Color(0xFF00F0FF),
                                            activeTrackColor = if (isMatched) Color(0xFF00FF88) else Color(0xFF00F0FF)
                                        ),
                                        modifier = Modifier.testTag("frequency_slider_$index")
                                    )
                                }
                            }
                        }

                        PuzzleType.CIPHER_SEQUENCE -> {
                            // Cipher rune pads
                            val symbols = listOf("Ψ", "Ω", "Δ", "Ξ")
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Secuencia objetivo: ${puzzle.sequenceTarget.joinToString(" ") { symbols[it] }}",
                                    color = Color(0xFFFFE600),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Entrada actual: ${puzzle.currentSequence.joinToString(" ") { symbols[it] }}",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    symbols.forEachIndexed { index, sym ->
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1A1238))
                                                .border(2.dp, Color(0xFF00F0FF), CircleShape)
                                                .clickable { onInputSequence(index) }
                                                .testTag("cipher_symbol_$index"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = sym,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Status Indicator
                    if (puzzle.isSolved) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF00FF88).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Descifrado", tint = Color(0xFF00FF88))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡CÓDIGO DESCIFRADO EXITOSAMENTE!",
                                color = Color(0xFF00FF88),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFF0055).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = Color(0xFFFF0055))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NODO BLOQUEADO // INTRODUCE FRECUENCIA",
                                color = Color(0xFFFF0055),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
