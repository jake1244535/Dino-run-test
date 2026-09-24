package com.example.model

import androidx.compose.ui.graphics.Color

data class DinoSkin(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val bodyColor: Color,
    val bellyColor: Color,
    val eyeColor: Color,
    val accentColor: Color,
    val rarity: String,
    val hatType: SkinHat = SkinHat.NONE
)

enum class SkinHat {
    NONE,
    CROWN,
    SUNGLASSES,
    ASTRONAUT_HELMET,
    FIRE_SPIKES,
    BANDANA
}

object SkinCatalog {
    val skins = listOf(
        DinoSkin(
            id = "classic_rex",
            name = "Rex Clásico",
            description = "El tiranosaurio original que todos conocemos del navegador sin conexión.",
            cost = 0,
            bodyColor = Color(0xFF535353),
            bellyColor = Color(0xFF757575),
            eyeColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFF333333),
            rarity = "Común",
            hatType = SkinHat.NONE
        ),
        DinoSkin(
            id = "neon_raptor",
            name = "Raptor Neón",
            description = "Dino futurista recubierto de fibra de carbono y luces cyberpunk.",
            cost = 80,
            bodyColor = Color(0xFF06B6D4),
            bellyColor = Color(0xFF22D3EE),
            eyeColor = Color(0xFFF43F5E),
            accentColor = Color(0xFF10B981),
            rarity = "Raro",
            hatType = SkinHat.SUNGLASSES
        ),
        DinoSkin(
            id = "golden_rex",
            name = "T-Rex Dorado",
            description = "Bañado en oro jurásico macizo de 24 kilates para reyes de la pista.",
            cost = 180,
            bodyColor = Color(0xFFF59E0B),
            bellyColor = Color(0xFFFBBF24),
            eyeColor = Color(0xFFB45309),
            accentColor = Color(0xFFFEF3C7),
            rarity = "Legendario",
            hatType = SkinHat.CROWN
        ),
        DinoSkin(
            id = "spinosaurus",
            name = "Spinosaurus Feroz",
            description = "Depredador alfa con cresta dorsal llameante y garras afiladas.",
            cost = 120,
            bodyColor = Color(0xFFE11D48),
            bellyColor = Color(0xFFFB7185),
            eyeColor = Color(0xFFFDE047),
            accentColor = Color(0xFF9F1239),
            rarity = "Épico",
            hatType = SkinHat.FIRE_SPIKES
        ),
        DinoSkin(
            id = "astronaut_dino",
            name = "Dino Cósmico",
            description = "Explorador estelar listo para conquistar la gravedad cero.",
            cost = 150,
            bodyColor = Color(0xFF8B5CF6),
            bellyColor = Color(0xFFA78BFA),
            eyeColor = Color(0xFF38BDF8),
            accentColor = Color(0xFFEDE9FE),
            rarity = "Épico",
            hatType = SkinHat.ASTRONAUT_HELMET
        ),
        DinoSkin(
            id = "bone_fossil",
            name = "Fósil Viviente",
            description = "Esqueleto prehistórico preservado en ámbar durante 65 millones de años.",
            cost = 200,
            bodyColor = Color(0xFFE2E8F0),
            bellyColor = Color(0xFFCBD5E1),
            eyeColor = Color(0xFFEF4444),
            accentColor = Color(0xFF94A3B8),
            rarity = "Mítico",
            hatType = SkinHat.BANDANA
        )
    )

    fun find(id: String): DinoSkin {
        return skins.find { it.id == id } ?: skins.first()
    }
}
