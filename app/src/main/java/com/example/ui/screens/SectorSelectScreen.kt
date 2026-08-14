package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.world.DistrictType
import com.example.engine.world.SectorEngine
import com.example.ui.CurrentScreen
import com.example.ui.GameViewModel

@Composable
fun SectorSelectScreen(viewModel: GameViewModel) {
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedSectorsCount.collectAsStateWithLifecycle()
    val solvedCount by viewModel.solvedPuzzlesCount.collectAsStateWithLifecycle()

    var selectedDistrict by remember { mutableStateOf(DistrictType.NEON_ZERO) }
    var searchInput by remember { mutableStateOf("") }

    val progressMap = remember(allProgress) {
        allProgress.associateBy { it.sectorId }
    }

    val sectorsInDistrict = remember(selectedDistrict) {
        selectedDistrict.sectorRange.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070414))
            .padding(top = 36.dp, start = 16.dp, end = 16.dp)
            .testTag("sector_select_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(CurrentScreen.MAIN_MENU) },
                modifier = Modifier.testTag("back_to_menu_from_sectors")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF00F0FF))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "MAPA DE METRÓPOLIS OCCULTA",
                    color = Color(0xFF00F0FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "2344 Sectores // 1557 Enigmas // $completedCount Completados",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Jump / Search by Sector ID
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Saltar a Sector (1 - 2344)", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color(0xFF00F0FF)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF261D4C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    val secId = searchInput.toIntOrNull()
                    if (secId != null && secId in 1..SectorEngine.TOTAL_SECTORS) {
                        viewModel.loadSector(secId)
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_sector_input"),
                singleLine = true
            )

            Button(
                onClick = {
                    val secId = searchInput.toIntOrNull()
                    if (secId != null && secId in 1..SectorEngine.TOTAL_SECTORS) {
                        viewModel.loadSector(secId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp).testTag("warp_search_button")
            ) {
                Text("WARP", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // District Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(DistrictType.values()) { district ->
                val isSelected = district == selectedDistrict
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) district.primaryColor else Color(0xFF140D2E))
                        .border(1.dp, if (isSelected) district.secondaryColor else Color(0xFF261D4C), RoundedCornerShape(10.dp))
                        .clickable { selectedDistrict = district }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("district_tab_${district.id}")
                ) {
                    Text(
                        text = district.districtName,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = selectedDistrict.themeDescription,
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sectors List for Selected District
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sectorsInDistrict) { sectorId ->
                val progress = progressMap[sectorId]
                val hasPuzzle = (sectorId <= 1557) || (sectorId % 2 == 0)
                val puzzleId = if (hasPuzzle) ((sectorId - 1) % 1557) + 1 else null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (progress?.isCompleted == true) Color(0xFF00FF88) else Color(0xFF1E1442), RoundedCornerShape(12.dp))
                        .clickable { viewModel.loadSector(sectorId) }
                        .testTag("sector_item_$sectorId"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0924)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SECTOR #${sectorId.toString().padStart(4, '0')}",
                                    color = selectedDistrict.primaryColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                                if (hasPuzzle) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF00F0FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "🧩 ENIGMA #$puzzleId",
                                            color = Color(0xFF00F0FF),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (progress != null && progress.isCompleted) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    for (s in 1..3) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s <= progress.stars) Color(0xFFFFD700) else Color.DarkGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Récord: ${String.format("%.1f", progress.bestTimeSec)}s",
                                        color = Color(0xFF00FF88),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Text(
                                    text = "Estado: No explorado",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.loadSector(sectorId) },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedDistrict.primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("play_sector_btn_$sectorId")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Jugar", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("JUGAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
