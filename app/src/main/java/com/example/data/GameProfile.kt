package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profile")
data class GameProfile(
    @PrimaryKey
    val id: Int = 1,
    val coins: Int = 150, // Initial bonus coins to explore the shop right away!
    val highScoreClassic: Int = 0,
    val highScoreColor: Int = 0,
    val highScoreRealistic: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalObstaclesDojed: Int = 0,
    val totalCoinsEarned: Int = 150,
    
    // Upgrades (Level 0 = not bought, 1..5 = upgrade level)
    val doubleJumpLevel: Int = 0,
    val startShieldLevel: Int = 0,
    val magnetLevel: Int = 0,
    val coinMultiplierLevel: Int = 0,
    val reviveLevel: Int = 0,
    val slowReflexLevel: Int = 0,

    // Equipped customizations
    val selectedSkinId: String = "classic_rex",
    val unlockedSkins: String = "classic_rex", // comma separated skin IDs

    // Selected visual mode: CLASSIC, COLOR, REALISTIC
    val activeGameMode: String = "COLOR",

    // Audio & sensory settings
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    val overallHighScore: Int
        get() = maxOf(highScoreClassic, highScoreColor, highScoreRealistic)

    fun isSkinUnlocked(skinId: String): Boolean {
        return unlockedSkins.split(",").contains(skinId)
    }

    fun withUnlockedSkin(skinId: String): GameProfile {
        val currentList = unlockedSkins.split(",").filter { it.isNotBlank() }.toMutableSet()
        currentList.add(skinId)
        return copy(unlockedSkins = currentList.joinToString(","))
    }
}
