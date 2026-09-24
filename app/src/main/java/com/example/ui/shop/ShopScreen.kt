package com.example.ui.shop

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterTiltShift
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import com.example.model.DinoSkin
import com.example.model.GameMode
import com.example.model.SkinCatalog
import com.example.model.SkinHat
import com.example.model.UpgradeCatalog
import com.example.model.UpgradeItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profileFlow.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tienda Prehistórica",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("shop_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Balance & Free Bonus Button
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${profile.coins}",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Starter / Bonus Coins Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎁 ¿Necesitas más fósiles?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Reclama 100 monedas para probar mejoras y skins.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            viewModel.claimBonusCoins(100)
                            scope.launch {
                                snackbarHostState.showSnackbar("¡Recibiste +100 monedas prehistóricas!")
                            }
                        },
                        modifier = Modifier.testTag("claim_free_coins_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "+100 🪙",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF38BDF8)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Mejoras", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Aspectos", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Modos", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTab) {
                0 -> UpgradesList(
                    profile = profile,
                    onBuyUpgrade = { item, cost ->
                        viewModel.buyUpgrade(
                            upgradeKey = item.id,
                            cost = cost,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("¡${item.name} mejorado exitosamente!")
                                }
                            },
                            onFailure = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No tienes suficientes monedas.")
                                }
                            }
                        )
                    }
                )
                1 -> SkinsList(
                    profile = profile,
                    onEquip = { skinId ->
                        viewModel.equipSkin(skinId)
                        scope.launch {
                            snackbarHostState.showSnackbar("Aspecto equipado.")
                        }
                    },
                    onBuy = { skin, cost ->
                        viewModel.buySkin(
                            skinId = skin.id,
                            cost = cost,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("¡Has desbloqueado ${skin.name}!")
                                }
                            },
                            onFailure = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No tienes suficientes monedas.")
                                }
                            }
                        )
                    }
                )
                2 -> GameModesList(
                    activeModeId = profile.activeGameMode,
                    onSelectMode = { mode ->
                        viewModel.setGameMode(mode)
                        scope.launch {
                            snackbarHostState.showSnackbar("Modo activado: ${mode.title}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UpgradesList(
    profile: com.example.data.GameProfile,
    onBuyUpgrade: (UpgradeItem, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(UpgradeCatalog.items) { item ->
            val currentLevel = when (item.id) {
                "double_jump" -> profile.doubleJumpLevel
                "shield" -> profile.startShieldLevel
                "magnet" -> profile.magnetLevel
                "multiplier" -> profile.coinMultiplierLevel
                "revive" -> profile.reviveLevel
                "slow_mo" -> profile.slowReflexLevel
                else -> 0
            }
            val isMax = item.isMaxLevel(currentLevel)
            val cost = item.getCost(currentLevel)
            val canAfford = profile.coins >= cost && !isMax

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = getUpgradeColor(item.id).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, getUpgradeColor(item.id))
                            ) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(
                                        imageVector = getUpgradeIcon(item.id),
                                        contentDescription = null,
                                        tint = getUpgradeColor(item.id),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = item.badgeLabel,
                                    color = getUpgradeColor(item.id),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Level indicators (e.g. 3 bars)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..item.maxLevel) {
                                val isFilled = i <= currentLevel
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isFilled) getUpgradeColor(item.id) else Color(0xFF334155))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.getEffectDescription(currentLevel),
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isMax) {
                            Surface(
                                color = Color(0xFF065F46),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "NIVEL MÁXIMO",
                                    color = Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = { onBuyUpgrade(item, cost) },
                                enabled = canAfford,
                                modifier = Modifier.testTag("buy_${item.id}_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = getUpgradeColor(item.id),
                                    disabledContainerColor = Color(0xFF334155)
                                )
                            ) {
                                Text(
                                    text = "Mejorar: $cost 🪙",
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkinsList(
    profile: com.example.data.GameProfile,
    onEquip: (String) -> Unit,
    onBuy: (DinoSkin, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(SkinCatalog.skins) { skin ->
            val isUnlocked = profile.isSkinUnlocked(skin.id)
            val isEquipped = profile.selectedSkinId == skin.id
            val canAfford = profile.coins >= skin.cost

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = if (isEquipped) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF38BDF8)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dino Skin Preview Avatar
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, skin.bodyColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (skin.hatType) {
                                    SkinHat.CROWN -> "👑"
                                    SkinHat.SUNGLASSES -> "🕶️"
                                    SkinHat.ASTRONAUT_HELMET -> "🚀"
                                    SkinHat.FIRE_SPIKES -> "🔥"
                                    SkinHat.BANDANA -> "🦴"
                                    SkinHat.NONE -> "🦖"
                                },
                                fontSize = 28.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 6.dp)
                                    .background(skin.bodyColor, RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = skin.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = skin.bodyColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = skin.rarity,
                                    color = skin.bodyColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = skin.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEquipped) {
                            Surface(
                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "EQUIPADO",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (isUnlocked) {
                            OutlinedButton(
                                onClick = { onEquip(skin.id) },
                                modifier = Modifier.testTag("equip_${skin.id}_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "Equipar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onBuy(skin, skin.cost) },
                                enabled = canAfford,
                                modifier = Modifier.testTag("buy_${skin.id}_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B),
                                    disabledContainerColor = Color(0xFF334155)
                                )
                            ) {
                                Text(
                                    text = "Desbloquear: ${skin.cost} 🪙",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color.Black else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameModesList(
    activeModeId: String,
    onSelectMode: (GameMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(GameMode.entries) { mode ->
            val isSelected = mode.id.equals(activeModeId, ignoreCase = true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMode(mode) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, mode.primaryColor) else null
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = mode.primaryColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, mode.primaryColor)
                            ) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    Icon(
                                        imageVector = when (mode) {
                                            GameMode.CLASSIC -> Icons.Default.Landscape
                                            GameMode.COLOR -> Icons.Default.ColorLens
                                            GameMode.REALISTIC -> Icons.Default.Image
                                        },
                                        contentDescription = null,
                                        tint = mode.primaryColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = mode.subtitle,
                                    color = mode.accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isSelected) {
                            Surface(
                                color = mode.primaryColor,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ACTIVO",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = mode.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isSelected) {
                        Button(
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("activate_${mode.id.lowercase()}_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = mode.primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Activar ${mode.title}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getUpgradeColor(id: String): Color {
    return when (id) {
        "double_jump" -> Color(0xFF38BDF8)
        "shield" -> Color(0xFF3B82F6)
        "magnet" -> Color(0xFFEF4444)
        "multiplier" -> Color(0xFFF59E0B)
        "revive" -> Color(0xFFEC4899)
        "slow_mo" -> Color(0xFF8B5CF6)
        else -> Color(0xFF10B981)
    }
}

private fun getUpgradeIcon(id: String): ImageVector {
    return when (id) {
        "double_jump" -> Icons.Default.Flight
        "shield" -> Icons.Default.Shield
        "magnet" -> Icons.Default.FilterTiltShift
        "multiplier" -> Icons.Default.MonetizationOn
        "revive" -> Icons.Default.Favorite
        "slow_mo" -> Icons.Default.Speed
        else -> Icons.Default.Upgrade
    }
}
