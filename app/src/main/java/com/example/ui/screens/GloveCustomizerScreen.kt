package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CurrentScreen
import com.example.ui.GameViewModel

data class GloveSkin(
    val name: String,
    val primaryColor: Color,
    val description: String
)

val GLOVE_SKINS = listOf(
    GloveSkin("Cyan Velocity", Color(0xFF00F0FF), "Fibra de carbono con emisores de neón cian estándar."),
    GloveSkin("Synthwave Pulse", Color(0xFFFF007F), "Aleación polimérica magenta de alta conductividad."),
    GloveSkin("Apex Dorado", Color(0xFFFFD700), "Blindaje de oro cuántico para corredores de élite."),
    GloveSkin("Matrix Esmeralda", Color(0xFF00FF66), "Resonador cibernético sintonizado con el código base."),
    GloveSkin("Obsidiana del Vacío", Color(0xFF7B2CBF), "Material extraterrestre forjado en el Sector 2344.")
)

@Composable
fun GloveCustomizerScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val equippedIndex = profile?.equippedSkinIndex ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070415))
            .padding(top = 36.dp, start = 16.dp, end = 16.dp)
            .testTag("glove_customizer_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(CurrentScreen.MAIN_MENU) },
                modifier = Modifier.testTag("back_from_customizer_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF00F0FF))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "TALLER DE GUANTELETES CIBERNÉTICOS",
                    color = Color(0xFF00F0FF),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Personaliza tus guantes de parkour y ganchos de plasma",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Glove Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(2.dp, GLOVE_SKINS[equippedIndex].primaryColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0826)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val skinColor = GLOVE_SKINS[equippedIndex].primaryColor
                Canvas(modifier = Modifier.size(160.dp, 160.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Hand Forearm
                    val path = Path().apply {
                        moveTo(cx - 40f, cy + 60f)
                        lineTo(cx - 20f, cy - 20f)
                        lineTo(cx + 20f, cy - 20f)
                        lineTo(cx + 40f, cy + 60f)
                        close()
                    }
                    drawPath(path, Color(0xFF13112E))
                    drawPath(path, skinColor, style = Stroke(width = 2.5f))

                    // Knuckles
                    for (i in -2..1) {
                        drawCircle(skinColor, radius = 5f, center = Offset(cx + i * 14f + 7f, cy - 18f))
                    }
                    // Wrist Core
                    drawCircle(skinColor, radius = 14f, center = Offset(cx, cy + 20f))
                    drawCircle(Color.Black, radius = 6f, center = Offset(cx, cy + 20f))
                }

                Text(
                    text = "EQUIPADO: ${GLOVE_SKINS[equippedIndex].name}",
                    color = skinColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Skin List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(GLOVE_SKINS) { index, skin ->
                val isEquipped = index == equippedIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isEquipped) skin.primaryColor else Color(0xFF221845),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.equipGloveSkin(index) }
                        .testTag("skin_option_$index"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) Color(0xFF130D36) else Color(0xFF09061C)
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
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(skin.primaryColor, RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = skin.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = skin.description,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isEquipped) {
                            Icon(Icons.Default.Check, contentDescription = "Equipado", tint = skin.primaryColor)
                        } else {
                            Button(
                                onClick = { viewModel.equipGloveSkin(index) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1547)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("EQUIPAR", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
